package com.example.springbootproject.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootproject.pojo.Follow;
import com.example.springbootproject.pojo.Result;

public interface IFollowService extends IService<Follow> {

    /** 关注用户 */
    Result follow(Long followeeId);

    /** 取关用户 */
    Result unfollow(Long followeeId);

    /** 检查当前用户是否已关注目标用户 */
    Result isFollowing(Long followeeId);

    /** 分页查询粉丝列表 */
    Result listFollowers(Long userId, Integer page, Integer size);

    /** 分页查询关注列表 */
    Result listFollowing(Long userId, Integer page, Integer size);

    /** 粉丝数 */
    Long getFollowerCount(Long userId);

    /** 关注数 */
    Long getFollowingCount(Long userId);

}
