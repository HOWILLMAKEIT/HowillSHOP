package com.javaweb.shop.util;

import org.mindrot.jbcrypt.BCrypt;

// 密码哈希工具
public final class PasswordHasher {
    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        // bcrypt 成本因子固定为 10，后续需要可配置化
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }

    public static boolean matches(String rawPassword, String hash) {
        return BCrypt.checkpw(rawPassword, hash);
    }
}
