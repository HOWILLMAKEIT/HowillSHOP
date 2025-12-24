package com.javaweb.shop.model;

// 发货邮件需要的精简信息
public class OrderMailInfo {
    private final String orderNo;
    private final String email;

    public OrderMailInfo(String orderNo, String email) {
        this.orderNo = orderNo;
        this.email = email;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getEmail() {
        return email;
    }
}
