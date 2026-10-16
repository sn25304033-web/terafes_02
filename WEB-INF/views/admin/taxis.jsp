<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="admin.taxis" scope="request"/>
<%@ include file="../common/header.jsp" %>

<h2 class="section-title">&#128661; <fmt:message key="admin.taxis"/></h2>
<%@ include file="nav.jspf" %>

<div style="margin-bottom:14px; display:flex; gap:8px; flex-wrap:wrap;">
    <a class="chip" href="${pageContext.request.contextPath}/admin/taxis"
       style="${empty statusFilter ? 'background:var(--primary);color:#fff;' : ''}"><fmt:message key="admin.all"/></a>
    <c:forEach var="s" items="${statuses}">
        <a class="chip" href="${pageContext.request.contextPath}/admin/taxis?status=${s}"
           style="${statusFilter == s ? 'background:var(--primary);color:#fff;' : ''}"><fmt:message key="taxiStatus.${s}"/></a>
    </c:forEach>
</div>

<table class="data-table">
    <thead><tr><th>#</th><th><fmt:message key="taxi.pickupTime"/></th><th><fmt:message key="label.guest"/></th>
        <th><fmt:message key="taxi.pickup"/> &rarr; <fmt:message key="taxi.dropoff"/></th><th><fmt:message key="taxi.car"/></th>
        <th><fmt:message key="taxi.fare"/></th><th><fmt:message key="admin.statusDriver"/></th></tr></thead>
    <tbody>
    <c:forEach var="t" items="${taxis}">
        <tr>
            <td>#${t.id}<br><a class="muted" href="${pageContext.request.contextPath}/reservation?id=${t.reservationId}">R#${t.reservationId}</a></td>
            <td><fmt:formatDate value="${t.pickupTime}" type="both" dateStyle="medium" timeStyle="short"/></td>
            <td><c:out value="${t.guestName}"/><br><span class="muted"><c:out value="${t.guestPhone}"/></span></td>
            <td><fmt:message key="taxi.zone.${t.pickupType}"/>: <c:out value="${t.pickupLocation}"/><br>
                &rarr; <c:out value="${t.hotelName}"/>
                <c:if test="${not empty t.notes}"><br><span class="muted">&#128221; <c:out value="${t.notes}"/></span></c:if></td>
            <td><fmt:message key="taxi.car.${t.carType}"/><br><span class="muted">&#128101; ${t.passengers} &middot; &#129523; ${t.luggage}</span></td>
            <td>Rs. <fmt:formatNumber value="${t.estimatedFare}" pattern="#,##0"/></td>
            <td>
                <form action="${pageContext.request.contextPath}/admin/taxis" method="post" style="display:flex; flex-direction:column; gap:4px; min-width:170px;">
                    <input type="hidden" name="id" value="${t.id}">
                    <input type="hidden" name="filter" value="${statusFilter}">
                    <select name="status" style="padding:5px; border-radius:6px;">
                        <c:forEach var="s" items="${statuses}">
                            <option value="${s}" ${t.status == s ? 'selected' : ''}><fmt:message key="taxiStatus.${s}"/></option>
                        </c:forEach>
                    </select>
                    <input type="text" name="driverName" value="<c:out value='${t.driverName}'/>" placeholder="<fmt:message key='taxi.driver'/>" maxlength="100">
                    <input type="text" name="driverPhone" value="<c:out value='${t.driverPhone}'/>" placeholder="<fmt:message key='label.phone'/>" maxlength="20">
                    <button type="submit" class="btn" style="padding:5px 10px;"><fmt:message key="btn.save"/></button>
                </form>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty taxis}"><tr><td colspan="7" class="muted"><fmt:message key="taxi.none"/></td></tr></c:if>
    </tbody>
</table>

<%@ include file="../common/footer.jsp" %>
