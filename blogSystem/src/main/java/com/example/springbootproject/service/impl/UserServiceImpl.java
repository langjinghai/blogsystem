package com.example.springbootproject.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springbootproject.mapper.ArticleMapper;
import com.example.springbootproject.mapper.UserMapper;
import com.example.springbootproject.pojo.Article;
import com.example.springbootproject.pojo.Result;
import com.example.springbootproject.pojo.User;
import com.example.springbootproject.pojo.UserDTO;
import com.example.springbootproject.pojo.loginForm;
import com.example.springbootproject.service.IFollowService;
import com.example.springbootproject.service.IUserService;
import com.example.springbootproject.utils.PasswordUtils;
import com.example.springbootproject.utils.RegexUtils;
import com.example.springbootproject.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.springbootproject.utils.Constants.*;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private IFollowService followService;

    @Override
    public Result login(loginForm loginForm) {
        //  校验手机号格式
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }

        //  根据手机号查询用户
        User user = query().eq("phone", phone).one();


        if (user == null) {
            return Result.fail("用户不存在，请先注册");
        }

        String code = loginForm.getCode();
        String password = loginForm.getPassword();

        // 4. 验证码登录模式
        if (code != null && !code.isEmpty()) {
            String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
            if (cacheCode == null || !cacheCode.equals(code)) {
                return Result.fail("验证码错误");
            }
            // 验证通过后删除已使用的验证码
            stringRedisTemplate.delete(LOGIN_CODE_KEY + phone);
        }
        // 5. 密码登录模式
        else if (password != null && !password.isEmpty()) {

            if (!PasswordUtils.matches(password, user.getPassword())) {
                return Result.fail("密码错误");
            }
            // 兼容历史明文数据：密码登录成功后顺手升级为 BCrypt
            if (!PasswordUtils.isBcrypt(user.getPassword())) {
                user.setPassword(PasswordUtils.encode(password));
                updateById(user);
            }
        }
        // 6. 既无验证码也无密码
        else {
            return Result.fail("请提供验证码或密码");
        }

        // 7. 生成token，存入Redis
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(
                userDTO,
                new java.util.HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString())
        );
        userMap.put("tokenVersion", currentTokenVersion(user.getId()));
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.SECONDS);

        return Result.ok(token);
    }

    @Override
    public Result sendCode(String phone) {
        // 校验手机号
        boolean phoneInvalid = RegexUtils.isPhoneInvalid(phone);
        if (phoneInvalid) {
            return Result.fail("手机号格式错误");
        }
        String code = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        log.info("发送短信验证码成功，验证码：{}", code);

        return Result.ok();
    }

    @Override
    public Result register(loginForm loginForm) {
        // 1. 校验手机号格式
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }
        // 2. 检查用户是否已存在
        User existUser = query().eq("phone", phone).one();
        if (existUser != null) {
            return Result.fail("该手机号已注册，请直接登录");
        }
        // 3. 校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误");
        }



        // 4. 校验密码
        String password = loginForm.getPassword();
        String confirmPassword = loginForm.getConfirmPassword();
        if (password == null || password.length() < 4 || password.length() > 32) {
            return Result.fail("密码为4~32位");
        }
        if (!password.equals(confirmPassword)) {
            return Result.fail("两次密码不一致");
        }

        // 5. 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setPassword(PasswordUtils.encode(password));
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(6));
        save(user);

        // 6. 删除已使用的验证码
        stringRedisTemplate.delete(LOGIN_CODE_KEY + phone);

        // 7. 自动登录：生成token存入Redis
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(
                userDTO,
                new java.util.HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue == null ? null : fieldValue.toString())
        );
        userMap.put("tokenVersion", currentTokenVersion(user.getId()));
        stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.SECONDS);

        log.info("新用户注册成功：{}", phone);
        return Result.ok(token);
    }

    @Override
    public Result updatePassword(loginForm loginForm) {
        // 1. 从 ThreadLocal 获取当前登录用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.fail("未登录");
        }

        // 2. 校验旧密码
        User user = getById(currentUser.getId());
        if (user == null) {
            return Result.fail("用户不存在");
        }
        String oldPassword = loginForm.getPassword(); // loginForm.password 当作旧密码
        if (!PasswordUtils.matches(oldPassword, user.getPassword())) {
            return Result.fail("旧密码错误");
        }

        // 3. 校验新密码
        String newPassword = loginForm.getNewPassword();
        String confirmPassword = loginForm.getConfirmPassword();
        if (newPassword == null || newPassword.length() < 4 || newPassword.length() > 32) {
            return Result.fail("密码为4~32位");
        }
        if (!newPassword.equals(confirmPassword)) {
            return Result.fail("两次密码不一致");
        }

        // 4. 更新密码
        user.setPassword(PasswordUtils.encode(newPassword));
        updateById(user);
        // 让该用户所有旧 token 立即失效
        invalidateUserTokens(user.getId());

        log.info("密码修改成功：{}", user.getPhone());
        return Result.ok();
    }

    @Override
    public Result forget(loginForm loginForm) {
        // 1. 校验手机号格式
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
        }

        // 2. 校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误");
        }

        // 3. 校验新密码
        String newPassword = loginForm.getNewPassword();
        String confirmPassword = loginForm.getConfirmPassword();
        if (newPassword == null || newPassword.length() < 4 || newPassword.length() > 32) {
            return Result.fail("密码为4~32位");
        }
        if (!newPassword.equals(confirmPassword)) {
            return Result.fail("两次密码不一致");
        }

        // 4. 查找用户
        User existUser = query().eq("phone", phone).one();
        if (existUser == null) {
            return Result.fail("该手机号未注册，请先注册");
        }

        // 5. 更新密码
        existUser.setPassword(PasswordUtils.encode(newPassword));
        updateById(existUser);
        // 重置密码后，该用户所有旧 token 立即失效
        invalidateUserTokens(existUser.getId());

        // 6. 删除已使用的验证码
        stringRedisTemplate.delete(LOGIN_CODE_KEY + phone);

        log.info("密码重置成功：{}", phone);
        return Result.ok();
    }
    //获取用户信息
    @Override
    public Result getUserInfo(Long id) {
        User user = getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);

        // 统计该用户的文章数和总获赞数
        List<Article> userArticles = articleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Article>()
                        .eq(Article::getAuthorId, id)
        );
        int articleCount = userArticles.size();
        int totalLikes = userArticles.stream()
                .mapToInt(a -> a.getLikeCount() != null ? a.getLikeCount() : 0)
                .sum();

        userDTO.setArticleCount(articleCount);
        userDTO.setLikeCount(totalLikes);

        // 统计关注数和粉丝数
        userDTO.setFollowingCount(followService.getFollowingCount(id).intValue());
        userDTO.setFollowerCount(followService.getFollowerCount(id).intValue());

        return Result.ok(userDTO);
    }

    // 获取他人主页信息（含是否已关注）
    @Override
    public Result getUserProfileById(Long profileId) {
        User user = getById(profileId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);

        // 统计该用户的文章数和总获赞数
        List<Article> userArticles = articleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Article>()
                        .eq(Article::getAuthorId, profileId)
        );
        int articleCount = userArticles.size();
        int totalLikes = userArticles.stream()
                .mapToInt(a -> a.getLikeCount() != null ? a.getLikeCount() : 0)
                .sum();

        userDTO.setArticleCount(articleCount);
        userDTO.setLikeCount(totalLikes);
        userDTO.setFollowingCount(followService.getFollowingCount(profileId).intValue());
        userDTO.setFollowerCount(followService.getFollowerCount(profileId).intValue());

        // 检查当前登录用户是否已关注此人
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser != null && !currentUser.getId().equals(profileId)) {
            Result isFollowingResult = followService.isFollowing(profileId);
            userDTO.setIsFollowed((Boolean) isFollowingResult.getData());
        } else {
            userDTO.setIsFollowed(false);
        }

        return Result.ok(userDTO);
    }

    //更新用户资料
    @Override
    public Result updateProfile(UserDTO userDTO, String token) {
        // 1. 获取当前用户
        UserDTO currentUser = UserHolder.getUser();
        if (currentUser == null) {
            return Result.fail("未登录");
        }

        // 2. 只允许更新 nickName 和 icon
        User user = getById(currentUser.getId());
        if (userDTO.getNickName() != null && !userDTO.getNickName().isEmpty()) {
            user.setNickName(userDTO.getNickName());
        }
        if (userDTO.getIcon() != null) {
            user.setIcon(userDTO.getIcon());
        }
        updateById(user);

        // 3. 同步更新 Redis 缓存
        if (token != null && !token.isEmpty()) {
            UserDTO updatedDTO = BeanUtil.copyProperties(user, UserDTO.class);
            Map<String, Object> userMap = BeanUtil.beanToMap(
                    updatedDTO,
                    new java.util.HashMap<>(),
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setFieldValueEditor((fn, fv) -> fv == null ? null : fv.toString())
            );
            stringRedisTemplate.opsForHash().putAll(LOGIN_USER_KEY + token, userMap);
            stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.SECONDS);
        }

        log.info("用户资料更新成功：{}", currentUser.getId());
        return Result.ok();
    }

    //退出登录
    @Override
    public Result logout(String token) {
        stringRedisTemplate.delete(LOGIN_USER_KEY + token);
        log.info("用户退出登录，token已清除");
        return Result.ok();
    }

    /**
     * 当前用户 token 版本。改密/重置密码时该值自增，旧 token 在拦截器中会被判定失效。
     */
    private String currentTokenVersion(Long userId) {
        String version = stringRedisTemplate.opsForValue().get(USER_TOKEN_VERSION_KEY + userId);
        return version == null ? "0" : version;
    }

    /**
     * 让该用户所有已签发的 token 立即失效（改密、重置密码后调用）。
     * 不删除 token 本身，而是递增版本号，避免遍历大量 Redis key。
     */
    private void invalidateUserTokens(Long userId) {
        try {
            stringRedisTemplate.opsForValue().increment(USER_TOKEN_VERSION_KEY + userId);
        } catch (Exception e) {
            log.error("刷新用户 token 版本失败，userId={}", userId, e);
        }
    }

}
