package com.example.springbootproject.controller;

import com.example.springbootproject.pojo.Result;
import com.example.springbootproject.service.IFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private IFollowService followService;

    //关注用户
    @PostMapping("/{followeeId}")
    public Result follow(@PathVariable Long followeeId) {
        return followService.follow(followeeId);
    }

    // 取关用户
    @DeleteMapping("/{followeeId}")
    public Result unfollow(@PathVariable Long followeeId) {
        return followService.unfollow(followeeId);
    }

    // 检查是否已关注
    @GetMapping("/status/{followeeId}")
    public Result isFollowing(@PathVariable Long followeeId) {
        return followService.isFollowing(followeeId);
    }

    //粉丝列表
    @GetMapping("/{userId}/followers")
    public Result followers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return followService.listFollowers(userId, page, size);
    }

    // 关注列表
    @GetMapping("/{userId}/following")
    public Result following(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return followService.listFollowing(userId, page, size);
    }
}
