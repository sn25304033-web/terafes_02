<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="delete.title" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="form-box danger-zone" style="max-width:520px;">
    <h2 style="color:var(--danger-text);">&#9888; <fmt:message key="delete.title"/></h2>
    <c:if test="${not empty info}"><div class="flash"><c:out value="${info}"/></div></c:if>

    <p><fmt:message key="delete.intro"/></p>
    <ul style="font-size:14px; line-height:1.7;">
        <li><fmt:message key="delete.point1"/></li>
        <li><fmt:message key="delete.point2"/></li>
        <li><fmt:message key="delete.point3"/></li>
        <li><fmt:message key="delete.point4"/></li>
    </ul>

    <c:choose>
        <c:when test="${not codeSent}">
            <form action="${pageContext.request.contextPath}/delete-account" method="post">
                <input type="hidden" name="action" value="sendCode">
                <button type="submit" class="btn btn-danger btn-block"><fmt:message key="delete.sendCode"/></button>
            </form>
        </c:when>
        <c:otherwise>
            <form action="${pageContext.request.contextPath}/delete-account" method="post"
                  onsubmit="return confirm('<fmt:message key="delete.finalConfirm"/>');">
                <input type="hidden" name="action" value="confirm">
                <div class="form-group">
                    <label for="code"><fmt:message key="label.code"/></label>
                    <input type="text" id="code" name="code" maxlength="6" inputmode="numeric" required autofocus>
                </div>
                <div class="form-group">
                    <label for="password"><fmt:message key="delete.password"/></label>
                    <input type="password" id="password" name="password" required>
                </div>
                <div class="form-group">
                    <label for="confirmText"><fmt:message key="delete.typeDelete"/></label>
                    <input type="text" id="confirmText" name="confirmText" required autocomplete="off" placeholder="DELETE">
                </div>
                <button type="submit" class="btn btn-danger btn-block"><fmt:message key="delete.confirmBtn"/></button>
            </form>
            <form action="${pageContext.request.contextPath}/delete-account" method="post" style="margin-top:10px;">
                <input type="hidden" name="action" value="sendCode">
                <button type="submit" class="btn btn-block"><fmt:message key="btn.resendCode"/></button>
            </form>
        </c:otherwise>
    </c:choose>
    <p style="margin-top:14px;"><a href="${pageContext.request.contextPath}/settings" style="color:var(--primary);">&larr; <fmt:message key="delete.keep"/></a></p>
</div>

<%@ include file="common/footer.jsp" %>
