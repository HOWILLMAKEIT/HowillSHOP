package com.javaweb.shop.model;

// 登录校验用的组合对象
public class UserCredential {
    private final User user;
    // 加密后的密码
    private final String passwordHash;

    public UserCredential(User user, String passwordHash) {
        this.user = user;
        this.passwordHash = passwordHash;
    }

    public User getUser() {
        return user;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
