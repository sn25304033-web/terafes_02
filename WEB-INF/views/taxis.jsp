<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="nav.taxis" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="section-title">&#128661; <fmt:message key="nav.taxis"/></h2>

<c:choose>
    <c:when test="${empty taxis}">
        <p><fmt:message key="taxi.none"/> <a href="${pageContext.request.contextPath}/reservations" style="color:var(--primary);"><fmt:message key="nav.reservations"/> &rarr;</a></p>
    </c:when>
    <c:otherwise>
        <c:forEach var="t" items="${taxis}">
            <div class="room-card">
                <div style="font-size:42px; width:70px; text-align:center;">${t.carType == 'VAN' ? '&#128656;' : (t.carType == 'PREMIUM' ? '&#128664;' : '&#128661;')}</div>
                <div class="info">
                    <h3 style="margin:0 0 4px;">#${t.id} &middot; <fmt:formatDate value="${t.pickupTime}" type="both" dateStyle="full" timeStyle="short"/></h3>
                    <div><strong><fmt:message key="taxi.pickup"/>:</strong> <fmt:message key="taxi.zone.${t.pickupType}"/> &mdash; <c:out value="${t.pickupLocation}"/></div>
                    <div><strong><fmt:message key="taxi.dropoff"/>:</strong> <c:out value="${t.hotelName}"/> <span class="muted">(<c:out value="${t.hotelAddress}"/>)</span></div>
                    <div class="muted"><fmt:message key="taxi.car.${t.carType}"/> &middot;
                        <fmt:message key="taxi.passengers"/>: ${t.passengers} &middot; <fmt:message key="taxi.luggage"/>: ${t.luggage}</div>
                    <c:if test="${not empty t.driverName}">
                        <div>&#128100; <fmt:message key="taxi.driver"/>: <strong><c:out value="${t.driverName}"/></strong>
                            <c:if test="${not empty t.driverPhone}"> &middot; &#128222; <c:out value="${t.driverPhone}"/></c:if></div>
                    </c:if>
                </div>
                <div class="book">
                    <span class="badge badge-${t.status.toLowerCase()}"><fmt:message key="taxiStatus.${t.status}"/></span>
                    <div class="price" style="margin:6px 0;">Rs. <fmt:formatNumber value="${t.estimatedFare}" pattern="#,##0"/></div>
                    <a class="btn" href="${pageContext.request.contextPath}/reservation?id=${t.reservationId}"><fmt:message key="label.reservationNo"/> #${t.reservationId}</a>
                    <c:if test="${t.cancellable}">
                        <form action="${pageContext.request.contextPath}/taxi" method="post" style="margin-top:6px;"
                              onsubmit="return confirm('<fmt:message key="taxi.cancelConfirm"/>');">
                            <input type="hidden" name="action" value="cancel">
                            <input type="hidden" name="id" value="${t.id}">
                            <button type="submit" class="btn btn-danger"><fmt:message key="taxi.cancelBtn"/></button>
                        </form>
                    </c:if>
                </div>
            </div>
        </c:forEach>
    </c:otherwise>
</c:choose>

<%@ include file="common/footer.jsp" %>
