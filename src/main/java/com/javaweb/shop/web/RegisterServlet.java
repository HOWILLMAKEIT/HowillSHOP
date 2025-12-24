package com.javaweb.shop.web;

import com.javaweb.shop.dao.UserDao;
import com.javaweb.shop.infra.db.DataSourceFactory;
import com.javaweb.shop.service.UserService;
import com.javaweb.shop.service.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

// 注册入口
public class RegisterServlet extends HttpServlet {
    private UserService userService;

    @Override
    public void init() {
        UserDao userDao = new UserDao(DataSourceFactory.getDataSource());
        this.userService = new UserService(userDao);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirm_password");
        String registerType = request.getParameter("registerType");

        if (password == null || !password.equals(confirmPassword)) {
            // 两次密码不一致直接提示
            request.setAttribute("error", "两次输入的密码不一致。");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        try {
            String role = "merchant".equalsIgnoreCase(registerType) ? "MERCHANT" : "CUSTOMER";
            userService.register(username, email, phone, password, role);
            response.sendRedirect(request.getContextPath() + "/auth/login?registered=1");
        } catch (ValidationException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("注册时数据库出错。", ex);
        }
    }
}
