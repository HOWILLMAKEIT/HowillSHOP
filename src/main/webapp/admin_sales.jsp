<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.javaweb.shop.model.ProductSales" %>
<%@ page import="com.javaweb.shop.model.SalesSummary" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>销售统计</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<jsp:include page="/partials/navbar.jsp" />
<main>
    <div class="container">
        <div class="card">
            <div class="top-nav">
                <div>
                    <h1>销售统计</h1>
                    <div class="muted">统计已支付订单的趋势与商品销量</div>
                </div>
                <a class="btn secondary" href="${pageContext.request.contextPath}/products">返回首页</a>
            </div>
<%
    String startDate = (String) request.getAttribute("startDate");
    String endDate = (String) request.getAttribute("endDate");
    List<SalesSummary> dailySales = (List<SalesSummary>) request.getAttribute("dailySales");
    List<ProductSales> productSales = (List<ProductSales>) request.getAttribute("productSales");
%>
            <form class="filter" method="get" action="${pageContext.request.contextPath}/admin/sales">
                <div class="form-grid">
                    <div class="form-group">
                        <label for="startDate">开始日期</label>
                        <input id="startDate" type="date" name="startDate" value="<%= startDate == null ? "" : startDate %>">
                    </div>
                    <div class="form-group">
                        <label for="endDate">结束日期</label>
                        <input id="endDate" type="date" name="endDate" value="<%= endDate == null ? "" : endDate %>">
                    </div>
                </div>
                <div class="actions">
                    <button type="submit">应用筛选</button>
                </div>
            </form>

            <div class="section">
                <h2>每日销售汇总</h2>
                <% if (dailySales == null || dailySales.isEmpty()) { %>
                    <p class="muted">暂无数据。</p>
                <% } else { %>
                    <table>
                        <thead>
                        <tr>
                            <th>日期</th>
                            <th>订单数</th>
                            <th>销售额</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (SalesSummary row : dailySales) { %>
                            <tr>
                                <td><%= row.getSaleDate() %></td>
                                <td><%= row.getOrderCount() %></td>
                                <td><%= row.getTotalAmount() %></td>
                            </tr>
                        <% } %>
                        </tbody>
                    </table>
                <% } %>
            </div>

            <div class="section">
                <h2>商品销量排行</h2>
                <% if (productSales == null || productSales.isEmpty()) { %>
                    <p class="muted">暂无数据。</p>
                <% } else { %>
                    <table>
                        <thead>
                        <tr>
                            <th>商品</th>
                            <th>销量</th>
                            <th>销售额</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% for (ProductSales row : productSales) { %>
                            <tr>
                                <td><%= row.getProductName() %></td>
                                <td><%= row.getTotalQuantity() %></td>
                                <td><%= row.getTotalAmount() %></td>
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
