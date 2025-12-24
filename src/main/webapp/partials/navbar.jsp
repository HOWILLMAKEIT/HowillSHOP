<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.javaweb.shop.model.CartItem" %>
<%@ page import="com.javaweb.shop.model.CartSummary" %>
<%@ page import="com.javaweb.shop.model.User" %>
<%!
    private boolean isActive(String uri, String path) {
        return uri != null && uri.contains(path);
    }
%>
<%
    String contextPath = request.getContextPath();
    String uri = request.getRequestURI();
    User currentUser = (User) session.getAttribute("currentUser");
    boolean loggedIn = currentUser != null;
    boolean isMerchant = loggedIn && "MERCHANT".equalsIgnoreCase(currentUser.getRole());
    boolean isAdminPath = uri != null && uri.contains("/admin/");
    int cartCount = 0;
    CartSummary summary = (CartSummary) session.getAttribute("cartSummary");
    if (summary != null && summary.getItems() != null) {
        for (CartItem item : summary.getItems()) {
            cartCount += item.getQuantity();
        }
    }
%>
<nav class="navbar">
    <div class="container nav-inner">
        <a class="logo" href="<%= contextPath %>/products">
            <span class="logo-mark">HowillShop</span>
            <span class="logo-text">小昊商城</span>
        </a>
        <div class="nav-links">
            <a class="nav-link<%= (!isAdminPath && isActive(uri, "/products")) ? " active" : "" %>" href="<%= contextPath %>/products">
                <span class="icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                        <rect x="3" y="3" width="7" height="7" rx="1.5"></rect>
                        <rect x="14" y="3" width="7" height="7" rx="1.5"></rect>
                        <rect x="3" y="14" width="7" height="7" rx="1.5"></rect>
                        <rect x="14" y="14" width="7" height="7" rx="1.5"></rect>
                    </svg>
                </span>
                商品
            </a>
            <a class="nav-link<%= (!isAdminPath && isActive(uri, "/cart")) ? " active" : "" %>" href="<%= contextPath %>/cart">
                <span class="icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                        <path d="M6 6h15l-1.5 8H8.2L6 6z"></path>
                        <circle cx="9" cy="20" r="1.6"></circle>
                        <circle cx="18" cy="20" r="1.6"></circle>
                        <path d="M3 3h2l2 10"></path>
                    </svg>
                </span>
                购物车
                <span class="nav-badge"><%= cartCount %></span>
            </a>
            <a class="nav-link<%= (!isAdminPath && isActive(uri, "/orders")) ? " active" : "" %>" href="<%= contextPath %>/orders">
                <span class="icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                        <path d="M7 6h10"></path>
                        <path d="M7 12h10"></path>
                        <path d="M7 18h6"></path>
                        <rect x="4" y="3" width="16" height="18" rx="2"></rect>
                    </svg>
                </span>
                订单
            </a>
        </div>
        <div class="nav-actions">
            <% if (isMerchant) { %>
                <div class="nav-dropdown">
                    <button type="button" class="nav-link<%= isAdminPath ? " active" : "" %>">
                        <span class="icon" aria-hidden="true">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                                <path d="M4 6h16"></path>
                                <path d="M4 12h16"></path>
                                <path d="M4 18h16"></path>
                            </svg>
                        </span>
                        商家后台
                    </button>
                    <div class="dropdown-menu">
                        <a href="<%= contextPath %>/admin/orders">订单管理</a>
                        <a href="<%= contextPath %>/admin/products">商品管理</a>
                        <a href="<%= contextPath %>/admin/sales">销售报表</a>
                    </div>
                </div>
            <% } %>
            <% if (loggedIn) { %>
                <div class="user-chip">
                    <span class="icon" aria-hidden="true">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6">
                            <circle cx="12" cy="8" r="4"></circle>
                            <path d="M4 20c1.6-3.6 5-6 8-6s6.4 2.4 8 6"></path>
                        </svg>
                    </span>
                    <span><%= currentUser.getUsername() %></span>
                </div>
                <a class="btn secondary" href="<%= contextPath %>/auth/logout">退出</a>
            <% } else { %>
                <a class="btn secondary<%= isActive(uri, "/auth/login") ? " active" : "" %>" href="<%= contextPath %>/auth/login">登录</a>
                <a class="btn<%= isActive(uri, "/auth/register") ? " active" : "" %>" href="<%= contextPath %>/auth/register">注册</a>
            <% } %>
        </div>
    </div>
</nav>
