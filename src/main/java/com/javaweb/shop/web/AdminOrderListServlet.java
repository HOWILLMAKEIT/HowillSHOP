package com.javaweb.shop.web;

import com.javaweb.shop.dao.CartDao;
import com.javaweb.shop.dao.OrderDao;
import com.javaweb.shop.dao.ProductDao;
import com.javaweb.shop.infra.db.DataSourceFactory;
import com.javaweb.shop.model.Order;
import com.javaweb.shop.model.User;
import com.javaweb.shop.service.OrderService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// 商家订单列表
public class AdminOrderListServlet extends HttpServlet {
    private OrderService orderService;

    @Override
    public void init() {
        OrderDao orderDao = new OrderDao(DataSourceFactory.getDataSource());
        CartDao cartDao = new CartDao(DataSourceFactory.getDataSource());
        ProductDao productDao = new ProductDao(DataSourceFactory.getDataSource());
        this.orderService = new OrderService(DataSourceFactory.getDataSource(), cartDao, orderDao, productDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User merchant = getMerchantUser(request);
        if (merchant == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String status = request.getParameter("status");
        // status 为空时默认 ALL
        String selectedStatus = (status == null || status.isBlank()) ? "ALL" : status.trim().toUpperCase();

        try {
            List<Order> orders = orderService.listOrdersByMerchant(merchant.getId(), selectedStatus);
            request.setAttribute("orders", orders);
            request.setAttribute("selectedStatus", selectedStatus);

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

            request.getRequestDispatcher("/admin_orders.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("加载订单管理数据失败。", ex);
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
            if ("MERCHANT".equalsIgnoreCase(current.getRole())) {
                return current;
            }
        }
        return null;
    }
}
