<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="title.changePassword" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="form-box">
    <h2><fmt:message key="title.changePassword"/></h2>
    <c:if test="${not empty info}"><div class="flash"><c:out value="${info}"/></div></c:if>

    <c:choose>
        <c:when test="${not codeSent}">
            <p class="muted" style="font-size:14px;"><fmt:message key="auth.changeIntro"/></p>
            <form action="${pageContext.request.contextPath}/change-password" method="post">
                <input type="hidden" name="action" value="sendCode">
                <button type="submit" class="btn btn-primary btn-block"><fmt:message key="btn.sendCode"/></button>
            </form>
        </c:when>
        <c:otherwise>
            <form action="${pageContext.request.contextPath}/change-password" method="post">
                <input type="hidden" name="action" value="confirm">
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
                <button type="submit" class="btn btn-primary btn-block"><fmt:message key="auth.changeBtn"/></button>
            </form>
            <form action="${pageContext.request.contextPath}/change-password" method="post" style="margin-top:10px;">
                <input type="hidden" name="action" value="sendCode">
                <button type="submit" class="btn btn-block"><fmt:message key="btn.resendCode"/></button>
            </form>
        </c:otherwise>
    </c:choose>
</div>

<%@ include file="common/footer.jsp" %>
