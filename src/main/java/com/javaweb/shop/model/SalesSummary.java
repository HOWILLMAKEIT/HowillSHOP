package com.javaweb.shop.model;

import java.math.BigDecimal;
import java.time.LocalDate;

// 按天汇总的销售数据
public class SalesSummary {
    private LocalDate saleDate;
    private long orderCount;
    private BigDecimal totalAmount;

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
