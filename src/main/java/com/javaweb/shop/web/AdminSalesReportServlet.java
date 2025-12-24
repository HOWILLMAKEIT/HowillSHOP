package com.javaweb.shop.web;

import com.javaweb.shop.dao.CartDao;
import com.javaweb.shop.dao.OrderDao;
import com.javaweb.shop.dao.ProductDao;
import com.javaweb.shop.infra.db.DataSourceFactory;
import com.javaweb.shop.model.ProductSales;
import com.javaweb.shop.model.SalesSummary;
import com.javaweb.shop.model.User;
import com.javaweb.shop.service.OrderService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// 商家销售统计报表
public class AdminSalesReportServlet extends HttpServlet {
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

        LocalDate startDate = parseDate(request.getParameter("startDate"));
        LocalDate endDate = parseDate(request.getParameter("endDate"));
        // 前端回显筛选条件
        if (startDate != null) {
            request.setAttribute("startDate", startDate.toString());
        }
        if (endDate != null) {
            request.setAttribute("endDate", endDate.toString());
        }

        try {
            List<SalesSummary> dailySales = orderService.listDailySalesForMerchant(
                    merchant.getId(), startDate, endDate
            );
            List<ProductSales> productSales = orderService.listProductSalesForMerchant(
                    merchant.getId(), startDate, endDate
            );
            request.setAttribute("dailySales", dailySales);
            request.setAttribute("productSales", productSales);
            request.getRequestDispatcher("/admin_sales.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("加载销售统计失败。", ex);
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

    private LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }
}
