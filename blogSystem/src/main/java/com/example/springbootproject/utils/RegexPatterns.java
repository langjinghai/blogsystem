package com.example.springbootproject.utils;


public abstract class RegexPatterns {
    //手机号正则
    public static final String PHONE_REGEX = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";

    //邮箱正则（通用邮箱；QQ/Foxmail 也满足）
    public static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

}
