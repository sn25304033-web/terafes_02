<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="title.forgot" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="form-box">
    <h2><fmt:message key="title.forgot"/></h2>
    <c:if test="${not empty info}"><div class="flash"><c:out value="${info}"/></div></c:if>

    <c:choose>
        <c:when test="${not codeSent}">
            <p class="muted" style="font-size:14px;"><fmt:message key="auth.forgotIntro"/></p>
            <form action="${pageContext.request.contextPath}/forgot-password" method="post">
                <input type="hidden" name="action" value="sendCode">
                <div class="form-group">
                    <label for="email"><fmt:message key="label.email"/></label>
                    <input type="email" id="email" name="email" required autofocus>
                </div>
                <button type="submit" class="btn btn-primary btn-block"><fmt:message key="btn.sendCode"/></button>
            </form>
        </c:when>
        <c:otherwise>
            <form action="${pageContext.request.contextPath}/forgot-password" method="post">
                <input type="hidden" name="action" value="confirm">
                <input type="hidden" name="email" value="<c:out value='${email}'/>">
                <div class="form-group">
                    <label><fmt:message key="label.email"/></label>
                    <input type="text" value="<c:out value='${email}'/>" disabled>
                </div>
                <div class="form-group">
                    <label for="code"><fmt:message key="label.code"/></label>
                    <input type="text" id="code" name="code" maxlength="6" inputmode="numeric" required autofocus>
                </div>
                <div class="form-group">
                    <label for="newPassword"><fmt:message key="label.newPassword"/></label>
                    <input type="password" id="newPassword" name="newPassword" minlength="4" required>
                </div>
                <div class="form-group">
                    <label for="confirmPassword"><fmt:message key="label.confirmPassword"/></label>
                    <input type="password" id="confirmPassword" name="confirmPassword" minlength="4" required>
                </div>
                <button type="submit" class="btn btn-primary btn-block"><fmt:message key="auth.resetBtn"/></button>
            </form>
            <form action="${pageContext.request.contextPath}/forgot-password" method="post" style="margin-top:10px;">
                <input type="hidden" name="action" value="sendCode">
                <input type="hidden" name="email" value="<c:out value='${email}'/>">
                <button type="submit" class="btn btn-block"><fmt:message key="btn.resendCode"/></button>
            </form>
        </c:otherwise>
    </c:choose>
    <p style="margin-top:14px;font-size:14px;">
        <a href="${pageContext.request.contextPath}/login" style="color:var(--primary);"><fmt:message key="auth.backToLogin"/></a>
    </p>
</div>

<%@ include file="common/footer.jsp" %>
