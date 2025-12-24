package com.javaweb.shop.service;

import com.javaweb.shop.dao.CartDao;
import com.javaweb.shop.dao.ProductDao;
import com.javaweb.shop.model.CartItem;
import com.javaweb.shop.model.CartSummary;
import com.javaweb.shop.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// 购物车业务处理
public class CartService {
    private final CartDao cartDao;
    private final ProductDao productDao;

    public CartService(CartDao cartDao, ProductDao productDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
    }

    public CartSummary loadCart(long userId) throws SQLException {
        Long cartId = cartDao.findActiveCartId(userId);
        if (cartId == null) {
            return new CartSummary(Collections.emptyList(), BigDecimal.ZERO);
        }
        List<CartItem> items = cartDao.listCartItems(cartId);
        return new CartSummary(items, calculateTotal(items));
    }

    public void addItem(long userId, long productId, int quantity)
            throws ValidationException, SQLException {
        if (quantity <= 0) {
            throw new ValidationException("商品数量必须大于0。");
        }
        Product product = requireProduct(productId);
        long cartId = cartDao.getOrCreateActiveCartId(userId);

        Optional<CartDao.CartItemData> existing = cartDao.findCartItem(cartId, productId);
        int newQuantity = quantity;
        BigDecimal unitPrice = product.getPrice();
        if (existing.isPresent()) {
            // 同款商品合并数量，避免重复行
            newQuantity = existing.get().getQuantity() + quantity;
            unitPrice = existing.get().getUnitPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(newQuantity));
            cartDao.updateCartItem(cartId, productId, newQuantity, subtotal);
        } else {
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(newQuantity));
            cartDao.insertCartItem(cartId, productId, newQuantity, unitPrice, subtotal);
        }
    }

    public void updateItem(long userId, long productId, int quantity)
            throws ValidationException, SQLException {
        long cartId = cartDao.getOrCreateActiveCartId(userId);
        if (quantity <= 0) {
            // 数量<=0 直接当删除处理
            cartDao.deleteCartItem(cartId, productId);
            return;
        }

        Optional<CartDao.CartItemData> existing = cartDao.findCartItem(cartId, productId);
        BigDecimal unitPrice;
        if (existing.isPresent()) {
            unitPrice = existing.get().getUnitPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            cartDao.updateCartItem(cartId, productId, quantity, subtotal);
        } else {
            Product product = requireProduct(productId);
            unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            cartDao.insertCartItem(cartId, productId, quantity, unitPrice, subtotal);
        }
    }

    public void removeItem(long userId, long productId) throws SQLException {
        Long cartId = cartDao.findActiveCartId(userId);
        if (cartId == null) {
            return;
        }
        cartDao.deleteCartItem(cartId, productId);
    }

    private Product requireProduct(long productId) throws ValidationException, SQLException {
        if (productId <= 0) {
            throw new ValidationException("商品参数不正确。");
        }
        Optional<Product> product = productDao.findById(productId);
        if (product.isEmpty()) {
            throw new ValidationException("商品不存在。");
        }
        return product.get();
    }

    private BigDecimal calculateTotal(List<CartItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            if (item.getSubtotal() != null) {
                total = total.add(item.getSubtotal());
            }
        }
        return total;
    }
}
