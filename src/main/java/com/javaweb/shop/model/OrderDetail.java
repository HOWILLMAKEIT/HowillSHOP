package com.javaweb.shop.model;

import java.util.List;

// 订单详情视图对象（主表 + 明细）
public class OrderDetail {
    private final Order order;
    private final List<OrderItem> items;

    public OrderDetail(Order order, List<OrderItem> items) {
        this.order = order;
        this.items = items;
    }

    public Order getOrder() {
        return order;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
