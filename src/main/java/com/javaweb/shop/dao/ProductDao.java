package com.javaweb.shop.dao;

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

// 商品数据访问
public class ProductDao {
    private final DataSource dataSource;

    public ProductDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<Product> findById(long productId) throws SQLException {
        // 前台仅显示上架商品
        String sql = "SELECT p.id, p.category_id, p.merchant_id, p.name, p.price, p.stock, p.status, " +
                "p.description, p.image_url, c.name AS category_name, u.username AS merchant_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "LEFT JOIN users u ON p.merchant_id = u.id " +
                "WHERE p.id = ? AND p.status = 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Product> findByIdForAdmin(long productId) throws SQLException {
        String sql = "SELECT p.id, p.category_id, p.merchant_id, p.name, p.price, p.stock, p.status, " +
                "p.description, p.image_url, c.name AS category_name, u.username AS merchant_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "LEFT JOIN users u ON p.merchant_id = u.id " +
                "WHERE p.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Product> findByIdForMerchant(long productId, long merchantId) throws SQLException {
        String sql = "SELECT p.id, p.category_id, p.merchant_id, p.name, p.price, p.stock, p.status, " +
                "p.description, p.image_url, c.name AS category_name, u.username AS merchant_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "LEFT JOIN users u ON p.merchant_id = u.id " +
                "WHERE p.id = ? AND p.merchant_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            stmt.setLong(2, merchantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapProduct(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Product> listProducts(Long categoryId) throws SQLException {
        return listProducts(categoryId, null, 0, 50);
    }

    public List<Product> listProducts(Long categoryId, String keyword, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.category_id, p.merchant_id, p.name, p.price, p.stock, p.status, " +
                        "p.description, p.image_url, c.name AS category_name, u.username AS merchant_name " +
                        "FROM products p " +
                        "LEFT JOIN categories c ON p.category_id = c.id " +
                        "LEFT JOIN users u ON p.merchant_id = u.id " +
                        "WHERE p.status = 1 "
        );
        List<Object> params = new ArrayList<>();
        if (categoryId != null) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        if (!isBlank(keyword)) {
            sql.append("AND p.name LIKE ? ");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
        }
        sql.append("ORDER BY p.created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Product> products = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        }
        return products;
    }

    public int countProducts(Long categoryId, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products p WHERE p.status = 1 ");
        List<Object> params = new ArrayList<>();
        if (categoryId != null) {
            sql.append("AND p.category_id = ? ");
            params.add(categoryId);
        }
        if (!isBlank(keyword)) {
            sql.append("AND p.name LIKE ? ");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Product> listAllProducts() throws SQLException {
        String sql = "SELECT p.id, p.category_id, p.merchant_id, p.name, p.price, p.stock, p.status, " +
                "p.description, p.image_url, c.name AS category_name, u.username AS merchant_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "LEFT JOIN users u ON p.merchant_id = u.id " +
                "ORDER BY p.created_at DESC";
        List<Product> products = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapProduct(rs));
            }
        }
        return products;
    }

    public List<Product> listProductsByMerchant(long merchantId) throws SQLException {
        String sql = "SELECT p.id, p.category_id, p.merchant_id, p.name, p.price, p.stock, p.status, " +
                "p.description, p.image_url, c.name AS category_name, u.username AS merchant_name " +
                "FROM products p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "LEFT JOIN users u ON p.merchant_id = u.id " +
                "WHERE p.merchant_id = ? " +
                "ORDER BY p.created_at DESC";
        List<Product> products = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, merchantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }
        }
        return products;
    }

    public long insertProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products " +
                "(category_id, merchant_id, name, price, stock, status, description, image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, product.getCategoryId());
            if (product.getMerchantId() > 0) {
                stmt.setLong(2, product.getMerchantId());
            } else {
                stmt.setObject(2, null);
            }
            stmt.setString(3, product.getName());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStock());
            stmt.setInt(6, product.getStatus());
            stmt.setString(7, product.getDescription());
            stmt.setString(8, product.getImageUrl());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("商品创建失败。");
    }

    public void updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, name = ?, price = ?, stock = ?, " +
                "status = ?, description = ?, image_url = ?, updated_at = NOW() " +
                "WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, product.getCategoryId());
            stmt.setString(2, product.getName());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setInt(5, product.getStatus());
            stmt.setString(6, product.getDescription());
            stmt.setString(7, product.getImageUrl());
            stmt.setLong(8, product.getId());
            stmt.executeUpdate();
        }
    }

    public int updateProductForMerchant(Product product, long merchantId) throws SQLException {
        String sql = "UPDATE products SET category_id = ?, name = ?, price = ?, stock = ?, " +
                "status = ?, description = ?, image_url = ?, updated_at = NOW() " +
                "WHERE id = ? AND merchant_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, product.getCategoryId());
            stmt.setString(2, product.getName());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setInt(5, product.getStatus());
            stmt.setString(6, product.getDescription());
            stmt.setString(7, product.getImageUrl());
            stmt.setLong(8, product.getId());
            stmt.setLong(9, merchantId);
            return stmt.executeUpdate();
        }
    }

    public void deleteProduct(long productId) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            stmt.executeUpdate();
        }
    }

    public int deleteProductForMerchant(long productId, long merchantId) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ? AND merchant_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, productId);
            stmt.setLong(2, merchantId);
            return stmt.executeUpdate();
        }
    }

    public int decreaseStock(Connection conn, long productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock = stock - ? " +
                "WHERE id = ? AND stock >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setLong(2, productId);
            stmt.setInt(3, quantity);
            return stmt.executeUpdate();
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setCategoryId(rs.getLong("category_id"));
        long merchantId = rs.getLong("merchant_id");
        if (rs.wasNull()) {
            merchantId = 0;
        }
        product.setMerchantId(merchantId);
        product.setCategoryName(rs.getString("category_name"));
        product.setMerchantName(rs.getString("merchant_name"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setStatus(rs.getInt("status"));
        product.setDescription(rs.getString("description"));
        product.setImageUrl(rs.getString("image_url"));
        return product;
    }

    private void bindParams(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
