<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>登录</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<jsp:include page="/partials/navbar.jsp" />
<main>
    <div class="container">
        <div class="card">
            <h1>账号登录</h1>
            <div class="message">${requestScope.message}</div>
            <div class="error">${requestScope.error}</div>
            <form method="post" action="${pageContext.request.contextPath}/auth/login">
                <div class="form-group">
                    <label for="loginType">登录身份</label>
                    <div class="actions">
                        <label><input type="radio" name="loginType" value="user"
                                <%= (request.getParameter("loginType") == null || "user".equals(request.getParameter("loginType"))) ? "checked" : "" %>> 用户</label>
                        <label><input type="radio" name="loginType" value="merchant"
                                <%= "merchant".equals(request.getParameter("loginType")) ? "checked" : "" %>> 商家</label>
                    </div>
                </div>
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input id="username" name="username" required value="${param.username}">
                </div>
                <div class="form-group">
                    <label for="password">密码</label>
                    <input id="password" name="password" type="password" required>
                </div>
                <div class="actions">
                    <button type="submit">登录</button>
                    <a class="btn secondary" href="${pageContext.request.contextPath}/auth/register">注册账号</a>
                </div>
            </form>
        </div>
    </div>
</main>
</body>
</html>
