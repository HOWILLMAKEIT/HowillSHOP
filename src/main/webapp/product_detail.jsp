<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.javaweb.shop.model.Product" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>商品详情</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<jsp:include page="/partials/navbar.jsp" />
<%
    Product product = (Product) request.getAttribute("product");
    String contextPath = request.getContextPath();
%>
<main>
    <div class="container">
        <div class="card">
            <% if (product == null) { %>
                <p>商品不存在。</p>
            <% } else { %>
                <div class="product-detail">
                    <div class="product-media">
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
                    </div>
                    <div>
                        <h1><%= product.getName() %></h1>
                        <div class="muted">分类：<%= product.getCategoryName() == null ? "-" : product.getCategoryName() %></div>
                        <div class="muted">商家：<%= product.getMerchantName() == null ? "平台自营" : product.getMerchantName() %></div>
                        <div class="price">￥<%= product.getPrice() %></div>
                        <div class="muted">库存：<%= product.getStock() %></div>
                        <p><%= product.getDescription() == null ? "" : product.getDescription() %></p>
                        <form method="post" action="${pageContext.request.contextPath}/cart/add">
                            <input type="hidden" name="productId" value="<%= product.getId() %>">
                            <div class="form-group">
                                <label for="quantity">数量</label>
                                <input id="quantity" type="number" name="quantity" min="1" value="1">
                            </div>
                            <div class="actions">
                                <button type="submit">加入购物车</button>
                                <a class="btn secondary" href="${pageContext.request.contextPath}/products">返回列表</a>
                            </div>
                        </form>
                    </div>
                </div>
            <% } %>
        </div>
    </div>
</main>
</body>
</html>
