<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="pageTitle" value="${hotel.name}" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="product-detail">
    <img src="${hotel.imageUrl}" alt="<c:out value='${hotel.name}'/>">
    <div class="info">
        <h1 style="margin:0 0 4px;"><c:out value="${hotel.name}"/></h1>
        <div class="stars"><c:forEach begin="1" end="${hotel.stars}">&#9733;</c:forEach></div>
        <p class="muted">&#128205; <c:out value="${hotel.address}"/></p>
        <div class="rating">&#11088; <fmt:formatNumber value="${hotel.rating}" pattern="0.0"/> &middot; <fmt:message key="hotel.reviewsN"><fmt:param value="${hotel.reviewCount}"/></fmt:message></div>
        <p><c:out value="${hotel.description}"/></p>
        <div>
            <c:forEach var="a" items="${hotel.amenityList}"><span class="chip"><c:out value="${a}"/></span></c:forEach>
        </div>
        <c:if test="${not hotel.active}"><p class="error-box" style="margin-top:12px;"><fmt:message key="hotel.hidden"/></p></c:if>
    </div>
</div>

<h2 class="section-title"><fmt:message key="hotel.chooseDates"/></h2>
<form class="stay-search" action="${pageContext.request.contextPath}/hotel" method="get">
    <input type="hidden" name="id" value="${hotel.id}">
    <div class="form-group">
        <label for="checkIn"><fmt:message key="label.checkIn"/></label>
        <input type="date" id="checkIn" name="checkIn" min="${today}" value="${checkIn}" required>
    </div>
    <div class="form-group">
        <label for="checkOut"><fmt:message key="label.checkOut"/></label>
        <input type="date" id="checkOut" name="checkOut" min="${today}" value="${checkOut}" required>
    </div>
    <div class="form-group" style="max-width:110px;">
        <label for="guests"><fmt:message key="label.guests"/></label>
        <input type="number" id="guests" name="guests" min="1" max="40" value="${guests}">
    </div>
    <button type="submit" class="btn"><fmt:message key="hotel.checkAvailability"/></button>
</form>

<h2 class="section-title">
    <fmt:message key="hotel.rooms"/>
    <span class="muted">&middot; <fmt:formatDate value="${checkIn}" dateStyle="medium"/> &rarr;
        <fmt:formatDate value="${checkOut}" dateStyle="medium"/> (<fmt:message key="label.nightsN"><fmt:param value="${nights}"/></fmt:message>)</span>
</h2>

<c:if test="${empty roomTypes}"><p><fmt:message key="hotel.noRooms"/></p></c:if>

<c:forEach var="rt" items="${roomTypes}">
    <div class="room-card">
        <img src="${rt.imageUrl}" alt="<c:out value='${rt.name}'/>">
        <div class="info">
            <h3 style="margin:0 0 4px;"><c:out value="${rt.name}"/></h3>
            <div class="muted"><c:out value="${rt.description}"/></div>
            <div class="muted">&#128101; <fmt:message key="hotel.upTo"><fmt:param value="${rt.capacity}"/></fmt:message></div>
            <c:choose>
                <c:when test="${rt.availableRooms == 0}"><span class="badge badge-cancelled"><fmt:message key="hotel.soldOut"/></span></c:when>
                <c:when test="${rt.availableRooms <= 3}"><span class="badge badge-pending"><fmt:message key="hotel.onlyLeft"><fmt:param value="${rt.availableRooms}"/></fmt:message></span></c:when>
                <c:otherwise><span class="badge badge-delivered"><fmt:message key="hotel.available"><fmt:param value="${rt.availableRooms}"/></fmt:message></span></c:otherwise>
            </c:choose>
        </div>
        <div class="book">
            <div class="price">Rs. <fmt:formatNumber value="${rt.pricePerNight}" pattern="#,##0"/><span class="muted"> / <fmt:message key="label.night"/></span></div>
            <div class="muted" style="margin-bottom:8px;">
                Rs. <fmt:formatNumber value="${rt.pricePerNight * nights}" pattern="#,##0"/> &middot; <fmt:message key="label.nightsN"><fmt:param value="${nights}"/></fmt:message>
            </div>
            <c:if test="${rt.availableRooms > 0}">
                <form action="${pageContext.request.contextPath}/book" method="get">
                    <input type="hidden" name="roomTypeId" value="${rt.id}">
                    <input type="hidden" name="checkIn" value="${checkIn}">
                    <input type="hidden" name="checkOut" value="${checkOut}">
                    <input type="hidden" name="guests" value="${guests}">
                    <select name="rooms" aria-label="Rooms">
                        <c:forEach begin="1" end="${rt.availableRooms > 10 ? 10 : rt.availableRooms}" var="n">
                            <option value="${n}"><fmt:message key="hotel.roomsN"><fmt:param value="${n}"/></fmt:message></option>
                        </c:forEach>
                    </select>
                    <button type="submit" class="btn btn-primary"><fmt:message key="btn.book"/></button>
                </form>
            </c:if>
        </div>
    </div>
</c:forEach>

<h2 class="section-title" id="reviews"><fmt:message key="hotel.reviews"/></h2>
<c:if test="${canReview}">
    <div class="form-box" style="margin:0 0 16px; max-width:none;">
        <form action="${pageContext.request.contextPath}/review" method="post">
            <input type="hidden" name="hotelId" value="${hotel.id}">
            <div class="form-group">
                <label for="rating"><fmt:message key="hotel.yourRating"/></label>
                <select id="rating" name="rating" required>
                    <option value="5">&#9733;&#9733;&#9733;&#9733;&#9733; <fmt:message key="rating.5"/></option>
                    <option value="4">&#9733;&#9733;&#9733;&#9733; <fmt:message key="rating.4"/></option>
                    <option value="3">&#9733;&#9733;&#9733; <fmt:message key="rating.3"/></option>
                    <option value="2">&#9733;&#9733; <fmt:message key="rating.2"/></option>
                    <option value="1">&#9733; <fmt:message key="rating.1"/></option>
                </select>
            </div>
            <div class="form-group">
                <label for="comment"><fmt:message key="hotel.comment"/></label>
                <textarea id="comment" name="comment" rows="3" maxlength="1000"></textarea>
            </div>
            <button type="submit" class="btn btn-primary"><fmt:message key="hotel.submitReview"/></button>
        </form>
    </div>
</c:if>
<c:if test="${empty reviews}"><p class="muted"><fmt:message key="hotel.noReviews"/></p></c:if>
<c:forEach var="rv" items="${reviews}">
    <div class="form-box" style="margin:0 0 10px; max-width:none; padding:14px 18px;">
        <strong><c:out value="${rv.userName}"/></strong>
        <span class="stars"><c:forEach begin="1" end="${rv.rating}">&#9733;</c:forEach></span>
        <span class="muted"> &middot; <fmt:formatDate value="${rv.createdAt}" dateStyle="medium"/></span>
        <p style="margin:6px 0 0;"><c:out value="${rv.commentText}"/></p>
    </div>
</c:forEach>

<script>
document.getElementById('checkIn').addEventListener('change', function () {
    var out = document.getElementById('checkOut');
    var next = new Date(this.value); next.setDate(next.getDate() + 1);
    var nextStr = next.toISOString().slice(0, 10);
    out.min = nextStr;
    if (out.value <= this.value) out.value = nextStr;
});
</script>

<%@ include file="common/footer.jsp" %>
