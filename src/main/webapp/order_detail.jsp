<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.javaweb.shop.model.OrderDetail" %>
<%@ page import="com.javaweb.shop.model.OrderItem" %>
<%@ page import="java.util.List" %>
<%!
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

    private String shipStatusLabel(String status) {
        if (status == null) {
            return "-";
        }
        switch (status) {
            case "PENDING":
                return "未发货";
            case "SHIPPED":
                return "已发货";
            case "DELIVERED":
                return "已送达";
            default:
                return status;
        }
    }

    private String orderCompletionLabel(String status) {
        if ("COMPLETED".equals(status)) {
            return "已完成";
        }
        return "未完成";
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>订单详情</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<jsp:include page="/partials/navbar.jsp" />
<main>
    <div class="container">
        <div class="card">
    <h1>订单详情</h1>
    <div class="message">${requestScope.message}</div>
    <div class="error">${requestScope.error}</div>
<%
    OrderDetail detail = (OrderDetail) request.getAttribute("orderDetail");
    List<OrderItem> items = detail == null ? null : detail.getItems();
%>
<% if (detail == null) { %>
    <p>订单不存在。</p>
<% } else { %>
    <div class="muted" style="margin-bottom: 12px;">
        <div>订单号：<strong><%= detail.getOrder().getOrderNo() %></strong></div>
        <div>支付状态：<%= payStatusLabel(detail.getOrder().getPayStatus()) %></div>
        <div>发货状态：<%= shipStatusLabel(detail.getOrder().getShipStatus()) %></div>
        <div>订单状态：<%= orderCompletionLabel(detail.getOrder().getOrderStatus()) %></div>
        <div>收货人：<%= detail.getOrder().getReceiverName() %> - <%= detail.getOrder().getReceiverPhone() %></div>
        <div>地址：<%= detail.getOrder().getReceiverAddress() %></div>
        <div>金额：<%= detail.getOrder().getTotalAmount() %></div>
    </div>
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
    <div class="actions">
        <% if ("UNPAID".equals(detail.getOrder().getPayStatus())) { %>
            <a class="btn" href="${pageContext.request.contextPath}/payment?orderId=<%= detail.getOrder().getId() %>">去支付</a>
        <% } %>
        <% if ("SHIPPED".equals(detail.getOrder().getShipStatus())) { %>
            <form method="post" action="${pageContext.request.contextPath}/orders/detail">
                <input type="hidden" name="action" value="confirmReceipt">
                <input type="hidden" name="orderId" value="<%= detail.getOrder().getId() %>">
                <button type="submit">确认收货</button>
            </form>
        <% } %>
        <a class="btn secondary" href="${pageContext.request.contextPath}/orders">返回订单列表</a>
    </div>
<% } %>
        </div>
    </div>
</main>
</body>
</html>
