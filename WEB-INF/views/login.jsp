<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="title.login" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="form-box">
    <h2><fmt:message key="title.login"/></h2>
    <form action="${pageContext.request.contextPath}/login" method="post">
        <div class="form-group">
            <label for="email"><fmt:message key="label.email"/></label>
            <input type="email" id="email" name="email" required>
        </div>
        <div class="form-group">
            <label for="password"><fmt:message key="label.password"/></label>
            <input type="password" id="password" name="password" required>
        </div>
        <button type="submit" class="btn btn-primary btn-block"><fmt:message key="nav.login"/></button>
    </form>
    <p style="margin-top:14px;font-size:14px;">
        <fmt:message key="auth.noAccount"/> <a href="${pageContext.request.contextPath}/register" style="color:var(--primary);"><fmt:message key="auth.registerHere"/></a>
    </p>
    <p style="margin-top:8px;font-size:14px;">
        <a href="${pageContext.request.contextPath}/forgot-password" style="color:var(--primary);"><fmt:message key="auth.forgot"/></a>
    </p>
</div>

<%@ include file="common/footer.jsp" %>
