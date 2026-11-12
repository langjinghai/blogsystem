package com.example.springbootproject.config;

import com.example.springbootproject.mapper.UserMapper;
import com.example.springbootproject.pojo.User;
import com.example.springbootproject.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时把历史明文密码迁移为 BCrypt。
 *
 * <p>迁移方式是对库中现有的明文值直接做 BCrypt，因此用户仍然可以用原密码登录；
 * 已经迁移过的账号（$2a$/$2b$/$2y$ 开头）会被跳过，可重复执行。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserMapper userMapper;

    @Override
    public void run(String... args) {
        List<User> users = userMapper.selectList(null);
        int migrated = 0;
        for (User user : users) {
            String stored = user.getPassword();
            if (stored != null && !stored.isEmpty() && !PasswordUtils.isBcrypt(stored)) {
                user.setPassword(PasswordUtils.encode(stored));
                userMapper.updateById(user);
                migrated++;
            }
        }
        if (migrated > 0) {
            log.info("已将 {} 个存量明文密码迁移为 BCrypt", migrated);
        }
    }
}
