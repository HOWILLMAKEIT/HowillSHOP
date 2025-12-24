package com.javaweb.shop.service;

import com.javaweb.shop.dao.OrderDao;

import java.sql.SQLException;

// 支付状态模拟
public class PaymentService {
    private final OrderDao orderDao;

    public PaymentService(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    public void simulatePayment(long userId, long orderId, boolean success)
            throws ValidationException, SQLException {
        // 这里只做状态模拟，不接真实支付网关
        String orderStatus = success ? "PAID" : "CREATED";
        String payStatus = success ? "PAID" : "UNPAID";
        int updated = orderDao.updatePaymentStatus(orderId, userId, orderStatus, payStatus);
        if (updated == 0) {
            throw new ValidationException("订单不存在。");
        }
    }
}
