package com.javaweb.shop.web;

import com.javaweb.shop.dao.CartDao;
import com.javaweb.shop.dao.OrderDao;
import com.javaweb.shop.dao.ProductDao;
import com.javaweb.shop.infra.db.DataSourceFactory;
import com.javaweb.shop.model.CartSummary;
import com.javaweb.shop.model.User;
import com.javaweb.shop.service.CartService;
import com.javaweb.shop.service.OrderService;
import com.javaweb.shop.service.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

// 结算入口
public class CheckoutServlet extends HttpServlet {
    private CartService cartService;
    private OrderService orderService;

    @Override
    public void init() {
        CartDao cartDao = new CartDao(DataSourceFactory.getDataSource());
        ProductDao productDao = new ProductDao(DataSourceFactory.getDataSource());
        OrderDao orderDao = new OrderDao(DataSourceFactory.getDataSource());
        this.cartService = new CartService(cartDao, productDao);
        this.orderService = new OrderService(DataSourceFactory.getDataSource(), cartDao, orderDao, productDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            CartSummary summary = cartService.loadCart(user.getId());
            if (summary.getItems().isEmpty()) {
                // 空购物车不允许进入结算
                request.getSession().setAttribute("cartError", "购物车为空。");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }
            request.setAttribute("cartSummary", summary);

            Object error = request.getSession().getAttribute("checkoutError");
            if (error != null) {
                request.setAttribute("error", error.toString());
                request.getSession().removeAttribute("checkoutError");
            }

            request.getRequestDispatcher("/checkout.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("加载结算数据失败。", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String receiverName = request.getParameter("receiverName");
        String receiverPhone = request.getParameter("receiverPhone");
        String receiverAddress = request.getParameter("receiverAddress");

        try {
            List<com.javaweb.shop.model.Order> orders = orderService.createOrders(
                    user.getId(), receiverName, receiverPhone, receiverAddress
            );
            request.getSession().setAttribute("cartSummary",
                    new CartSummary(Collections.emptyList(), BigDecimal.ZERO));
            if (orders.size() == 1) {
                response.sendRedirect(request.getContextPath() + "/payment?orderId=" + orders.get(0).getId());
            } else {
                request.getSession().setAttribute("orderMessage",
                        "已拆分为 " + orders.size() + " 个订单，请分别支付。");
                response.sendRedirect(request.getContextPath() + "/orders");
            }
        } catch (ValidationException ex) {
            request.getSession().setAttribute("checkoutError", ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/checkout");
        } catch (SQLException ex) {
            throw new ServletException("创建订单失败。", ex);
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("currentUser");
        if (user instanceof User) {
            return (User) user;
        }
        return null;
    }
}
