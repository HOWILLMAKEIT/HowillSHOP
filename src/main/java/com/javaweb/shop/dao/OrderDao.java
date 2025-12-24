package com.javaweb.shop.dao;

import com.javaweb.shop.model.CartItem;
import com.javaweb.shop.model.Order;
import com.javaweb.shop.model.OrderItem;
import com.javaweb.shop.model.OrderMailInfo;
import com.javaweb.shop.model.ProductSales;
import com.javaweb.shop.model.SalesSummary;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// 订单数据访问与统计查询
public class OrderDao {
    private final DataSource dataSource;

    public OrderDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public long insertOrder(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO orders " +
                "(order_no, user_id, merchant_id, total_amount, order_status, pay_status, ship_status, " +
                "receiver_name, receiver_phone, receiver_address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, order.getOrderNo());
            stmt.setLong(2, order.getUserId());
            if (order.getMerchantId() > 0) {
                stmt.setLong(3, order.getMerchantId());
            } else {
                stmt.setObject(3, null);
            }
            stmt.setBigDecimal(4, order.getTotalAmount());
            stmt.setString(5, order.getOrderStatus());
            stmt.setString(6, order.getPayStatus());
            stmt.setString(7, order.getShipStatus());
            stmt.setString(8, order.getReceiverName());
            stmt.setString(9, order.getReceiverPhone());
            stmt.setString(10, order.getReceiverAddress());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("订单创建失败。");
    }

