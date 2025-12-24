<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.javaweb.shop.model.OrderDetail" %>
<%@ page import="com.javaweb.shop.model.OrderItem" %>
<%@ page import="java.util.List" %>
<%!
    private String orderStatusLabel(String status) {
        if (status == null) {
            return "-";
        }
        switch (status) {
            case "CREATED":
                return "待支付";
            case "PAID":
                return "已支付";
            case "SHIPPED":
                return "已发货";
            case "COMPLETED":
                return "已完成";
            case "CANCELLED":
                return "已取消";
            default:
                return status;
        }
    }

    private String payStatusLabel(String status) {
        if (status == null) {
            return "-";
        }
        switch (status) {
            case "UNPAID":
                return "未支付";
            case "PAID":
                return "已支付";
            case "REFUNDED":
                return "已退款";
            default:
                return status;
        }
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>支付</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<jsp:include page="/partials/navbar.jsp" />
<main>
    <div class="container">
        <div class="card">
            <h1>支付确认</h1>
            <div class="steps">
                <div class="step done">1. 填写收货信息</div>
                <div class="step active">2. 选择支付方式</div>
                <div class="step">3. 确认完成</div>
            </div>
            <div class="error">${requestScope.error}</div>
<%
    OrderDetail detail = (OrderDetail) request.getAttribute("orderDetail");
    List<OrderItem> items = detail == null ? null : detail.getItems();
%>
<% if (detail == null) { %>
    <p>订单不存在。</p>
<% } else { %>
    <p>订单号：<strong><%= detail.getOrder().getOrderNo() %></strong></p>
    <p>状态：<%= orderStatusLabel(detail.getOrder().getOrderStatus()) %> / <%= payStatusLabel(detail.getOrder().getPayStatus()) %></p>
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
        <% if (items != null) { %>
            <% for (OrderItem item : items) { %>
                <tr>
                    <td><%= item.getProductName() %></td>
                    <td><%= item.getUnitPrice() %></td>
                    <td><%= item.getQuantity() %></td>
                    <td><%= item.getSubtotal() %></td>
                </tr>
            <% } %>
        <% } %>
        </tbody>
    </table>
    <div class="actions" style="justify-content: flex-end;">
        <div class="pill">合计：<%= detail.getOrder().getTotalAmount() %></div>
    </div>

    <h2>模拟支付</h2>
    <form method="post" action="${pageContext.request.contextPath}/payment">
        <input type="hidden" name="orderId" value="<%= detail.getOrder().getId() %>">
        <input type="hidden" name="result" value="success">
        <div class="actions">
            <button type="submit">确认支付</button>
            <a class="btn secondary" href="${pageContext.request.contextPath}/orders/detail?orderId=<%= detail.getOrder().getId() %>">返回订单</a>
        </div>
    </form>
<% } %>
        </div>
    </div>
</main>
</body>
</html>
