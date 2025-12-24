package com.javaweb.shop.service;

import com.javaweb.shop.dao.UserDao;
import com.javaweb.shop.model.User;
import com.javaweb.shop.model.UserCredential;
import com.javaweb.shop.util.PasswordHasher;

import java.sql.SQLException;
import java.util.Optional;

// 用户注册与登录校验
public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User register(String username, String email, String phone, String rawPassword, String role)
            throws ValidationException, SQLException {
        if (isBlank(username) || isBlank(email) || isBlank(rawPassword)) {
            throw new ValidationException("用户名、邮箱和密码不能为空。");
        }
        if (rawPassword.length() < 6) {
            throw new ValidationException("密码长度不能少于6位。");
        }

        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim();
        String normalizedPhone = phone == null ? null : phone.trim();
        String normalizedRole = normalizeRole(role);

        // 入库前做唯一性检查，减少重复数据
        if (userDao.findByUsername(normalizedUsername).isPresent()) {
            throw new ValidationException("用户名已被占用。");
        }
        if (userDao.findByEmail(normalizedEmail).isPresent()) {
            throw new ValidationException("邮箱已被注册。");
        }

        // 不保存明文密码
        String passwordHash = PasswordHasher.hash(rawPassword);
        User user = new User();
        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPhone(normalizedPhone);
        user.setRole(normalizedRole);
        user.setStatus(1);

        long userId = userDao.insert(user, passwordHash);
        user.setId(userId);
        return user;
    }

    public User authenticate(String username, String rawPassword)
            throws ValidationException, SQLException {
        if (isBlank(username) || isBlank(rawPassword)) {
            throw new ValidationException("用户名和密码不能为空。");
        }

        Optional<UserCredential> credential = userDao.findCredentialByUsername(username.trim());
        if (credential.isEmpty()) {
            throw new ValidationException("用户名或密码错误。");
        }

        UserCredential data = credential.get();
        if (!PasswordHasher.matches(rawPassword, data.getPasswordHash())) {
            throw new ValidationException("用户名或密码错误。");
        }

        // 兼容旧角色数据，确保只保留两种角色
        data.getUser().setRole(normalizeRole(data.getUser().getRole()));

        // 记录最近登录时间，方便后续审计
        userDao.updateLastLogin(data.getUser().getId());
        return data.getUser();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "CUSTOMER";
        }
        String normalized = role.trim().toUpperCase();
        if ("MERCHANT".equals(normalized) || "ADMIN".equals(normalized)) {
            return "MERCHANT";
        }
        return "CUSTOMER";
    }
}
