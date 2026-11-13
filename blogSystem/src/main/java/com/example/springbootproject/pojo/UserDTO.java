package com.example.springbootproject.pojo;

import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO {
    // 用户ID
    private Long id;
    // 手机号
    private String phone;
    // 邮箱（公开用户信息接口中会被置空）
    private String email;
    // 用户昵称
    private String nickName;
    // 用户头像
    private String icon;
    //文章数
    private Integer articleCount;
    //点赞数
    private Integer likeCount;
    //粉丝数
    private Integer followerCount;
    //关注数
    private Integer followingCount;
    //当前登录用户是否已关注此人
    private Boolean isFollowed;

}
