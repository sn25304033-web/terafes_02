<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="admin.dashboard" scope="request"/>
<%@ include file="../common/header.jsp" %>

<h2 class="section-title"><fmt:message key="admin.dashboard"/></h2>
<%@ include file="nav.jspf" %>

<div style="display:flex;gap:16px;flex-wrap:wrap;margin-bottom:24px;">
    <div class="form-box" style="margin:0;flex:1;min-width:170px;text-align:center;">
        <h3>${hotelCount}</h3><p><fmt:message key="admin.hotels"/></p>
    </div>
    <div class="form-box" style="margin:0;flex:1;min-width:170px;text-align:center;">
        <h3>${reservationCount}</h3><p><fmt:message key="admin.totalReservations"/></p>
    </div>
    <div class="form-box" style="margin:0;flex:1;min-width:170px;text-align:center;">
        <h3><a href="${pageContext.request.contextPath}/admin/reservations?status=PENDING">${pendingCount}</a></h3><p><fmt:message key="admin.awaiting"/></p>
    </div>
    <div class="form-box" style="margin:0;flex:1;min-width:170px;text-align:center;">
        <h3><a href="${pageContext.request.contextPath}/admin/taxis?status=REQUESTED">${taxiRequestedCount}</a></h3><p><fmt:message key="admin.taxiRequests"/></p>
    </div>
    <div class="form-box" style="margin:0;flex:1;min-width:170px;text-align:center;">
        <h3>Rs. <fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/></h3><p><fmt:message key="admin.revenue"/></p>
    </div>
</div>

<h3 class="section-title"><fmt:message key="admin.arrivals"/></h3>
<c:choose>
    <c:when test="${empty arrivalsToday}"><p class="muted"><fmt:message key="admin.noArrivals"/></p></c:when>
    <c:otherwise>
        <table class="data-table">
            <thead><tr><th>#</th><th><fmt:message key="label.guest"/></th><th><fmt:message key="admin.hotelRoom"/></th><th><fmt:message key="label.nights"/></th><th><fmt:message key="label.status"/></th></tr></thead>
            <tbody>
            <c:forEach var="r" items="${arrivalsToday}">
                <tr>
                    <td><a href="${pageContext.request.contextPath}/reservation?id=${r.id}">#${r.id}</a></td>
                    <td><c:out value="${r.guestName}"/><br><span class="muted"><c:out value="${r.guestPhone}"/></span></td>
                    <td><c:out value="${r.hotelName}"/><br><span class="muted"><c:out value="${r.roomTypeName}"/> &times; ${r.roomsCount}</span></td>
                    <td>${r.nights}</td>
                    <td><span class="badge badge-${r.status.toLowerCase()}"><fmt:message key="status.${r.status}"/></span></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

<h3 class="section-title"><fmt:message key="admin.recent"/></h3>
<table class="data-table">
    <thead><tr><th>#</th><th><fmt:message key="label.bookedOn"/></th><th><fmt:message key="label.hotel"/></th><th><fmt:message key="admin.stay"/></th><th><fmt:message key="label.total"/></th><th><fmt:message key="label.status"/></th></tr></thead>
    <tbody>
        <c:forEach var="r" items="${recentReservations}">
            <tr>
                <td><a href="${pageContext.request.contextPath}/reservation?id=${r.id}">#${r.id}</a></td>
                <td><fmt:formatDate value="${r.createdAt}" type="both" dateStyle="medium" timeStyle="short"/></td>
                <td><c:out value="${r.hotelName}"/></td>
                <td><fmt:formatDate value="${r.checkIn}" dateStyle="medium"/> &rarr; <fmt:formatDate value="${r.checkOut}" dateStyle="medium"/></td>
                <td>Rs. <fmt:formatNumber value="${r.totalAmount}" pattern="#,##0"/></td>
                <td><span class="badge badge-${r.status.toLowerCase()}"><fmt:message key="status.${r.status}"/></span></td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<%@ include file="../common/footer.jsp" %>
