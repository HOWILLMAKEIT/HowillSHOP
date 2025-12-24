package com.javaweb.shop.service;

import com.javaweb.shop.dao.CartDao;
import com.javaweb.shop.dao.OrderDao;
import com.javaweb.shop.dao.ProductDao;
import com.javaweb.shop.model.CartItem;
import com.javaweb.shop.model.Order;
import com.javaweb.shop.model.OrderDetail;
import com.javaweb.shop.model.OrderItem;
import com.javaweb.shop.model.ProductSales;
import com.javaweb.shop.model.SalesSummary;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// 订单业务处理
public class OrderService {
    private final DataSource dataSource;
    private final CartDao cartDao;
    private final OrderDao orderDao;
    private final ProductDao productDao;

    public OrderService(DataSource dataSource, CartDao cartDao, OrderDao orderDao, ProductDao productDao) {
        this.dataSource = dataSource;
        this.cartDao = cartDao;
        this.orderDao = orderDao;
        this.productDao = productDao;
    }

    public Order createOrder(long userId, String receiverName, String receiverPhone,
                             String receiverAddress) throws ValidationException, SQLException {
        List<Order> orders = createOrders(userId, receiverName, receiverPhone, receiverAddress);
        if (orders.isEmpty()) {
            throw new ValidationException("创建订单失败。");
        }
        return orders.get(0);
    }

    public List<Order> createOrders(long userId, String receiverName, String receiverPhone,
                                    String receiverAddress) throws ValidationException, SQLException {
        if (isBlank(receiverName) || isBlank(receiverPhone) || isBlank(receiverAddress)) {
            throw new ValidationException("收货信息不能为空。");
        }

        Long cartId = cartDao.findActiveCartId(userId);
        if (cartId == null) {
            throw new ValidationException("购物车为空。");
        }

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<CartItem> items = cartDao.listCartItems(conn, cartId);
                if (items.isEmpty()) {
                    throw new ValidationException("购物车为空。");
                }
                // 下单时扣减库存，库存不足直接回滚
                for (CartItem item : items) {
                    int updated = productDao.decreaseStock(conn, item.getProduct().getId(), item.getQuantity());
                    if (updated == 0) {
                        throw new ValidationException("库存不足，商品：" + item.getProduct().getName());
                    }
                }

                Map<Long, List<CartItem>> itemsByMerchant = new LinkedHashMap<>();
                for (CartItem item : items) {
                    long merchantId = item.getProduct().getMerchantId();
                    if (merchantId <= 0) {
                        throw new ValidationException("商品缺少商家信息：" + item.getProduct().getName());
                    }
                    itemsByMerchant.computeIfAbsent(merchantId, key -> new ArrayList<>()).add(item);
                }

                List<Order> orders = new ArrayList<>();
                for (Map.Entry<Long, List<CartItem>> entry : itemsByMerchant.entrySet()) {
                    long merchantId = entry.getKey();
                    List<CartItem> merchantItems = entry.getValue();

                    Order order = new Order();
                    order.setOrderNo(generateOrderNo());
                    order.setUserId(userId);
                    order.setMerchantId(merchantId);
                    order.setTotalAmount(calculateTotal(merchantItems));
                    order.setOrderStatus("CREATED");
                    order.setPayStatus("UNPAID");
                    order.setShipStatus("PENDING");
                    order.setReceiverName(receiverName.trim());
                    order.setReceiverPhone(receiverPhone.trim());
                    order.setReceiverAddress(receiverAddress.trim());

                    long orderId = orderDao.insertOrder(conn, order);
                    order.setId(orderId);
                    orderDao.insertOrderItems(conn, orderId, merchantItems);
                    orders.add(order);
                }
                cartDao.clearCartItems(conn, cartId);
                cartDao.updateCartStatus(conn, cartId, 0);

                conn.commit();
                return orders;
            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof ValidationException) {
                    throw (ValidationException) ex;
                }
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("创建订单失败。", ex);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Order> listOrders(long userId) throws SQLException {
        return orderDao.listOrdersByUser(userId);
    }

