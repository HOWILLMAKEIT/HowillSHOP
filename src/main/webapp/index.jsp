<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="com.javaweb.shop.model.User" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return;
    }
    response.sendRedirect(request.getContextPath() + "/products");
    return;
%>
