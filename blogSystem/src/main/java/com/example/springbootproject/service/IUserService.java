package com.example.springbootproject.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootproject.pojo.Result;
import com.example.springbootproject.pojo.User;
import com.example.springbootproject.pojo.UserDTO;
import com.example.springbootproject.pojo.loginForm;

public interface IUserService extends IService<User> {
    Result login(loginForm loginForm);

    Result sendCode(String phone, String scene);

    Result sendEmailCode(String email, String scene);

    Result emailLogin(loginForm loginForm);

    Result emailRegister(loginForm loginForm);

    Result emailForget(loginForm loginForm);

    Result register(loginForm loginForm);

    Result updatePassword(loginForm loginForm);

    Result forget(loginForm loginForm);

    Result getUserInfo(Long id);

    Result getUserProfileById(Long profileId);

    Result updateProfile(UserDTO userDTO, String token);

    Result logout(String token);


}

