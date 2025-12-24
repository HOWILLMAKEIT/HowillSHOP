package com.javaweb.shop.model;

// 发货邮件需要的精简信息
public class OrderMailInfo {
    private final String orderNo;
    private final String email;
    private final String merchantName;

    public OrderMailInfo(String orderNo, String email, String merchantName) {
        this.orderNo = orderNo;
        this.email = email;
        this.merchantName = merchantName;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getEmail() {
        return email;
    }

    public String getMerchantName() {
        return merchantName;
    }
}
