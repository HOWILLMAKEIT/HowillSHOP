package com.javaweb.shop.dao;

import com.javaweb.shop.model.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// 分类数据访问
public class CategoryDao {
    private final DataSource dataSource;

    public CategoryDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Category> listActiveCategories() throws SQLException {
        // 只取启用分类，给前台筛选用
        String sql = "SELECT id, name FROM categories WHERE status = 1 ORDER BY sort_order, id";
        List<Category> categories = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Category category = new Category();
                category.setId(rs.getLong("id"));
                category.setName(rs.getString("name"));
                categories.add(category);
            }
        }
        return categories;
    }
}
