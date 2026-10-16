<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="nav.settings" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="section-title"><fmt:message key="settings.title"/></h2>

<div style="display:flex; flex-direction:column; gap:20px; max-width:640px;">

    <div class="form-box" style="margin:0; max-width:none;">
        <h3 style="margin-top:0;"><fmt:message key="settings.profile"/></h3>
        <form action="${pageContext.request.contextPath}/settings" method="post">
            <input type="hidden" name="action" value="updateProfile">
            <div class="form-group">
                <label for="email"><fmt:message key="settings.emailFixed"/></label>
                <input type="text" id="email" value="<c:out value='${user.email}'/>" disabled>
            </div>
            <div class="form-group">
                <label for="fullName"><fmt:message key="label.fullName"/></label>
                <input type="text" id="fullName" name="fullName" value="<c:out value='${user.fullName}'/>" required>
            </div>
            <div class="form-group">
                <label for="phone"><fmt:message key="label.phone"/></label>
                <input type="text" id="phone" name="phone" value="<c:out value='${user.phone}'/>">
            </div>
            <div class="form-group">
                <label for="address"><fmt:message key="label.address"/></label>
                <textarea id="address" name="address" rows="2"><c:out value="${user.address}"/></textarea>
            </div>
            <button type="submit" class="btn btn-primary"><fmt:message key="btn.save"/></button>
        </form>
    </div>

    <%-- Language + appearance --%>
    <div class="form-box" style="margin:0; max-width:none;">
        <h3 style="margin-top:0;"><fmt:message key="settings.langTheme"/></h3>
        <form action="${pageContext.request.contextPath}/lang" method="get">
            <div class="form-group">
                <label for="langSel">&#127760; <fmt:message key="settings.language"/></label>
                <select id="langSel" name="code" onchange="this.form.submit()">
                    <c:forEach var="l" items="${languages}">
                        <option value="${l.key}" ${l.key == lang ? 'selected' : ''}>${l.value}</option>
                    </c:forEach>
                </select>
                <p class="muted" style="margin:6px 0 0;"><fmt:message key="settings.languageNote"/></p>
            </div>
        </form>
        <div class="form-group" style="margin-bottom:0;">
            <label for="themeSel">&#127763; <fmt:message key="settings.theme"/></label>
            <select id="themeSel" onchange="setTheme(this.value)">
                <option value="system" ${empty cookie.theme ? 'selected' : ''}><fmt:message key="settings.theme.system"/></option>
                <option value="light" ${cookie.theme.value == 'light' ? 'selected' : ''}><fmt:message key="settings.theme.light"/></option>
                <option value="dark" ${cookie.theme.value == 'dark' ? 'selected' : ''}><fmt:message key="settings.theme.dark"/></option>
            </select>
        </div>
    </div>

    <div class="form-box" style="margin:0; max-width:none;">
        <h3 style="margin-top:0;"><fmt:message key="settings.security"/></h3>
        <p class="muted" style="font-size:14px;"><fmt:message key="settings.securityText"/></p>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/change-password"><fmt:message key="title.changePassword"/></a>
    </div>

    <div class="form-box" style="margin:0; max-width:none;">
        <h3 style="margin-top:0;"><fmt:message key="nav.notifications"/></h3>
        <p class="muted" style="font-size:14px;"><fmt:message key="settings.unread"><fmt:param value="${unreadCount}"/></fmt:message></p>
        <a class="btn" href="${pageContext.request.contextPath}/notifications"><fmt:message key="btn.view"/></a>
    </div>

    <div class="form-box" style="margin:0; max-width:none;">
        <h3 style="margin-top:0;"><fmt:message key="settings.accountInfo"/></h3>
        <table class="data-table">
            <tr><th style="width:45%;"><fmt:message key="settings.role"/></th><td><fmt:message key="role.${user.role}"/></td></tr>
            <tr><th><fmt:message key="nav.reservations"/></th><td>${reservationCount}</td></tr>
            <tr><th><fmt:message key="nav.taxis"/></th><td>${taxiCount}</td></tr>
        </table>
        <p style="margin-top:14px;">
            <a class="btn" href="${pageContext.request.contextPath}/logout"><fmt:message key="nav.logout"/></a>
        </p>
    </div>

    <div class="form-box danger-zone" style="margin:0; max-width:none;">
        <h3 style="margin-top:0;">&#9888; <fmt:message key="settings.danger"/></h3>
        <p class="muted" style="font-size:14px;"><fmt:message key="settings.deleteText"/></p>
        <a class="btn btn-danger" href="${pageContext.request.contextPath}/delete-account"><fmt:message key="delete.title"/></a>
    </div>
</div>

<%@ include file="common/footer.jsp" %>
