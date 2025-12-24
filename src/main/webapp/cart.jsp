<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.javaweb.shop.model.CartSummary" %>
<%@ page import="com.javaweb.shop.model.CartItem" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>购物车</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<jsp:include page="/partials/navbar.jsp" />
<main>
    <div class="container">
        <div class="card">
            <h1>我的购物车</h1>
            <div class="error">${requestScope.error}</div>
<%
    CartSummary summary = (CartSummary) request.getAttribute("cartSummary");
    List<CartItem> items = summary == null ? null : summary.getItems();
%>
<% if (items == null || items.isEmpty()) { %>
    <p>购物车为空，去挑点喜欢的商品吧。</p>
<% } else { %>
    <table>
        <thead>
        <tr>
            <th>商品</th>
            <th>单价</th>
            <th>数量</th>
            <th>小计</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <% for (CartItem item : items) { %>
            <tr>
                <td><%= item.getProduct().getName() %></td>
                <td><%= item.getUnitPrice() %></td>
                <td>
                    <form method="post" action="${pageContext.request.contextPath}/cart/update">
                        <input type="hidden" name="productId" value="<%= item.getProduct().getId() %>">
                        <input type="number" name="quantity" min="1" value="<%= item.getQuantity() %>">
                        <button type="submit">更新</button>
                    </form>
                </td>
                <td><%= item.getSubtotal() %></td>
                <td>
                    <form method="post" action="${pageContext.request.contextPath}/cart/remove">
                        <input type="hidden" name="productId" value="<%= item.getProduct().getId() %>">
                        <button type="submit" class="secondary">删除</button>
                    </form>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
    <div class="actions" style="justify-content: flex-end;">
        <div class="pill">合计：<%= summary.getTotal() %></div>
    </div>
<% } %>
            <div class="actions">
                <a class="btn secondary" href="${pageContext.request.contextPath}/products">返回首页</a>
                <% if (items != null && !items.isEmpty()) { %>
                    <a class="btn" href="${pageContext.request.contextPath}/checkout">去结算</a>
                <% } %>
            </div>
        </div>
    </div>
</main>
</body>
</html>
