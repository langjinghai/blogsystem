package com.example.springbootproject.utils;

public class Constants {
    // 登录验证码相关
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    // 登录用户会话相关
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 3600L;
    public static final String USER_NICK_NAME_PREFIX = "user_";
    public static final long MAX_PAGE_SIZE = 10;
    public static final  String BLOG_LIKED_KEY = "blog:liked:";
    // 关注相关
    public static final String FOLLOW_FOLLOWING_KEY = "follow:following:";
}

