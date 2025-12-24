package com.javaweb.shop.web;

import com.javaweb.shop.dao.CartDao;
import com.javaweb.shop.dao.OrderDao;
import com.javaweb.shop.dao.ProductDao;
import com.javaweb.shop.infra.db.DataSourceFactory;
import com.javaweb.shop.model.OrderDetail;
import com.javaweb.shop.model.User;
import com.javaweb.shop.service.MailService;
import com.javaweb.shop.service.OrderService;
import com.javaweb.shop.service.ShippingService;
import com.javaweb.shop.service.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

// 商家订单详情与状态更新
public class AdminOrderDetailServlet extends HttpServlet {
    private OrderService orderService;
    private ShippingService shippingService;

    @Override
    public void init() {
        OrderDao orderDao = new OrderDao(DataSourceFactory.getDataSource());
        CartDao cartDao = new CartDao(DataSourceFactory.getDataSource());
        ProductDao productDao = new ProductDao(DataSourceFactory.getDataSource());
        this.orderService = new OrderService(DataSourceFactory.getDataSource(), cartDao, orderDao, productDao);
        try {
            this.shippingService = new ShippingService(orderDao, new MailService());
        } catch (ValidationException ex) {
            throw new IllegalStateException("邮件配置有误。", ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User merchant = getMerchantUser(request);
        if (merchant == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        long orderId = parseLong(request.getParameter("orderId"));
        try {
            OrderDetail detail = orderService.getOrderDetailForMerchant(merchant.getId(), orderId);
            request.setAttribute("orderDetail", detail);

            Object message = request.getSession().getAttribute("adminOrderMessage");
            if (message != null) {
                request.setAttribute("message", message.toString());
                request.getSession().removeAttribute("adminOrderMessage");
            }
            Object error = request.getSession().getAttribute("adminOrderError");
            if (error != null) {
                request.setAttribute("error", error.toString());
                request.getSession().removeAttribute("adminOrderError");
            }

            request.getRequestDispatcher("/admin_order_detail.jsp").forward(request, response);
        } catch (ValidationException ex) {
            request.getSession().setAttribute("adminOrderError", ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/orders");
        } catch (SQLException ex) {
            throw new ServletException("加载订单详情失败。", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        User merchant = getMerchantUser(request);
        if (merchant == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        long orderId = parseLong(request.getParameter("orderId"));
        try {
            String action = request.getParameter("action");
            if ("ship".equalsIgnoreCase(action)) {
                shippingService.shipOrder(orderId, merchant.getId());
                request.getSession().setAttribute("adminOrderMessage", "订单已标记为已发货。");
            } else {
                request.getSession().setAttribute("adminOrderError", "不支持的操作。");
            }
            response.sendRedirect(request.getContextPath() + "/admin/orders/detail?orderId=" + orderId);
        } catch (ValidationException ex) {
            request.getSession().setAttribute("adminOrderError", ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/orders/detail?orderId=" + orderId);
        } catch (SQLException ex) {
            throw new ServletException("更新订单状态失败。", ex);
        }
    }

    private User getMerchantUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("currentUser");
        if (user instanceof User) {
            User current = (User) user;
            // 简单角色校验，拦非商家访问
            if ("MERCHANT".equalsIgnoreCase(current.getRole())) {
                return current;
            }
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
