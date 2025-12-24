package com.javaweb.shop.model;

import java.math.BigDecimal;

// 购物车里的单行商品
public class CartItem {
    // 商品信息（用于展示）
    private Product product;
    // 购买数量
    private int quantity;
    // 下单时单价，避免后续改价影响
    private BigDecimal unitPrice;
    // 小计 = 数量 * 单价
    private BigDecimal subtotal;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
