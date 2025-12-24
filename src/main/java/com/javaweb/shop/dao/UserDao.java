package com.javaweb.shop.dao;

import com.javaweb.shop.model.User;
import com.javaweb.shop.model.UserCredential;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

// 用户数据访问
public class UserDao {
    private final DataSource dataSource;

    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, email, phone, role, status, created_at, last_login_at " +
                "FROM users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id, username, email, phone, role, status, created_at, last_login_at " +
                "FROM users WHERE email = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<UserCredential> findCredentialByUsername(String username) throws SQLException {
        // 只允许启用状态用户登录
        String sql = "SELECT id, username, password_hash, email, phone, role, status, created_at, last_login_at " +
                "FROM users WHERE username = ? AND status = 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    String passwordHash = rs.getString("password_hash");
                    return Optional.of(new UserCredential(user, passwordHash));
                }
            }
        }
        return Optional.empty();
    }

    public long insert(User user, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, email, phone, role, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, passwordHash);
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getRole());
            stmt.setInt(6, user.getStatus());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("用户创建失败。");
    }

    public void updateLastLogin(long userId) throws SQLException {
        String sql = "UPDATE users SET last_login_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getInt("status"));
        user.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        user.setLastLoginAt(toLocalDateTime(rs.getTimestamp("last_login_at")));
        return user;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }
}
