<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>注册</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<jsp:include page="/partials/navbar.jsp" />
<main>
    <div class="container">
        <div class="card">
            <h1>注册</h1>
            <div class="error">${requestScope.error}</div>
            <form method="post" action="${pageContext.request.contextPath}/auth/register">
                <div class="form-group">
                    <label for="registerType">注册身份</label>
                    <div class="actions">
                        <label>
                            <input type="radio" name="registerType" value="user"
                                   <%= (request.getParameter("registerType") == null || "user".equals(request.getParameter("registerType"))) ? "checked" : "" %>>
                            普通用户
                        </label>
                        <label>
                            <input type="radio" name="registerType" value="merchant"
                                   <%= "merchant".equals(request.getParameter("registerType")) ? "checked" : "" %>>
                            商家
                        </label>
                    </div>
                </div>
                <div class="form-group">
                    <label for="username">用户名</label>
                    <input id="username" name="username" required value="${param.username}">
                </div>
                <div class="form-group">
                    <label for="email">邮箱</label>
                    <input id="email" name="email" type="email" required value="${param.email}">
                </div>
                <div class="form-group">
                    <label for="phone">手机（可选）</label>
                    <input id="phone" name="phone" value="${param.phone}">
                </div>
                <div class="form-group">
                    <label for="password">密码</label>
                    <input id="password" name="password" type="password" required>
                </div>
                <div class="form-group">
                    <label for="confirm_password">确认密码</label>
                    <input id="confirm_password" name="confirm_password" type="password" required>
                </div>
                <div class="actions">
                    <button type="submit">注册</button>
                    <a class="btn secondary" href="${pageContext.request.contextPath}/auth/login">已有账号</a>
                </div>
            </form>
        </div>
    </div>
</main>
</body>
</html>
