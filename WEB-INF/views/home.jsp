<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="title.home" scope="request"/>
<%@ include file="common/header.jsp" %>

<div class="hero">
    <h1><fmt:message key="home.heroTitle"/></h1>
    <p><fmt:message key="home.heroText"/></p>
    <form class="stay-search" action="${pageContext.request.contextPath}/search" method="get">
        <div class="form-group" style="flex:2;">
            <label for="q"><fmt:message key="label.destination"/></label>
            <input type="text" id="q" name="q" placeholder="<fmt:message key='label.cityOrHotel'/>" list="cityList">
            <datalist id="cityList">
                <c:forEach var="city" items="${cities}"><option value="<c:out value='${city}'/>"></c:forEach>
            </datalist>
        </div>
        <div class="form-group">
            <label for="checkIn"><fmt:message key="label.checkIn"/></label>
            <input type="date" id="checkIn" name="checkIn" min="${today}" value="${defaultCheckIn}" required>
        </div>
        <div class="form-group">
            <label for="checkOut"><fmt:message key="label.checkOut"/></label>
            <input type="date" id="checkOut" name="checkOut" min="${today}" value="${defaultCheckOut}" required>
        </div>
        <div class="form-group" style="max-width:110px;">
            <label for="guests"><fmt:message key="label.guests"/></label>
            <input type="number" id="guests" name="guests" min="1" max="20" value="2">
        </div>
        <button type="submit" class="btn btn-primary"><fmt:message key="btn.search"/></button>
    </form>
</div>

<h2 class="section-title"><fmt:message key="home.topRated"/></h2>
<div class="product-grid">
    <c:forEach var="h" items="${topHotels}">
        <a class="product-card" href="${pageContext.request.contextPath}/hotel?id=${h.id}">
            <img src="${h.imageUrl}" alt="<c:out value='${h.name}'/>">
            <h3><c:out value="${h.name}"/></h3>
            <div class="muted">&#128205; <c:out value="${h.city}"/></div>
            <div class="stars"><c:forEach begin="1" end="${h.stars}">&#9733;</c:forEach></div>
            <div class="rating">&#11088; <fmt:formatNumber value="${h.rating}" pattern="0.0"/> (<fmt:message key="hotel.reviewsN"><fmt:param value="${h.reviewCount}"/></fmt:message>)</div>
            <c:if test="${not empty h.minPrice}">
                <div class="price" style="margin-top:auto;">
                    <span class="muted"><fmt:message key="label.from"/></span> Rs. <fmt:formatNumber value="${h.minPrice}" pattern="#,##0"/>
                    <span class="muted">/ <fmt:message key="label.night"/></span>
                </div>
            </c:if>
        </a>
    </c:forEach>
</div>

<script>
// Keep check-out after check-in.
document.getElementById('checkIn').addEventListener('change', function () {
    var out = document.getElementById('checkOut');
    var next = new Date(this.value); next.setDate(next.getDate() + 1);
    var nextStr = next.toISOString().slice(0, 10);
    out.min = nextStr;
    if (out.value <= this.value) out.value = nextStr;
});
</script>

<%@ include file="common/footer.jsp" %>
