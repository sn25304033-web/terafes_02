<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="admin.reservations" scope="request"/>
<%@ include file="../common/header.jsp" %>

<h2 class="section-title"><fmt:message key="admin.reservations"/></h2>
<%@ include file="nav.jspf" %>

<div style="margin-bottom:14px; display:flex; gap:8px; flex-wrap:wrap;">
    <a class="chip" href="${pageContext.request.contextPath}/admin/reservations"
       style="${empty statusFilter ? 'background:var(--primary);color:#fff;' : ''}"><fmt:message key="admin.all"/></a>
    <c:forEach var="s" items="${statuses}">
        <a class="chip" href="${pageContext.request.contextPath}/admin/reservations?status=${s}"
           style="${statusFilter == s ? 'background:var(--primary);color:#fff;' : ''}"><fmt:message key="status.${s}"/></a>
    </c:forEach>
</div>

<table class="data-table">
    <thead><tr><th>#</th><th><fmt:message key="label.guest"/></th><th><fmt:message key="admin.hotelRoom"/></th><th><fmt:message key="admin.stay"/></th><th><fmt:message key="label.total"/></th><th><fmt:message key="label.payment"/></th><th><fmt:message key="label.status"/></th></tr></thead>
    <tbody>
    <c:forEach var="r" items="${reservations}">
        <tr>
            <td><a href="${pageContext.request.contextPath}/reservation?id=${r.id}">#${r.id}</a></td>
            <td><c:out value="${r.guestName}"/><br><span class="muted"><c:out value="${empty r.userEmail ? '-' : r.userEmail}"/></span></td>
            <td><c:out value="${r.hotelName}"/><br><span class="muted"><c:out value="${r.roomTypeName}"/> &times; ${r.roomsCount}</span></td>
            <td><fmt:formatDate value="${r.checkIn}" dateStyle="medium"/> &rarr; <fmt:formatDate value="${r.checkOut}" dateStyle="medium"/></td>
            <td>Rs. <fmt:formatNumber value="${r.totalAmount}" pattern="#,##0"/></td>
            <td class="muted"><fmt:message key="pay.${r.paymentMethod}"/></td>
            <td>
                <form action="${pageContext.request.contextPath}/admin/reservations" method="post" style="display:flex; gap:6px;">
                    <input type="hidden" name="id" value="${r.id}">
                    <input type="hidden" name="filter" value="${statusFilter}">
                    <select name="status" style="padding:5px; border-radius:6px;">
                        <c:forEach var="s" items="${statuses}">
                            <option value="${s}" ${r.status == s ? 'selected' : ''}><fmt:message key="status.${s}"/></option>
                        </c:forEach>
                    </select>
                    <button type="submit" class="btn" style="padding:5px 10px;"><fmt:message key="btn.save"/></button>
                </form>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty reservations}"><tr><td colspan="7" class="muted"><fmt:message key="admin.noReservations"/></td></tr></c:if>
    </tbody>
</table>

<%@ include file="../common/footer.jsp" %>
