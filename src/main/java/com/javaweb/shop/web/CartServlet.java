package com.javaweb.shop.web;

import com.javaweb.shop.dao.CartDao;
import com.javaweb.shop.dao.ProductDao;
import com.javaweb.shop.infra.db.DataSourceFactory;
import com.javaweb.shop.model.CartSummary;
import com.javaweb.shop.model.User;
import com.javaweb.shop.service.CartService;
import com.javaweb.shop.service.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

// 购物车增删改入口
public class CartServlet extends HttpServlet {
    private CartService cartService;

    @Override
    public void init() {
        CartDao cartDao = new CartDao(DataSourceFactory.getDataSource());
        ProductDao productDao = new ProductDao(DataSourceFactory.getDataSource());
        this.cartService = new CartService(cartDao, productDao);
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
            request.setAttribute("cartSummary", summary);
            request.getSession().setAttribute("cartSummary", summary);

            Object error = request.getSession().getAttribute("cartError");
            if (error != null) {
                request.setAttribute("error", error.toString());
                request.getSession().removeAttribute("cartError");
            }

            request.getRequestDispatcher("/cart.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("加载购物车失败。", ex);
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

        String path = request.getServletPath();
        long productId = parseLong(request.getParameter("productId"));
        int quantity = parseInt(request.getParameter("quantity"), 1);

        try {
            // 根据路径区分增删改
            if ("/cart/add".equals(path)) {
                cartService.addItem(user.getId(), productId, quantity);
            } else if ("/cart/update".equals(path)) {
                cartService.updateItem(user.getId(), productId, quantity);
            } else if ("/cart/remove".equals(path)) {
                cartService.removeItem(user.getId(), productId);
            }
        } catch (ValidationException ex) {
            request.getSession().setAttribute("cartError", ex.getMessage());
        } catch (SQLException ex) {
            throw new ServletException("更新购物车失败。", ex);
        }

        response.sendRedirect(request.getContextPath() + "/cart");
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

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
