package com.javaweb.shop.model;

import java.math.BigDecimal;
import java.util.List;

// 购物车汇总：条目列表 + 总价
public class CartSummary {
    private final List<CartItem> items;
    private final BigDecimal total;

    public CartSummary(List<CartItem> items, BigDecimal total) {
        this.items = items;
        this.total = total;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
