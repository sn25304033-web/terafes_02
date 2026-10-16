<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="pageTitle" value="#${r.id} ${r.hotelName}" scope="request"/>
<%@ include file="common/header.jsp" %>

<c:if test="${justPlaced}">
    <div class="flash" style="font-size:16px;">
        &#127881; <fmt:message key="res.placed"><fmt:param value="${r.id}"/><fmt:param><c:out value="${r.userEmail}"/></fmt:param></fmt:message>
    </div>
</c:if>

<h2 class="section-title"><fmt:message key="label.reservationNo"/> #${r.id}
    <span class="badge badge-${r.status.toLowerCase()}"><fmt:message key="status.${r.status}"/></span></h2>

<div style="display:flex; gap:24px; flex-wrap:wrap; align-items:flex-start;">
    <div class="form-box" style="margin:0; flex:1; min-width:280px; max-width:none;">
        <img src="${r.imageUrl}" alt="" style="width:100%; height:180px; object-fit:cover; border-radius:10px;">
        <h3 style="margin:12px 0 2px;"><a href="${pageContext.request.contextPath}/hotel?id=${r.hotelId}"><c:out value="${r.hotelName}"/></a></h3>
        <div class="muted" style="margin-bottom:10px;">&#128205; <c:out value="${r.hotelAddress}"/></div>
        <div class="summary-row"><span><fmt:message key="label.room"/></span><strong><c:out value="${r.roomTypeName}"/> &times; ${r.roomsCount}</strong></div>
        <div class="summary-row"><span><fmt:message key="label.checkIn"/></span><strong><fmt:formatDate value="${r.checkIn}" dateStyle="full"/></strong></div>
        <div class="summary-row"><span><fmt:message key="label.checkOut"/></span><strong><fmt:formatDate value="${r.checkOut}" dateStyle="full"/></strong></div>
        <div class="summary-row"><span><fmt:message key="label.nights"/></span><strong>${r.nights}</strong></div>
        <div class="summary-row"><span><fmt:message key="label.guests"/></span><strong>${r.guests}</strong></div>
        <h3 style="text-align:right; margin:10px 0 0;"><fmt:message key="label.total"/>: <span class="price">Rs. <fmt:formatNumber value="${r.totalAmount}" pattern="#,##0.00"/></span></h3>
    </div>

    <div class="form-box" style="margin:0; flex:1; min-width:280px;">
        <h3 style="margin-top:0;"><fmt:message key="book.guestPayment"/></h3>
        <div class="summary-row"><span><fmt:message key="label.guest"/></span><strong><c:out value="${r.guestName}"/></strong></div>
        <div class="summary-row"><span><fmt:message key="label.phone"/></span><strong><c:out value="${r.guestPhone}"/></strong></div>
        <c:if test="${not empty r.specialRequests}">
            <div class="summary-row"><span><fmt:message key="label.requests"/></span><span><c:out value="${r.specialRequests}"/></span></div>
        </c:if>
        <div class="summary-row"><span><fmt:message key="label.payment"/></span><strong><fmt:message key="pay.${r.paymentMethod}"/></strong></div>
        <c:if test="${not empty r.cardLast4}"><div class="summary-row"><span><fmt:message key="label.card"/></span><strong>${r.cardLast4}</strong></div></c:if>
        <div class="summary-row"><span><fmt:message key="label.bookedOn"/></span><span><fmt:formatDate value="${r.createdAt}" dateStyle="medium" timeStyle="short" type="both"/></span></div>

        <c:if test="${r.cancellable and r.userId == sessionScope.userId}">
            <form action="${pageContext.request.contextPath}/reservation" method="post" style="margin-top:16px;"
                  onsubmit="return confirm('<fmt:message key="res.cancelConfirm"/>');">
                <input type="hidden" name="action" value="cancel">
                <input type="hidden" name="id" value="${r.id}">
                <button type="submit" class="btn btn-danger btn-block"><fmt:message key="res.cancelBtn"/></button>
            </form>
            <p class="muted" style="margin:6px 0 0;"><fmt:message key="res.cancelNote"/></p>
        </c:if>
        <a class="btn btn-block" style="margin-top:10px;" href="${pageContext.request.contextPath}/reservations">&larr; <fmt:message key="res.all"/></a>
    </div>
</div>

<%-- ===== Taxi to the hotel ===== --%>
<h2 class="section-title">&#128661; <fmt:message key="taxi.toHotel"/></h2>
<c:forEach var="t" items="${taxis}">
    <div class="summary-row" style="background:var(--surface); border:1px solid var(--border); border-radius:10px; padding:10px 14px; margin-bottom:8px;">
        <span>#${t.id} &middot; <fmt:formatDate value="${t.pickupTime}" type="both" dateStyle="medium" timeStyle="short"/>
            &middot; <fmt:message key="taxi.car.${t.carType}"/> &middot; <c:out value="${t.pickupLocation}"/></span>
        <span class="badge badge-${t.status.toLowerCase()}"><fmt:message key="taxiStatus.${t.status}"/></span>
    </div>
</c:forEach>
<c:choose>
    <c:when test="${r.taxiBookable and r.userId == sessionScope.userId}">
        <p class="muted"><fmt:message key="taxi.promo"/></p>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/taxi?reservationId=${r.id}">&#128661; <fmt:message key="taxi.bookBtn"/></a>
    </c:when>
    <c:when test="${empty taxis}"><p class="muted"><fmt:message key="taxi.noneForRes"/></p></c:when>
</c:choose>

<%@ include file="common/footer.jsp" %>
