<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="title.register" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="form-box">
    <h2><fmt:message key="auth.createAccount"/></h2>
    <form action="${pageContext.request.contextPath}/register" method="post">
        <div class="form-group">
            <label for="fullName"><fmt:message key="label.fullName"/></label>
            <input type="text" id="fullName" name="fullName" value="<c:out value='${param.fullName}'/>" required>
        </div>
        <div class="form-group">
            <label for="email"><fmt:message key="label.email"/></label>
            <input type="email" id="email" name="email" value="<c:out value='${param.email}'/>" required>
        </div>
        <div class="form-group">
            <label for="password"><fmt:message key="label.password"/> <span class="muted">(<fmt:message key="auth.min4"/>)</span></label>
            <input type="password" id="password" name="password" minlength="4" required>
        </div>
        <div class="form-group">
            <label for="phone"><fmt:message key="label.phone"/></label>
            <input type="text" id="phone" name="phone" value="<c:out value='${param.phone}'/>">
        </div>
        <div class="form-group">
            <label for="address"><fmt:message key="label.address"/></label>
            <input type="text" id="address" name="address" value="<c:out value='${param.address}'/>">
        </div>
        <button type="submit" class="btn btn-primary btn-block"><fmt:message key="nav.register"/></button>
    </form>
    <p style="margin-top:14px;font-size:14px;">
        <fmt:message key="auth.haveAccount"/> <a href="${pageContext.request.contextPath}/login" style="color:var(--primary);"><fmt:message key="auth.loginHere"/></a>
    </p>
</div>

<%@ include file="common/footer.jsp" %>
