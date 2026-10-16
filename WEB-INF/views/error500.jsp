<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="title.err500" scope="request"/>
<%@ include file="common/header.jsp" %>
<div class="form-box" style="text-align:center; max-width:700px;">
    <h2>500 - <fmt:message key="title.err500"/></h2>
    <p><fmt:message key="err500.text"/></p>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/home"><fmt:message key="btn.goHome"/></a>
    <%
        Throwable cause = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        if (cause != null) {
            Throwable root = cause;
            while (root.getCause() != null) root = root.getCause();
    %>
    <div style="text-align:left; margin-top:20px; padding:14px; background:var(--danger-bg);
                border-radius:8px; font-family:monospace; font-size:12px; color:var(--danger-text); overflow:auto;">
        <strong>Debug info (remove this block in production):</strong><br>
        <%= root.getClass().getName() %>: <%= String.valueOf(root.getMessage()).replace("<", "&lt;") %>
    </div>
    <% } %>
</div>
<%@ include file="common/footer.jsp" %>
