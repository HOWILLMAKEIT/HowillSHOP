<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.javaweb.shop.model.Category" %>
<%@ page import="com.javaweb.shop.model.Product" %>
<%@ page import="java.util.List" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>商品列表</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<jsp:include page="/partials/navbar.jsp" />
<main>
    <div class="container">
        <div class="card" style="margin-bottom: 16px;">
            <h1>商品列表</h1>
<%
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    List<Product> products = (List<Product>) request.getAttribute("products");
    Long selectedCategoryId = (Long) request.getAttribute("selectedCategoryId");
    String keyword = (String) request.getAttribute("keyword");
    Integer currentPage = (Integer) request.getAttribute("currentPage");
    Integer totalPages = (Integer) request.getAttribute("totalPages");
    String contextPath = request.getContextPath();
    if (currentPage == null || currentPage < 1) {
        currentPage = 1;
    }
%>
            <form class="filters" method="get" action="${pageContext.request.contextPath}/products">
                <input type="hidden" name="page" value="1">
                <div class="filters-left">
                    <div class="search-field">
                        <input type="search" name="keyword" list="productSuggestions"
                               placeholder="搜索商品名称" value="<%= keyword == null ? "" : keyword %>">
                    </div>
                    <div class="category-field">
                        <label class="sr-only" for="categoryId">分类</label>
                        <select id="categoryId" name="categoryId">
                            <option value="" <%= selectedCategoryId == null ? "selected" : "" %>>全部</option>
                            <% if (categories != null) { %>
                                <% for (Category category : categories) { %>
                                    <option value="<%= category.getId() %>"
                                            <%= (selectedCategoryId != null && selectedCategoryId == category.getId()) ? "selected" : "" %>>
                                        <%= category.getName() %>
                                    </option>
                                <% } %>
                            <% } %>
                        </select>
                    </div>
                </div>
                <button type="submit" class="search-btn">搜索</button>
            </form>
            <datalist id="productSuggestions">
                <% if (products != null) { %>
                    <% for (Product product : products) { %>
                        <option value="<%= product.getName() %>"></option>
                    <% } %>
                <% } %>
            </datalist>
        </div>

<% if (products == null || products.isEmpty()) { %>
    <p class="muted">暂无商品。</p>
<% } else { %>
    <div class="grid">
        <% for (Product product : products) { %>
            <div class="card product-card">
                <%
                    String imageUrl = product.getImageUrl();
                    String imageSrc = null;
                    if (imageUrl != null && !imageUrl.isBlank()) {
                        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("/")) {
                            imageSrc = imageUrl;
                        } else {
                            imageSrc = contextPath + "/" + imageUrl;
                        }
                    }
                %>
                <% if (imageSrc != null) { %>
                    <img src="<%= imageSrc %>" alt="<%= product.getName() %>">
                <% } else { %>
                    <img src="" alt="暂无图片">
                <% } %>
                <h3><%= product.getName() %></h3>
                <div class="muted">分类：<%= product.getCategoryName() == null ? "-" : product.getCategoryName() %></div>
                <div class="muted">商家：<%= product.getMerchantName() == null ? "平台自营" : product.getMerchantName() %></div>
                <div class="price">￥<%= product.getPrice() %></div>
                    <div class="actions">
                        <a class="btn secondary" href="${pageContext.request.contextPath}/products/detail?productId=<%= product.getId() %>">查看详情</a>
                        <form method="post" action="${pageContext.request.contextPath}/cart/add">
                            <input type="hidden" name="productId" value="<%= product.getId() %>">
                        <input type="hidden" name="quantity" value="1">
                        <button type="submit">加入购物车</button>
                    </form>
                    </div>
                </div>
        <% } %>
    </div>
    <% if (totalPages != null && totalPages > 1) { %>
        <%
            String categoryValue = selectedCategoryId == null ? "" : selectedCategoryId.toString();
            String keywordValue = keyword == null ? "" : URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String baseQuery = "categoryId=" + categoryValue + "&keyword=" + keywordValue;
        %>
        <div class="pagination">
            <% if (currentPage > 1) { %>
                <a class="page-link" href="${pageContext.request.contextPath}/products?<%= baseQuery %>&page=<%= currentPage - 1 %>">上一页</a>
            <% } %>
            <% for (int i = 1; i <= totalPages; i++) { %>
                <a class="page-link<%= i == currentPage ? " active" : "" %>"
                   href="${pageContext.request.contextPath}/products?<%= baseQuery %>&page=<%= i %>"><%= i %></a>
            <% } %>
            <% if (currentPage < totalPages) { %>
                <a class="page-link" href="${pageContext.request.contextPath}/products?<%= baseQuery %>&page=<%= currentPage + 1 %>">下一页</a>
            <% } %>
        </div>
    <% } %>
<% } %>
    </div>
</main>
</body>
</html>
