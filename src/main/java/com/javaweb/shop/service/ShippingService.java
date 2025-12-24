package com.javaweb.shop.service;

import com.javaweb.shop.dao.OrderDao;
import com.javaweb.shop.model.OrderMailInfo;

import java.sql.SQLException;
import java.util.Optional;

// 发货状态更新与邮件通知
public class ShippingService {
    private final OrderDao orderDao;
    private final MailService mailService;

    public ShippingService(OrderDao orderDao, MailService mailService) {
        this.orderDao = orderDao;
        this.mailService = mailService;
    }

    public void shipOrder(long orderId, long merchantId) throws ValidationException, SQLException {
        Optional<OrderMailInfo> mailInfo;
        // 先校验订单支付与发货状态
        Optional<com.javaweb.shop.model.Order> order = orderDao.findOrderByIdForMerchant(orderId, merchantId);
        if (order.isEmpty()) {
            throw new ValidationException("订单不存在。");
        }
        if (!"PAID".equalsIgnoreCase(order.get().getPayStatus())) {
            throw new ValidationException("订单未支付，无法发货。");
        }
        if (!"PENDING".equalsIgnoreCase(order.get().getShipStatus())) {
            throw new ValidationException("订单已发货或已完成。");
        }

        // 先更新发货状态，再发邮件通知
        int updated = orderDao.updateShipStatusForMerchant(orderId, merchantId, "SHIPPED", "SHIPPED");
        if (updated == 0) {
            throw new ValidationException("订单不存在。");
        }

        mailInfo = orderDao.findOrderMailInfo(orderId);
        if (mailInfo.isEmpty()) {
            throw new ValidationException("订单邮箱信息不存在。");
        }

        mailService.sendShipmentEmail(mailInfo.get().getEmail(), mailInfo.get().getOrderNo());
        orderDao.updateEmailSentAt(orderId);
    }
}
