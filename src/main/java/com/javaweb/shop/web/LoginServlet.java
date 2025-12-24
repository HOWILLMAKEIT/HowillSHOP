package com.javaweb.shop.web;

import com.javaweb.shop.dao.CartDao;
import com.javaweb.shop.dao.ProductDao;
import com.javaweb.shop.dao.UserDao;
import com.javaweb.shop.infra.db.DataSourceFactory;
import com.javaweb.shop.model.CartSummary;
import com.javaweb.shop.model.User;
import com.javaweb.shop.service.CartService;
import com.javaweb.shop.service.UserService;
import com.javaweb.shop.service.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

// 登录入口
public class LoginServlet extends HttpServlet {
    private UserService userService;
    private CartService cartService;

    @Override
    public void init() {
        UserDao userDao = new UserDao(DataSourceFactory.getDataSource());
        this.userService = new UserService(userDao);
        this.cartService = new CartService(
                new CartDao(DataSourceFactory.getDataSource()),
                new ProductDao(DataSourceFactory.getDataSource())
        );
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String registered = request.getParameter("registered");
        String loggedOut = request.getParameter("loggedOut");
        if ("1".equals(registered)) {
            request.setAttribute("message", "注册成功，请登录。");
        } else if ("1".equals(loggedOut)) {
            request.setAttribute("message", "您已退出登录。");
        }
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String loginType = request.getParameter("loginType");
        boolean merchantLogin = "merchant".equalsIgnoreCase(loginType);

        try {
            User user = userService.authenticate(username, password);
            if (merchantLogin && !"MERCHANT".equalsIgnoreCase(user.getRole())) {
                throw new ValidationException("该账号不是商家，请选择用户登录。");
            }
            if (!merchantLogin && "MERCHANT".equalsIgnoreCase(user.getRole())) {
                throw new ValidationException("该账号为商家，请选择商家登录。");
            }
            request.getSession(true).setAttribute("currentUser", user);
            // 登录后加载购物车摘要，页面能直接展示数量/总价
            if (!merchantLogin) {
                CartSummary summary = cartService.loadCart(user.getId());
                request.getSession().setAttribute("cartSummary", summary);
            }
            response.sendRedirect(request.getContextPath() + "/products");
        } catch (ValidationException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("登录时数据库出错。", ex);
        }
    }
}
