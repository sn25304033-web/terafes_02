<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="title.search" scope="request"/>
<%@ include file="common/header.jsp" %>

<form class="stay-search" action="${pageContext.request.contextPath}/search" method="get" style="margin-bottom:20px;">
    <div class="form-group" style="flex:2;">
        <label for="q2"><fmt:message key="label.destination"/></label>
        <input type="text" id="q2" name="q" value="<c:out value='${q}'/>" placeholder="<fmt:message key='label.cityOrHotel'/>">
    </div>
    <div class="form-group">
        <label for="checkIn"><fmt:message key="label.checkIn"/></label>
        <input type="date" id="checkIn" name="checkIn" min="${today}" value="${checkIn}">
    </div>
    <div class="form-group">
        <label for="checkOut"><fmt:message key="label.checkOut"/></label>
        <input type="date" id="checkOut" name="checkOut" min="${today}" value="${checkOut}">
    </div>
    <div class="form-group" style="max-width:110px;">
        <label for="guests"><fmt:message key="label.guests"/></label>
        <input type="number" id="guests" name="guests" min="1" max="20" value="${guests}">
    </div>
    <button type="submit" class="btn btn-primary"><fmt:message key="btn.search"/></button>
</form>

<h2 class="section-title">
    <fmt:message key="search.count"><fmt:param value="${hotels.size()}"/></fmt:message>
    <c:if test="${not empty q}"> &middot; &ldquo;<c:out value='${q}'/>&rdquo;</c:if>
    <c:if test="${not empty checkIn}">
        <span class="muted">&middot; <fmt:message key="search.available"/> <fmt:formatDate value="${checkIn}" dateStyle="medium"/> &rarr;
            <fmt:formatDate value="${checkOut}" dateStyle="medium"/></span>
    </c:if>
</h2>

<c:if test="${empty hotels}">
    <p><fmt:message key="search.none"/></p>
</c:if>

<c:forEach var="h" items="${hotels}">
    <div class="room-card">
        <img src="${h.imageUrl}" alt="<c:out value='${h.name}'/>">
        <div class="info">
            <h3 style="margin:0 0 4px;"><a href="${pageContext.request.contextPath}/hotel?id=${h.id}&checkIn=${checkIn}&checkOut=${checkOut}&guests=${guests}"><c:out value="${h.name}"/></a></h3>
            <div class="stars"><c:forEach begin="1" end="${h.stars}">&#9733;</c:forEach></div>
            <div class="muted">&#128205; <c:out value="${h.address}"/></div>
            <div style="margin-top:6px;">
                <c:forEach var="a" items="${h.amenityList}"><span class="chip"><c:out value="${a}"/></span></c:forEach>
            </div>
        </div>
        <div class="book">
            <div class="rating">&#11088; <fmt:formatNumber value="${h.rating}" pattern="0.0"/> (${h.reviewCount})</div>
            <c:if test="${not empty h.minPrice}">
                <div class="price">Rs. <fmt:formatNumber value="${h.minPrice}" pattern="#,##0"/><span class="muted"> / <fmt:message key="label.night"/></span></div>
            </c:if>
            <a class="btn btn-primary" style="margin-top:8px;"
               href="${pageContext.request.contextPath}/hotel?id=${h.id}&checkIn=${checkIn}&checkOut=${checkOut}&guests=${guests}"><fmt:message key="search.seeRooms"/></a>
        </div>
    </div>
</c:forEach>

<%@ include file="common/footer.jsp" %>
