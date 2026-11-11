package com.example.springbootproject.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootproject.mapper.FollowMapper;
import com.example.springbootproject.mapper.UserMapper;
import com.example.springbootproject.pojo.Follow;
import com.example.springbootproject.pojo.Result;
import com.example.springbootproject.pojo.User;
import com.example.springbootproject.pojo.UserDTO;
import com.example.springbootproject.service.IFollowService;
import com.example.springbootproject.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.springbootproject.utils.Constants.FOLLOW_FOLLOWING_KEY;

@Slf4j
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result follow(Long followeeId) {
        // 1. 获取当前用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.fail("未登录");//会直接拦截
        }
        Long followerId = currentUser.getId();
        // 2. 不能自己关注自己（前端好像没有对应的接口，可省略）
        if (followerId.equals(followeeId)) {
            return Result.fail("不能关注自己");
        }

        // 3. 检查目标用户是否存在
        User followee = userMapper.selectById(followeeId);
        if (followee == null) {
            return Result.fail("用户不存在");//防止删除的用户还能被关注
        }
        // 4. Redis 检查是否已关注
        String key = FOLLOW_FOLLOWING_KEY + followerId;
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, followeeId.toString());
        if (Boolean.TRUE.equals(isMember)) {
            return Result.fail("已关注该用户");
        }
        // 5. 保存到数据库
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        try {
            save(follow);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // DB 已有记录但 Redis 丢失 → 补写 Redis
            stringRedisTemplate.opsForSet().add(key, followeeId.toString());
            return Result.fail("已关注该用户");
        }
        // 6. 写入 Redis Set
        stringRedisTemplate.opsForSet().add(key, followeeId.toString());
        log.info("关注成功：{} -> {}", followerId, followeeId);
        return Result.ok();
    }

    @Override
    public Result unfollow(Long followeeId) {
        // 1. 获取当前用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.fail("未登录");
        }
        Long followerId = currentUser.getId();

        // 2. 删除数据库记录
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<Follow>()
                .eq(Follow::getFollowerId, followerId)
                .eq(Follow::getFolloweeId, followeeId);
        boolean removed = remove(wrapper);

        if (!removed) {
            return Result.fail("未关注该用户");
        }

        // 3. 从 Redis Set 中移除
        String key = FOLLOW_FOLLOWING_KEY + followerId;
        stringRedisTemplate.opsForSet().remove(key, followeeId.toString());

        log.info("取关成功：{} -> {}", followerId, followeeId);
        return Result.ok();
    }

    @Override
    public Result isFollowing(Long followeeId) {
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.ok(false);
        }
        String key = FOLLOW_FOLLOWING_KEY + currentUser.getId();
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, followeeId.toString());
        return Result.ok(Boolean.TRUE.equals(isMember));
    }
    @Override
    public Result listFollowers(Long userId, Integer page, Integer size) {
        UserDTO currentUser = UserHolder.getUser();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        // 从数据库分页查询粉丝
        Page<Follow> followPage = query()
                .eq("followee_id", userId)
                .orderByDesc("create_time")
                .page(new Page<>(page, size));

        if (followPage.getRecords().isEmpty()) {
            return Result.ok(Collections.emptyList(), 0L);
        }

        // 关注者ID
        List<Long> followerIds = followPage.getRecords().stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toList());

        // 批量查用户信息
        List<User> users = userMapper.selectBatchIds(followerIds);

        // 用 Redis 判断当前用户是否关注了这些粉丝
        List<UserDTO> result = users.stream().map(user -> {
            UserDTO dto = BeanUtil.copyProperties(user, UserDTO.class);
            if (currentUserId != null) {
                String key = FOLLOW_FOLLOWING_KEY + currentUserId;
                dto.setIsFollowed(Boolean.TRUE.equals(
                        stringRedisTemplate.opsForSet().isMember(key, user.getId().toString())));
            }
            return dto;
        }).collect(Collectors.toList());

        return Result.ok(result, followPage.getTotal());
    }

    @Override
    public Result listFollowing(Long userId, Integer page, Integer size) {
        UserDTO currentUser = UserHolder.getUser();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        // 从数据库分页查询关注列表
        Page<Follow> followPage = query()
                .eq("follower_id", userId)
                .orderByDesc("create_time")
                .page(new Page<>(page, size));

        if (followPage.getRecords().isEmpty()) {
            return Result.ok(Collections.emptyList(), 0L);
        }

        // 收集被关注者ID
        List<Long> followeeIds = followPage.getRecords().stream()
                .map(Follow::getFolloweeId)
                .collect(Collectors.toList());

        // 批量查用户信息
        List<User> users = userMapper.selectBatchIds(followeeIds);

        // 用 Redis 判断当前用户是否也关注了这些人
        List<UserDTO> result = users.stream().map(user -> {
            UserDTO dto = BeanUtil.copyProperties(user, UserDTO.class);
            if (currentUserId != null) {
                String key = FOLLOW_FOLLOWING_KEY + currentUserId;
                dto.setIsFollowed(Boolean.TRUE.equals(
                        stringRedisTemplate.opsForSet().isMember(key, user.getId().toString())));
            }
            return dto;
        }).collect(Collectors.toList());

        return Result.ok(result, followPage.getTotal());
    }

    @Override
    public Long getFollowerCount(Long userId) {
        // 粉丝数用数据库 count函数统计
        return Long.valueOf(query().eq("followee_id", userId).count());
    }

    @Override
    public Long getFollowingCount(Long userId) {
        String key = FOLLOW_FOLLOWING_KEY + userId;
        Long size = stringRedisTemplate.opsForSet().size(key);
        if (size != null && size > 0) {
            return size;
        }
        return Long.valueOf(query().eq("follower_id", userId).count());
    }
}
