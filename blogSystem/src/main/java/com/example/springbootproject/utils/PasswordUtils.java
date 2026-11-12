package com.example.springbootproject.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码哈希工具。
 *
 * <p>新密码统一使用 BCrypt（cost=12）存储；旧的明文密码在应用启动时由
 * {@code PasswordMigrationRunner} 自动迁移为 BCrypt，用户密码本身不变。</p>
 */
public final class PasswordUtils {

    private static final int BCRYPT_COST = 12;

    private PasswordUtils() {
    }

    /** 是否已经是 BCrypt 哈希。 */
    public static boolean isBcrypt(String stored) {
        if (stored == null) {
            return false;
        }
        return stored.startsWith("$2a$")
                || stored.startsWith("$2b$")
                || stored.startsWith("$2y$");
    }

    /** 生成 BCrypt 哈希。 */
    public static String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("password must not be null");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * 校验密码。
     *
     * <p>兼容历史明文数据：如果库中仍然是明文，则按明文比较一次，便于迁移期间登录；
     * 新数据不会走到这个分支。</p>
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            try {
                return BCrypt.checkpw(rawPassword, storedPassword);
            } catch (IllegalArgumentException e) {
                // 格式损坏的哈希一律视为校验失败
                return false;
            }
        }
        return rawPassword.equals(storedPassword);
    }
}
