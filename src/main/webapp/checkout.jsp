<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.javaweb.shop.model.CartSummary" %>
<%@ page import="com.javaweb.shop.model.CartItem" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>结算</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<jsp:include page="/partials/navbar.jsp" />
<main>
    <div class="container">
        <div class="card">
            <h1>结算确认</h1>
            <div class="steps">
                <div class="step active">1. 填写收货信息</div>
                <div class="step">2. 选择支付方式</div>
                <div class="step">3. 确认完成</div>
            </div>
            <div class="error">${requestScope.error}</div>
<%
    CartSummary summary = (CartSummary) request.getAttribute("cartSummary");
    List<CartItem> items = summary == null ? null : summary.getItems();
%>
<% if (items == null || items.isEmpty()) { %>
    <p>购物车为空，请先添加商品。</p>
<% } else { %>
    <table>
        <thead>
        <tr>
            <th>商品</th>
            <th>单价</th>
            <th>数量</th>
            <th>小计</th>
        </tr>
        </thead>
        <tbody>
        <% for (CartItem item : items) { %>
            <tr>
                <td><%= item.getProduct().getName() %></td>
                <td><%= item.getUnitPrice() %></td>
                <td><%= item.getQuantity() %></td>
                <td><%= item.getSubtotal() %></td>
            </tr>
        <% } %>
        </tbody>
    </table>
    <div class="actions" style="justify-content: flex-end;">
        <div class="pill">合计：<%= summary.getTotal() %></div>
    </div>

    <h2>收货信息</h2>
    <form method="post" action="${pageContext.request.contextPath}/checkout">
        <div class="form-group">
            <label for="receiverName">收货人</label>
            <input id="receiverName" name="receiverName" required>
        </div>
        <div class="form-group">
            <label for="receiverPhone">联系电话</label>
            <input id="receiverPhone" name="receiverPhone" required>
        </div>
        <div class="form-group">
            <label for="receiverAddress">收货地址</label>
            <input id="receiverAddress" name="receiverAddress" required>
        </div>
        <div class="actions">
            <button type="submit">提交订单</button>
            <a class="btn secondary" href="${pageContext.request.contextPath}/cart">返回购物车</a>
        </div>
    </form>
<% } %>
        </div>
    </div>
</main>
</body>
</html>
