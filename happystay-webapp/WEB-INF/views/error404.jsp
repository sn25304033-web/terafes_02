<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="title.err404" scope="request"/>
<%@ include file="common/header.jsp" %>
<div class="form-box" style="text-align:center;">
    <h2>404 - <fmt:message key="title.err404"/></h2>
    <p><fmt:message key="err404.text"/></p>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/home"><fmt:message key="btn.goHome"/></a>
</div>
<%@ include file="common/footer.jsp" %>