    public void insertOrderItems(Connection conn, long orderId, List<CartItem> items)
            throws SQLException {
        // 批量写入明细，减少数据库往返
        String sql = "INSERT INTO order_items " +
                "(order_id, product_id, product_name, quantity, unit_price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CartItem item : items) {
                stmt.setLong(1, orderId);
                stmt.setLong(2, item.getProduct().getId());
                stmt.setString(3, item.getProduct().getName());
                stmt.setInt(4, item.getQuantity());
                stmt.setBigDecimal(5, item.getUnitPrice());
                stmt.setBigDecimal(6, item.getSubtotal());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<Order> listOrdersByUser(long userId) throws SQLException {
        String sql = "SELECT id, order_no, merchant_id, total_amount, order_status, pay_status, ship_status, " +
                "created_at, updated_at FROM orders WHERE user_id = ? ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrder(rs, userId));
                }
            }
        }
        return orders;
    }

    public List<Order> listAllOrders() throws SQLException {
        String sql = "SELECT id, order_no, user_id, merchant_id, total_amount, order_status, pay_status, ship_status, " +
                "created_at, updated_at FROM orders ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapOrderWithUserId(rs));
            }
        }
        return orders;
    }

    public List<Order> listOrdersByOrderStatus(String status) throws SQLException {
        String sql = "SELECT id, order_no, user_id, merchant_id, total_amount, order_status, pay_status, ship_status, " +
                "created_at, updated_at FROM orders WHERE order_status = ? ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrderWithUserId(rs));
                }
            }
        }
        return orders;
    }

    public List<Order> listOrdersByPayStatus(String status) throws SQLException {
        String sql = "SELECT id, order_no, user_id, merchant_id, total_amount, order_status, pay_status, ship_status, " +
                "created_at, updated_at FROM orders WHERE pay_status = ? ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrderWithUserId(rs));
                }
            }
        }
        return orders;
    }

    public List<Order> listOrdersByMerchant(long merchantId) throws SQLException {
        String sql = "SELECT id, order_no, user_id, merchant_id, total_amount, order_status, pay_status, ship_status, " +
                "created_at, updated_at FROM orders WHERE merchant_id = ? ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, merchantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrderWithUserId(rs));
                }
            }
        }
        return orders;
    }

    public List<Order> listOrdersByMerchantAndOrderStatus(long merchantId, String status) throws SQLException {
        String sql = "SELECT id, order_no, user_id, merchant_id, total_amount, order_status, pay_status, ship_status, " +
                "created_at, updated_at FROM orders WHERE merchant_id = ? AND order_status = ? " +
                "ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, merchantId);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrderWithUserId(rs));
                }
            }
        }
        return orders;
    }

    public List<Order> listOrdersByMerchantAndPayStatus(long merchantId, String status) throws SQLException {
        String sql = "SELECT id, order_no, user_id, merchant_id, total_amount, order_status, pay_status, ship_status, " +
                "created_at, updated_at FROM orders WHERE merchant_id = ? AND pay_status = ? " +
                "ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, merchantId);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrderWithUserId(rs));
                }
            }
        }
        return orders;
    }

    public Optional<Order> findOrderForUser(long orderId, long userId) throws SQLException {
        String sql = "SELECT id, order_no, merchant_id, total_amount, order_status, pay_status, ship_status, " +
                "receiver_name, receiver_phone, receiver_address, created_at, updated_at " +
                "FROM orders WHERE id = ? AND user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs, userId);
                    order.setReceiverName(rs.getString("receiver_name"));
                    order.setReceiverPhone(rs.getString("receiver_phone"));
                    order.setReceiverAddress(rs.getString("receiver_address"));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Order> findOrderById(long orderId) throws SQLException {
        String sql = "SELECT id, order_no, user_id, merchant_id, total_amount, order_status, pay_status, ship_status, " +
                "receiver_name, receiver_phone, receiver_address, created_at, updated_at " +
                "FROM orders WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrderWithUserId(rs);
                    order.setReceiverName(rs.getString("receiver_name"));
                    order.setReceiverPhone(rs.getString("receiver_phone"));
                    order.setReceiverAddress(rs.getString("receiver_address"));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Order> findOrderByIdForMerchant(long orderId, long merchantId) throws SQLException {
        String sql = "SELECT id, order_no, user_id, merchant_id, total_amount, order_status, pay_status, ship_status, " +
                "receiver_name, receiver_phone, receiver_address, created_at, updated_at " +
                "FROM orders WHERE id = ? AND merchant_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setLong(2, merchantId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrderWithUserId(rs);
                    order.setReceiverName(rs.getString("receiver_name"));
                    order.setReceiverPhone(rs.getString("receiver_phone"));
                    order.setReceiverAddress(rs.getString("receiver_address"));
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }

    public List<OrderItem> listOrderItems(long orderId) throws SQLException {
        String sql = "SELECT id, order_id, product_id, product_name, quantity, unit_price, subtotal " +
                "FROM order_items WHERE order_id = ? ORDER BY id";
        List<OrderItem> items = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getLong("id"));
                    item.setOrderId(rs.getLong("order_id"));
                    item.setProductId(rs.getLong("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public int updatePaymentStatus(long orderId, long userId, String orderStatus, String payStatus)
            throws SQLException {
        String sql = "UPDATE orders SET order_status = ?, pay_status = ?, updated_at = NOW() " +
                "WHERE id = ? AND user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderStatus);
            stmt.setString(2, payStatus);
            stmt.setLong(3, orderId);
            stmt.setLong(4, userId);
            return stmt.executeUpdate();
        }
    }

    public int updateOrderStatus(long orderId, String orderStatus) throws SQLException {
        String sql = "UPDATE orders SET order_status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderStatus);
            stmt.setLong(2, orderId);
            return stmt.executeUpdate();
        }
    }

    public List<SalesSummary> listDailySales(LocalDate startDate, LocalDate endDate) throws SQLException {
        // 销售统计只统计已支付订单
        StringBuilder sql = new StringBuilder(
                "SELECT DATE(o.created_at) AS sale_date, COUNT(*) AS order_count, " +
                        "SUM(o.total_amount) AS total_amount " +
                        "FROM orders o WHERE o.pay_status = 'PAID'"
        );
        List<Date> params = new ArrayList<>();
        appendDateRange(sql, params, startDate, endDate);
        sql.append(" GROUP BY DATE(o.created_at) ORDER BY sale_date DESC");

        List<SalesSummary> summaries = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            bindDates(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SalesSummary summary = new SalesSummary();
                    Date date = rs.getDate("sale_date");
                    summary.setSaleDate(date == null ? null : date.toLocalDate());
                    summary.setOrderCount(rs.getLong("order_count"));
                    BigDecimal total = rs.getBigDecimal("total_amount");
                    summary.setTotalAmount(total == null ? BigDecimal.ZERO : total);
                    summaries.add(summary);
                }
            }
        }
        return summaries;
    }

    public List<SalesSummary> listDailySalesByMerchant(long merchantId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT DATE(o.created_at) AS sale_date, COUNT(*) AS order_count, " +
                        "SUM(o.total_amount) AS total_amount " +
                        "FROM orders o WHERE o.pay_status = 'PAID' AND o.merchant_id = ?"
        );
        List<Date> params = new ArrayList<>();
        appendDateRange(sql, params, startDate, endDate);
        sql.append(" GROUP BY DATE(o.created_at) ORDER BY sale_date DESC");

        List<SalesSummary> summaries = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, merchantId);
            bindDatesWithOffset(stmt, params, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SalesSummary summary = new SalesSummary();
                    Date date = rs.getDate("sale_date");
                    summary.setSaleDate(date == null ? null : date.toLocalDate());
                    summary.setOrderCount(rs.getLong("order_count"));
                    BigDecimal total = rs.getBigDecimal("total_amount");
                    summary.setTotalAmount(total == null ? BigDecimal.ZERO : total);
                    summaries.add(summary);
                }
            }
        }
        return summaries;
    }

    public List<ProductSales> listProductSales(LocalDate startDate, LocalDate endDate) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT oi.product_id, oi.product_name, SUM(oi.quantity) AS total_quantity, " +
                        "SUM(oi.subtotal) AS total_amount " +
                        "FROM order_items oi " +
                        "JOIN orders o ON oi.order_id = o.id " +
                        "WHERE o.pay_status = 'PAID'"
        );
        List<Date> params = new ArrayList<>();
        appendDateRange(sql, params, startDate, endDate);
        sql.append(" GROUP BY oi.product_id, oi.product_name ORDER BY total_amount DESC");

        List<ProductSales> sales = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            bindDates(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProductSales row = new ProductSales();
                    row.setProductId(rs.getLong("product_id"));
                    row.setProductName(rs.getString("product_name"));
                    row.setTotalQuantity(rs.getLong("total_quantity"));
                    BigDecimal total = rs.getBigDecimal("total_amount");
                    row.setTotalAmount(total == null ? BigDecimal.ZERO : total);
                    sales.add(row);
                }
            }
        }
        return sales;
    }

    public List<ProductSales> listProductSalesByMerchant(long merchantId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT oi.product_id, oi.product_name, SUM(oi.quantity) AS total_quantity, " +
                        "SUM(oi.subtotal) AS total_amount " +
                        "FROM order_items oi " +
                        "JOIN orders o ON oi.order_id = o.id " +
                        "WHERE o.pay_status = 'PAID' AND o.merchant_id = ?"
        );
        List<Date> params = new ArrayList<>();
        appendDateRange(sql, params, startDate, endDate);
        sql.append(" GROUP BY oi.product_id, oi.product_name ORDER BY total_amount DESC");

        List<ProductSales> sales = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setLong(1, merchantId);
            bindDatesWithOffset(stmt, params, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProductSales row = new ProductSales();
                    row.setProductId(rs.getLong("product_id"));
                    row.setProductName(rs.getString("product_name"));
                    row.setTotalQuantity(rs.getLong("total_quantity"));
                    BigDecimal total = rs.getBigDecimal("total_amount");
                    row.setTotalAmount(total == null ? BigDecimal.ZERO : total);
                    sales.add(row);
                }
            }
        }
        return sales;
    }

    public int updateShipStatus(long orderId, String shipStatus, String orderStatus) throws SQLException {
        String sql = "UPDATE orders SET ship_status = ?, order_status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, shipStatus);
            stmt.setString(2, orderStatus);
            stmt.setLong(3, orderId);
            return stmt.executeUpdate();
        }
    }

    public int updateShipStatusForMerchant(long orderId, long merchantId, String shipStatus, String orderStatus)
            throws SQLException {
        String sql = "UPDATE orders SET ship_status = ?, order_status = ?, updated_at = NOW() " +
                "WHERE id = ? AND merchant_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, shipStatus);
            stmt.setString(2, orderStatus);
            stmt.setLong(3, orderId);
            stmt.setLong(4, merchantId);
            return stmt.executeUpdate();
        }
    }

    public int updateShipStatusForUser(long orderId, long userId, String shipStatus, String orderStatus)
            throws SQLException {
        String sql = "UPDATE orders SET ship_status = ?, order_status = ?, updated_at = NOW() " +
                "WHERE id = ? AND user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, shipStatus);
            stmt.setString(2, orderStatus);
            stmt.setLong(3, orderId);
            stmt.setLong(4, userId);
            return stmt.executeUpdate();
        }
    }

    public void updateEmailSentAt(long orderId) throws SQLException {
        String sql = "UPDATE orders SET email_sent_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.executeUpdate();
        }
    }

    public Optional<OrderMailInfo> findOrderMailInfo(long orderId) throws SQLException {
        String sql = "SELECT o.order_no, u.email FROM orders o " +
                "JOIN users u ON o.user_id = u.id WHERE o.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new OrderMailInfo(
                            rs.getString("order_no"),
                            rs.getString("email")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    private Order mapOrder(ResultSet rs, long userId) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setOrderNo(rs.getString("order_no"));
        order.setUserId(userId);
        long merchantId = rs.getLong("merchant_id");
        if (rs.wasNull()) {
            merchantId = 0;
        }
        order.setMerchantId(merchantId);
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setOrderStatus(rs.getString("order_status"));
        order.setPayStatus(rs.getString("pay_status"));
        order.setShipStatus(rs.getString("ship_status"));
        order.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        order.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        return order;
    }

    private Order mapOrderWithUserId(ResultSet rs) throws SQLException {
        return mapOrder(rs, rs.getLong("user_id"));
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    private void appendDateRange(StringBuilder sql, List<Date> params,
                                 LocalDate startDate, LocalDate endDate) {
        if (startDate != null) {
            sql.append(" AND o.created_at >= ?");
            params.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            // 结束日期用 < end+1，避免遗漏当天
            sql.append(" AND o.created_at < ?");
            params.add(Date.valueOf(endDate.plusDays(1)));
        }
    }

    private void bindDates(PreparedStatement stmt, List<Date> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setDate(i + 1, params.get(i));
        }
    }

    private void bindDatesWithOffset(PreparedStatement stmt, List<Date> params, int offset)
            throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setDate(i + 1 + offset, params.get(i));
        }
    }
}
