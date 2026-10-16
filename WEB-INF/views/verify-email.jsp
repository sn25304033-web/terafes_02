<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="title.verify" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="form-box">
    <h2><fmt:message key="title.verify"/></h2>
    <c:if test="${not empty info}"><div class="flash"><c:out value="${info}"/></div></c:if>
    <p class="muted" style="font-size:14px;">
        <fmt:message key="auth.verifyIntro"><fmt:param><strong><c:out value="${email}"/></strong></fmt:param></fmt:message>
    </p>
    <form action="${pageContext.request.contextPath}/verify-email" method="post">
        <input type="hidden" name="action" value="confirm">
        <input type="hidden" name="email" value="<c:out value='${email}'/>">
        <div class="form-group">
            <label for="code"><fmt:message key="label.code"/></label>
            <input type="text" id="code" name="code" maxlength="6" inputmode="numeric" required autofocus>
        </div>
        <button type="submit" class="btn btn-primary btn-block"><fmt:message key="auth.verifyBtn"/></button>
    </form>
    <form action="${pageContext.request.contextPath}/verify-email" method="post" style="margin-top:10px;">
        <input type="hidden" name="action" value="resend">
        <input type="hidden" name="email" value="<c:out value='${email}'/>">
        <button type="submit" class="btn btn-block"><fmt:message key="btn.resendCode"/></button>
    </form>
    <p style="margin-top:14px;font-size:14px;">
        <a href="${pageContext.request.contextPath}/login" style="color:var(--primary);"><fmt:message key="auth.backToLogin"/></a>
    </p>
</div>

<%@ include file="common/footer.jsp" %>
