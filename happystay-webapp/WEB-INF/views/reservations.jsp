<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="nav.reservations" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="section-title"><fmt:message key="nav.reservations"/></h2>

<c:choose>
    <c:when test="${empty reservations}">
        <p><fmt:message key="res.none"/> <a href="${pageContext.request.contextPath}/home" style="color:var(--primary);"><fmt:message key="res.findHotel"/> &rarr;</a></p>
    </c:when>
    <c:otherwise>
        <c:forEach var="r" items="${reservations}">
            <div class="room-card">
                <img src="${r.imageUrl}" alt="" style="width:150px; height:100px;">
                <div class="info">
                    <h3 style="margin:0 0 2px;"><c:out value="${r.hotelName}"/> <span class="muted">&middot; <c:out value="${r.hotelCity}"/></span></h3>
                    <div class="muted"><c:out value="${r.roomTypeName}"/> &times; ${r.roomsCount} &middot;
                        <fmt:message key="label.guestsN"><fmt:param value="${r.guests}"/></fmt:message></div>
                    <div>
                        <fmt:formatDate value="${r.checkIn}" dateStyle="medium"/> &rarr;
                        <fmt:formatDate value="${r.checkOut}" dateStyle="medium"/>
                        (<fmt:message key="label.nightsN"><fmt:param value="${r.nights}"/></fmt:message>)
                    </div>
                </div>
                <div class="book">
                    <span class="badge badge-${r.status.toLowerCase()}"><fmt:message key="status.${r.status}"/></span>
                    <div class="price" style="margin:6px 0;">Rs. <fmt:formatNumber value="${r.totalAmount}" pattern="#,##0.00"/></div>
                    <a class="btn" href="${pageContext.request.contextPath}/reservation?id=${r.id}"><fmt:message key="btn.view"/> #${r.id}</a>
                    <c:if test="${r.taxiBookable}">
                        <a class="btn" href="${pageContext.request.contextPath}/taxi?reservationId=${r.id}">&#128661; <fmt:message key="taxi.bookBtn"/></a>
                    </c:if>
                </div>
            </div>
        </c:forEach>
    </c:otherwise>
</c:choose>

<%@ include file="common/footer.jsp" %>
