package com.javaweb.shop.web;

import com.javaweb.shop.dao.CategoryDao;
import com.javaweb.shop.dao.ProductDao;
import com.javaweb.shop.infra.db.DataSourceFactory;
import com.javaweb.shop.model.Category;
import com.javaweb.shop.model.Product;
import com.javaweb.shop.model.User;
import com.javaweb.shop.service.OssService;
import com.javaweb.shop.service.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// 商家商品管理入口
@MultipartConfig
public class AdminProductServlet extends HttpServlet {
    private ProductDao productDao;
    private CategoryDao categoryDao;

    @Override
    public void init() {
        this.productDao = new ProductDao(DataSourceFactory.getDataSource());
        this.categoryDao = new CategoryDao(DataSourceFactory.getDataSource());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User merchant = getMerchantUser(request);
        if (merchant == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String editId = request.getParameter("editId");
        try {
            List<Product> products = productDao.listProductsByMerchant(merchant.getId());
            List<Category> categories = categoryDao.listActiveCategories();
            request.setAttribute("products", products);
            request.setAttribute("categories", categories);

            if (editId != null && !editId.isBlank()) {
                // 带 editId 时加载编辑数据
                try {
                    Optional<Product> product = productDao.findByIdForMerchant(parseLong(editId), merchant.getId());
                    product.ifPresent(value -> request.setAttribute("editProduct", value));
                } catch (ValidationException ex) {
                    request.setAttribute("error", ex.getMessage());
                }
            }

            Object message = request.getSession().getAttribute("adminProductMessage");
            if (message != null) {
                request.setAttribute("message", message.toString());
                request.getSession().removeAttribute("adminProductMessage");
            }
            Object error = request.getSession().getAttribute("adminProductError");
            if (error != null) {
                request.setAttribute("error", error.toString());
                request.getSession().removeAttribute("adminProductError");
            }

            request.getRequestDispatcher("/admin_product_list.jsp").forward(request, response);
        } catch (SQLException ex) {
            throw new ServletException("加载商品列表失败。", ex);
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

        String action = request.getParameter("action");
        try {
            if ("delete".equalsIgnoreCase(action)) {
                long productId = parseLong(request.getParameter("productId"));
                Optional<Product> existing = productDao.findByIdForMerchant(productId, merchant.getId());
                int deleted = productDao.deleteProductForMerchant(productId, merchant.getId());
                if (deleted == 0) {
                    request.getSession().setAttribute("adminProductError", "没有权限删除该商品。");
                } else {
                    if (existing.isPresent()) {
                        deleteImageIfPresent(existing.get().getImageUrl());
                    }
                    request.getSession().setAttribute("adminProductMessage", "商品已删除。");
                }
            } else {
                // 非 delete 走新增/更新
                Product product = buildProductFromRequest(request);
                String imageUrl = uploadImageIfPresent(request);
                if (imageUrl != null) {
                    product.setImageUrl(imageUrl);
                } else if (product.getId() > 0) {
                    String currentImage = request.getParameter("currentImageUrl");
                    if (currentImage != null && !currentImage.isBlank()) {
                        product.setImageUrl(currentImage.trim());
                    }
                }
                if (product.getId() > 0) {
                    int updated = productDao.updateProductForMerchant(product, merchant.getId());
                    if (updated == 0) {
                        request.getSession().setAttribute("adminProductError", "没有权限更新该商品。");
                    } else {
                        if (imageUrl != null) {
                            deleteImageIfPresent(request.getParameter("currentImageUrl"));
                        }
                        request.getSession().setAttribute("adminProductMessage", "商品已更新。");
                    }
                } else {
                    // 新增商品默认归属当前商家
                    product.setMerchantId(merchant.getId());
                    productDao.insertProduct(product);
                    request.getSession().setAttribute("adminProductMessage", "商品已新增。");
                }
            }
        } catch (ValidationException ex) {
            request.getSession().setAttribute("adminProductError", ex.getMessage());
        } catch (SQLException ex) {
            request.getSession().setAttribute("adminProductError", "商品保存失败。");
        }

        response.sendRedirect(request.getContextPath() + "/admin/products");
    }

    private Product buildProductFromRequest(HttpServletRequest request) throws ValidationException {
        long productId = parseLong(request.getParameter("productId"));
        long categoryId = parseLong(request.getParameter("categoryId"));
        String name = request.getParameter("name");
        BigDecimal price = parseBigDecimal(request.getParameter("price"));
        int stock = parseInt(request.getParameter("stock"));
        int status = parseIntWithDefault(request.getParameter("status"), 1);
        String description = request.getParameter("description");

        if (categoryId <= 0 || isBlank(name) || price == null || stock < 0) {
            throw new ValidationException("商品信息不完整或格式不正确。");
        }

        Product product = new Product();
        product.setId(productId);
        product.setCategoryId(categoryId);
        product.setName(name.trim());
        product.setPrice(price);
        product.setStock(stock);
        product.setStatus(status);
        product.setDescription(description == null ? null : description.trim());
        return product;
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

    private long parseLong(String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("请输入正确的数字。");
        }
    }

    private int parseInt(String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("请输入正确的数字。");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("请输入正确的数字。");
        }
    }

    private int parseIntWithDefault(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private BigDecimal parseBigDecimal(String value) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("请输入正确的价格。");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String uploadImageIfPresent(HttpServletRequest request)
            throws IOException, ServletException, ValidationException {
        Part part = request.getPart("imageFile");
        if (part == null || part.getSize() == 0) {
            return null;
        }
        OssService ossService = new OssService();
        return ossService.uploadImage(part);
    }

    private void deleteImageIfPresent(String imageUrl) {
        if (isBlank(imageUrl)) {
            return;
        }
        try {
            OssService ossService = new OssService();
            ossService.deleteByUrl(imageUrl);
        } catch (ValidationException ex) {
            // 删除失败不影响商品删除主流程
        }
    }
}