    public List<Order> listOrdersByMerchant(long merchantId, String status) throws SQLException {
        if (isBlank(status) || "ALL".equalsIgnoreCase(status)) {
            return orderDao.listOrdersByMerchant(merchantId);
        }
        String normalized = status.trim().toUpperCase();
        if ("PAID".equals(normalized) || "UNPAID".equals(normalized)) {
            return orderDao.listOrdersByMerchantAndPayStatus(merchantId, normalized);
        }
        return orderDao.listOrdersByMerchantAndOrderStatus(merchantId, normalized);
    }

    public List<SalesSummary> listDailySales(LocalDate startDate, LocalDate endDate) throws SQLException {
        return orderDao.listDailySales(startDate, endDate);
    }

    public List<ProductSales> listProductSales(LocalDate startDate, LocalDate endDate) throws SQLException {
        return orderDao.listProductSales(startDate, endDate);
    }

    public List<SalesSummary> listDailySalesForMerchant(long merchantId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        return orderDao.listDailySalesByMerchant(merchantId, startDate, endDate);
    }

    public List<ProductSales> listProductSalesForMerchant(long merchantId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        return orderDao.listProductSalesByMerchant(merchantId, startDate, endDate);
    }

    public List<Order> listAllOrders(String status) throws SQLException {
        if (isBlank(status) || "ALL".equalsIgnoreCase(status)) {
            return orderDao.listAllOrders();
        }
        String normalized = status.trim().toUpperCase();
        if ("PAID".equals(normalized) || "UNPAID".equals(normalized)) {
            return orderDao.listOrdersByPayStatus(normalized);
        }
        return orderDao.listOrdersByOrderStatus(normalized);
    }

    public OrderDetail getOrderDetail(long userId, long orderId) throws SQLException, ValidationException {
        Optional<Order> order = orderDao.findOrderForUser(orderId, userId);
        if (order.isEmpty()) {
            throw new ValidationException("订单不存在。");
        }
        List<OrderItem> items = orderDao.listOrderItems(orderId);
        return new OrderDetail(order.get(), items);
    }

    public OrderDetail getOrderDetailForAdmin(long orderId) throws SQLException, ValidationException {
        Optional<Order> order = orderDao.findOrderById(orderId);
        if (order.isEmpty()) {
            throw new ValidationException("订单不存在。");
        }
        List<OrderItem> items = orderDao.listOrderItems(orderId);
        return new OrderDetail(order.get(), items);
    }

    public OrderDetail getOrderDetailForMerchant(long merchantId, long orderId)
            throws SQLException, ValidationException {
        Optional<Order> order = orderDao.findOrderByIdForMerchant(orderId, merchantId);
        if (order.isEmpty()) {
            throw new ValidationException("订单不存在。");
        }
        List<OrderItem> items = orderDao.listOrderItems(orderId);
        return new OrderDetail(order.get(), items);
    }

    public void updateOrderStatus(long orderId, String status)
            throws ValidationException, SQLException {
        String normalized = status == null ? "" : status.trim().toUpperCase();
        if (!isAllowedStatus(normalized)) {
            throw new ValidationException("订单状态不合法。");
        }
        int updated = orderDao.updateOrderStatus(orderId, normalized);
        if (updated == 0) {
            throw new ValidationException("订单不存在。");
        }
    }

    public void confirmReceipt(long userId, long orderId) throws ValidationException, SQLException {
        Optional<Order> order = orderDao.findOrderForUser(orderId, userId);
        if (order.isEmpty()) {
            throw new ValidationException("订单不存在。");
        }
        Order current = order.get();
        if (!"PAID".equalsIgnoreCase(current.getPayStatus())) {
            throw new ValidationException("订单未支付，无法确认收货。");
        }
        if (!"SHIPPED".equalsIgnoreCase(current.getShipStatus())) {
            throw new ValidationException("订单尚未发货。");
        }
        if ("COMPLETED".equalsIgnoreCase(current.getOrderStatus())) {
            return;
        }
        int updated = orderDao.updateShipStatusForUser(orderId, userId, "DELIVERED", "COMPLETED");
        if (updated == 0) {
            throw new ValidationException("订单不存在。");
        }
    }

    private String generateOrderNo() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private BigDecimal calculateTotal(List<CartItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isAllowedStatus(String status) {
        return "CREATED".equals(status)
                || "PAID".equals(status)
                || "SHIPPED".equals(status)
                || "COMPLETED".equals(status);
    }
}
