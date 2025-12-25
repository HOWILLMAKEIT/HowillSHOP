<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.javaweb.shop.model.Order" %>
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
    <title>订单管理</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<jsp:include page="/partials/navbar.jsp" />
<main>
    <div class="container">
        <div class="card">
            <h1>订单管理</h1>
            <div class="message">${requestScope.message}</div>
            <div class="error">${requestScope.error}</div>
<%
    List<Order> orders = (List<Order>) request.getAttribute("orders");
    String selectedStatus = (String) request.getAttribute("selectedStatus");
    if (selectedStatus == null || selectedStatus.isBlank()) {
        selectedStatus = "ALL";
    }
%>
            <form class="filter" method="get" action="${pageContext.request.contextPath}/admin/orders">
                <label for="status">状态筛选：</label>
                <select id="status" name="status">
                    <option value="ALL" <%= "ALL".equals(selectedStatus) ? "selected" : "" %>>全部</option>
                    <option value="CREATED" <%= "CREATED".equals(selectedStatus) ? "selected" : "" %>>待支付</option>
                    <option value="PAID" <%= "PAID".equals(selectedStatus) ? "selected" : "" %>>已支付</option>
                    <option value="UNPAID" <%= "UNPAID".equals(selectedStatus) ? "selected" : "" %>>未支付</option>
                    <option value="SHIPPED" <%= "SHIPPED".equals(selectedStatus) ? "selected" : "" %>>已发货</option>
                    <option value="COMPLETED" <%= "COMPLETED".equals(selectedStatus) ? "selected" : "" %>>已完成</option>
                </select>
                <button type="submit">筛选</button>
                <a class="btn secondary" href="${pageContext.request.contextPath}/products">返回首页</a>
            </form>

<% if (orders == null || orders.isEmpty()) { %>
    <p>暂无订单。</p>
<% } else { %>
    <table>
        <thead>
        <tr>
            <th>订单号</th>
            <th>用户ID</th>
            <th>金额</th>
            <th>支付状态</th>
            <th>发货状态</th>
            <th>订单状态</th>
            <th>创建时间</th>
            <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <% for (Order order : orders) { %>
            <tr>
                <td><%= order.getOrderNo() %></td>
                <td><%= order.getUserId() %></td>
                <td><%= order.getTotalAmount() %></td>
                <td><%= payStatusLabel(order.getPayStatus()) %></td>
                <td><%= shipStatusLabel(order.getShipStatus()) %></td>
                <td><%= orderCompletionLabel(order.getOrderStatus()) %></td>
                <td><%= order.getCreatedAt() %></td>
                <td>
                    <a class="btn secondary" href="${pageContext.request.contextPath}/admin/orders/detail?orderId=<%= order.getId() %>">查看</a>
                </td>
            </tr>
        <% } %>
        </tbody>
    </table>
<% } %>
        </div>
    </div>
</main>
</body>
</html>
