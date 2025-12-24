package com.javaweb.shop.web;

import com.javaweb.shop.dao.CategoryDao;
import com.javaweb.shop.dao.ProductDao;
import com.javaweb.shop.infra.db.DataSourceFactory;
import com.javaweb.shop.model.Category;
import com.javaweb.shop.model.Product;
import com.javaweb.shop.service.OssService;
import com.javaweb.shop.service.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// 商品列表与详情入口
public class ProductServlet extends HttpServlet {
    private ProductDao productDao;
    private CategoryDao categoryDao;
    private static final int PAGE_SIZE = 12;

    @Override
    public void init() {
        this.productDao = new ProductDao(DataSourceFactory.getDataSource());
        this.categoryDao = new CategoryDao(DataSourceFactory.getDataSource());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            // 列表与详情共用一个入口，按路径分流
            if ("/products/detail".equals(path)) {
                showDetail(request, response);
            } else {
                showList(request, response);
            }
        } catch (SQLException | ValidationException ex) {
            throw new ServletException("加载商品数据失败。", ex);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        Long categoryId = parseLong(request.getParameter("categoryId"));
        String keyword = normalizeKeyword(request.getParameter("keyword"));
        int page = parseInt(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        int total = productDao.countProducts(categoryId, keyword);
        int totalPages = total == 0 ? 0 : (int) Math.ceil(total / (double) PAGE_SIZE);
        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }
        int offset = (page - 1) * PAGE_SIZE;

        List<Category> categories = categoryDao.listActiveCategories();
        List<Product> products = productDao.listProducts(categoryId, keyword, offset, PAGE_SIZE);
        signProductImages(products);
        request.setAttribute("categories", categories);
        request.setAttribute("products", products);
        request.setAttribute("selectedCategoryId", categoryId);
        request.setAttribute("keyword", keyword);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.getRequestDispatcher("/product_list.jsp").forward(request, response);
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ValidationException, ServletException, IOException {
        long productId = requireLong(request.getParameter("productId"));
        Optional<Product> product = productDao.findById(productId);
        if (product.isEmpty()) {
            throw new ValidationException("商品不存在。");
        }
        signProductImage(product.get());
        request.setAttribute("product", product.get());
        request.getRequestDispatcher("/product_detail.jsp").forward(request, response);
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
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

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private long requireLong(String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("商品参数不正确。");
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("商品参数不正确。");
        }
    }

    private void signProductImages(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        OssService ossService = null;
        for (Product product : products) {
            if (product == null) {
                continue;
            }
            String imageUrl = product.getImageUrl();
            if (imageUrl == null || imageUrl.isBlank()) {
                continue;
            }
            try {
                if (ossService == null) {
                    ossService = new OssService();
                }
                String signed = ossService.signUrl(imageUrl);
                if (signed != null && !signed.isBlank()) {
                    product.setImageUrl(signed);
                }
            } catch (ValidationException ex) {
                break;
            }
        }
    }

    private void signProductImage(Product product) {
        if (product == null) {
            return;
        }
        String imageUrl = product.getImageUrl();
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        try {
            OssService ossService = new OssService();
            String signed = ossService.signUrl(imageUrl);
            if (signed != null && !signed.isBlank()) {
                product.setImageUrl(signed);
            }
        } catch (ValidationException ex) {
            // OSS 配置缺失时保留原地址
        }
    }
}
