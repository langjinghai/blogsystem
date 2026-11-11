package com.example.springbootproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class loginForm {
    private String phone;
    private String password;
    private String code;
    //注册/忘记密码时用于二次确认
    private String confirmPassword;
    //忘记密码时的新密码
    private String newPassword;
}
