package com.example.springbootproject.utils;

import cn.hutool.core.bean.BeanUtil;
import com.example.springbootproject.pojo.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.springbootproject.utils.Constants.LOGIN_USER_KEY;
import static com.example.springbootproject.utils.Constants.LOGIN_USER_TTL;
import static com.example.springbootproject.utils.Constants.USER_TOKEN_VERSION_KEY;

public class LoginInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 1. 从请求头获取 token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        String token = authHeader.substring(7);
        // 2. 从 Redis 查用户信息
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash()
                .entries(LOGIN_USER_KEY + token);
        if (userMap.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 3. 校验 token 版本：改密/重置密码后旧 token 立即失效
        Object userIdValue = userMap.get("id");
        String currentVersion = "0";
        if (userIdValue != null) {
            String version = stringRedisTemplate.opsForValue()
                    .get(USER_TOKEN_VERSION_KEY + userIdValue);
            if (version != null) {
                currentVersion = version;
            }
        }
        Object tokenVersionValue = userMap.get("tokenVersion");
        String tokenVersion = tokenVersionValue == null ? "0" : tokenVersionValue.toString();
        if (!currentVersion.equals(tokenVersion)) {
            stringRedisTemplate.delete(LOGIN_USER_KEY + token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 4. Map → UserDTO，写入 ThreadLocal（tokenVersion 不是 UserDTO 字段，先移除）
        Map<Object, Object> beanMap = new HashMap<>(userMap);
        beanMap.remove("tokenVersion");
        UserDTO userDTO = BeanUtil.mapToBean(beanMap, UserDTO.class, false, null);
        UserHolder.saveUser(userDTO);
        // 5. 重新刷新 token 有效期
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
