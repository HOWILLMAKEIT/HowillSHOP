<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.javaweb.shop.model.Category" %>
<%@ page import="com.javaweb.shop.model.Product" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>商品管理</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<jsp:include page="/partials/navbar.jsp" />
<main>
    <div class="container">
        <div class="card">
            <div class="top-nav">
                <div>
                    <h1>商品管理</h1>
                    <div class="muted">维护商品目录、价格与库存</div>
                </div>
                <a class="btn secondary" href="${pageContext.request.contextPath}/products">返回首页</a>
            </div>
            <div class="message">${requestScope.message}</div>
            <div class="error">${requestScope.error}</div>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    Product editProduct = (Product) request.getAttribute("editProduct");
%>

            <div class="section">
                <h2><%= editProduct == null ? "新增商品" : "编辑商品" %></h2>
                <form method="post" action="${pageContext.request.contextPath}/admin/products" enctype="multipart/form-data">
                    <input type="hidden" name="action" value="save">
                    <input type="hidden" name="productId" value="<%= editProduct == null ? "" : editProduct.getId() %>">
                    <input type="hidden" name="currentImageUrl" value="<%= editProduct == null || editProduct.getImageUrl() == null ? "" : editProduct.getImageUrl() %>">
                    <div class="form-grid">
                        <div class="form-group">
                            <label for="categoryId">商品分类</label>
                            <select id="categoryId" name="categoryId" required>
                                <option value="">请选择</option>
                                <% if (categories != null) { %>
                                    <% for (Category category : categories) { %>
                                        <option value="<%= category.getId() %>"
                                                <%= (editProduct != null && editProduct.getCategoryId() == category.getId()) ? "selected" : "" %>>
                                            <%= category.getName() %>
                                        </option>
                                    <% } %>
                                <% } %>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="name">商品名称</label>
                            <input id="name" name="name" required value="<%= editProduct == null ? "" : editProduct.getName() %>">
                        </div>
                        <div class="form-group">
                            <label for="price">价格</label>
                            <input id="price" name="price" type="number" step="0.01" required
                                   value="<%= editProduct == null ? "" : editProduct.getPrice() %>">
                        </div>
                        <div class="form-group">
                            <label for="stock">库存</label>
                            <input id="stock" name="stock" type="number" required
                                   value="<%= editProduct == null ? "" : editProduct.getStock() %>">
                        </div>
                        <div class="form-group">
                            <label for="status">状态</label>
                            <select id="status" name="status">
                                <option value="1" <%= editProduct == null || editProduct.getStatus() == 1 ? "selected" : "" %>>上架</option>
                                <option value="0" <%= editProduct != null && editProduct.getStatus() == 0 ? "selected" : "" %>>下架</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="imageFile">商品图片</label>
                            <input id="imageFile" name="imageFile" type="file" accept="image/*">
                        </div>
                        <div class="form-group">
                            <label for="description">商品描述</label>
                            <textarea id="description" name="description"><%= (editProduct == null || editProduct.getDescription() == null) ? "" : editProduct.getDescription() %></textarea>
                        </div>
                    </div>
                    <div class="actions">
                        <button type="submit"><%= editProduct == null ? "保存商品" : "更新商品" %></button>
                        <% if (editProduct != null) { %>
                            <a class="btn secondary" href="${pageContext.request.contextPath}/admin/products">取消编辑</a>
                        <% } %>
                    </div>
                </form>
            </div>

            <div class="section">
                <h2>商品列表</h2>
                <% if (products == null || products.isEmpty()) { %>
                    <p class="muted">暂无商品。</p>
                <% } else { %>
                    <table>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>名称</th>
                            <th>分类</th>
                            <th>价格</th>
                            <th>库存</th>
                            <th>状态</th>
                            <th>操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (Product product : products) { %>
                            <tr>
                                <td><%= product.getId() %></td>
                                <td><%= product.getName() %></td>
                                <td><%= product.getCategoryName() == null ? "-" : product.getCategoryName() %></td>
                                <td><%= product.getPrice() %></td>
                                <td><%= product.getStock() %></td>
                                <td><%= product.getStatus() == 1 ? "上架" : "下架" %></td>
                                <td>
                                    <a class="btn secondary" href="${pageContext.request.contextPath}/admin/products?editId=<%= product.getId() %>">编辑</a>
                                    <form class="inline-form" method="post" action="${pageContext.request.contextPath}/admin/products">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="productId" value="<%= product.getId() %>">
                                        <button type="submit" onclick="return confirm('确认删除该商品？')">删除</button>
                                    </form>
                                </td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                <% } %>
            </div>
        </div>
    </div>
</main>
</body>
</html>
