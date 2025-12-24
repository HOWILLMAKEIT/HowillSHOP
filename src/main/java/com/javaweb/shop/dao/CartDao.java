package com.javaweb.shop.dao;

import com.javaweb.shop.model.CartItem;
import com.javaweb.shop.model.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 购物车数据访问
public class CartDao {
    private final DataSource dataSource;

    public CartDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long findActiveCartId(long userId) throws SQLException {
        String sql = "SELECT id FROM carts WHERE user_id = ? AND status = 1 LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        return null;
    }

    public long getOrCreateActiveCartId(long userId) throws SQLException {
        Long cartId = findActiveCartId(userId);
        if (cartId != null) {
            return cartId;
        }

        Long existingCartId = findCartId(userId);
        if (existingCartId != null) {
            // 复用历史购物车记录，避免用户多购物车
            updateCartStatus(existingCartId, 1);
            return existingCartId;
        }

        String sql = "INSERT INTO carts (user_id, status) VALUES (?, 1)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, userId);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("创建购物车失败。");
    }

    public Optional<CartItemData> findCartItem(long cartId, long productId) throws SQLException {
        String sql = "SELECT quantity, unit_price FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            stmt.setLong(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int quantity = rs.getInt("quantity");
                    BigDecimal unitPrice = rs.getBigDecimal("unit_price");
                    return Optional.of(new CartItemData(quantity, unitPrice));
                }
            }
        }
        return Optional.empty();
    }

    public void insertCartItem(long cartId, long productId, int quantity,
                               BigDecimal unitPrice, BigDecimal subtotal) throws SQLException {
        String sql = "INSERT INTO cart_items (cart_id, product_id, quantity, unit_price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            stmt.setLong(2, productId);
            stmt.setInt(3, quantity);
            stmt.setBigDecimal(4, unitPrice);
            stmt.setBigDecimal(5, subtotal);
            stmt.executeUpdate();
        }
    }

    public void updateCartItem(long cartId, long productId, int quantity, BigDecimal subtotal)
            throws SQLException {
        String sql = "UPDATE cart_items SET quantity = ?, subtotal = ? " +
                "WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setBigDecimal(2, subtotal);
            stmt.setLong(3, cartId);
            stmt.setLong(4, productId);
            stmt.executeUpdate();
        }
    }

    public void deleteCartItem(long cartId, long productId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            stmt.setLong(2, productId);
            stmt.executeUpdate();
        }
    }

    public List<CartItem> listCartItems(long cartId) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return listCartItems(conn, cartId);
        }
    }

    public List<CartItem> listCartItems(Connection conn, long cartId) throws SQLException {
        // 购物车列表需要商品名和图片，直接联表取
        String sql = "SELECT ci.product_id, ci.quantity, ci.unit_price, ci.subtotal, " +
                "p.name, p.image_url, p.merchant_id " +
                "FROM cart_items ci " +
                "JOIN products p ON ci.product_id = p.id " +
                "WHERE ci.cart_id = ? " +
                "ORDER BY ci.id";
        List<CartItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapCartItem(rs));
                }
            }
        }
        return items;
    }

    private Long findCartId(long userId) throws SQLException {
        String sql = "SELECT id FROM carts WHERE user_id = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        return null;
    }

    public void updateCartStatus(long cartId, int status) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            updateCartStatus(conn, cartId, status);
        }
    }

    public void updateCartStatus(Connection conn, long cartId, int status) throws SQLException {
        String sql = "UPDATE carts SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, status);
            stmt.setLong(2, cartId);
            stmt.executeUpdate();
        }
    }

    public void clearCartItems(Connection conn, long cartId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, cartId);
            stmt.executeUpdate();
        }
    }

    private CartItem mapCartItem(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("product_id"));
        product.setName(rs.getString("name"));
        product.setImageUrl(rs.getString("image_url"));
        product.setPrice(rs.getBigDecimal("unit_price"));
        long merchantId = rs.getLong("merchant_id");
        if (rs.wasNull()) {
            merchantId = 0;
        }
        product.setMerchantId(merchantId);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setSubtotal(rs.getBigDecimal("subtotal"));
        return item;
    }

    public static class CartItemData {
        private final int quantity;
        private final BigDecimal unitPrice;

        public CartItemData(int quantity, BigDecimal unitPrice) {
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }
    }
}
