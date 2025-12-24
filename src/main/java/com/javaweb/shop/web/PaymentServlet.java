package com.javaweb.shop.web;

import com.javaweb.shop.dao.CartDao;
import com.javaweb.shop.dao.OrderDao;
import com.javaweb.shop.dao.ProductDao;
import com.javaweb.shop.infra.db.DataSourceFactory;
import com.javaweb.shop.model.OrderDetail;
import com.javaweb.shop.model.User;
import com.javaweb.shop.service.OrderService;
import com.javaweb.shop.service.PaymentService;
import com.javaweb.shop.service.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

// 支付入口
public class PaymentServlet extends HttpServlet {
    private OrderService orderService;
    private PaymentService paymentService;

    @Override
    public void init() {
        OrderDao orderDao = new OrderDao(DataSourceFactory.getDataSource());
        CartDao cartDao = new CartDao(DataSourceFactory.getDataSource());
        ProductDao productDao = new ProductDao(DataSourceFactory.getDataSource());
        this.orderService = new OrderService(DataSourceFactory.getDataSource(), cartDao, orderDao, productDao);
        this.paymentService = new PaymentService(orderDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        long orderId = parseLong(request.getParameter("orderId"));
        try {
            OrderDetail detail = orderService.getOrderDetail(user.getId(), orderId);
            request.setAttribute("orderDetail", detail);

            Object error = request.getSession().getAttribute("paymentError");
            if (error != null) {
                request.setAttribute("error", error.toString());
                request.getSession().removeAttribute("paymentError");
            }

            request.getRequestDispatcher("/payment.jsp").forward(request, response);
        } catch (ValidationException ex) {
            request.getSession().setAttribute("orderError", ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/orders");
        } catch (SQLException ex) {
            throw new ServletException("加载支付信息失败。", ex);
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

        long orderId = parseLong(request.getParameter("orderId"));
        String result = request.getParameter("result");
        // 前端按钮传 success/fail，统一转成布尔
        boolean success = "success".equalsIgnoreCase(result);

        try {
            paymentService.simulatePayment(user.getId(), orderId, success);
            response.sendRedirect(request.getContextPath() + "/orders/detail?orderId=" + orderId);
        } catch (ValidationException ex) {
            request.getSession().setAttribute("paymentError", ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/payment?orderId=" + orderId);
        } catch (SQLException ex) {
            throw new ServletException("更新支付状态失败。", ex);
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

    private long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
