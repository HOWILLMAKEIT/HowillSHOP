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

// 用户订单列表
public class OrderListServlet extends HttpServlet {
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
        User user = getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        try {
            // 只查询当前用户的订单
            List<Order> orders = orderService.listOrders(user.getId());
            request.setAttribute("orders", orders);

            Object message = request.getSession().getAttribute("orderMessage");
            if (message != null) {
                request.setAttribute("message", message.toString());
                request.getSession().removeAttribute("orderMessage");
            }
            Object error = request.getSession().getAttribute("orderError");
            if (error != null) {
                request.setAttribute("error", error.toString());
                request.getSession().removeAttribute("orderError");
            }

            request.getRequestDispatcher("/orders.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("加载订单列表失败。", ex);
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
