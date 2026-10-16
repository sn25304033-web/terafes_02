<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="taxi.bookTitle" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="section-title">&#128661; <fmt:message key="taxi.bookTitle"/></h2>

<div style="display:flex; gap:24px; flex-wrap:wrap; align-items:flex-start;">
    <div class="form-box" style="margin:0; flex:1; min-width:260px; max-width:none;">
        <h3 style="margin-top:0;"><fmt:message key="taxi.dropoff"/></h3>
        <p style="margin:0;"><strong><c:out value="${r.hotelName}"/></strong></p>
        <p class="muted" style="margin:4px 0 12px;">&#128205; <c:out value="${r.hotelAddress}"/></p>
        <div class="summary-row"><span><fmt:message key="label.reservationNo"/></span><strong>#${r.id}</strong></div>
        <div class="summary-row"><span><fmt:message key="label.checkIn"/></span><strong><fmt:formatDate value="${r.checkIn}" dateStyle="full"/></strong></div>
        <div class="summary-row"><span><fmt:message key="label.guests"/></span><strong>${r.guests}</strong></div>
        <p class="muted" style="margin-top:14px;"><fmt:message key="taxi.fareRule"/></p>
        <table class="data-table">
            <tr><th></th><th><fmt:message key="taxi.zone.AIRPORT"/></th><th><fmt:message key="taxi.zone.STATION"/></th><th><fmt:message key="taxi.zone.OTHER"/></th></tr>
            <c:forEach var="car" items="${['STANDARD','PREMIUM','VAN']}">
                <tr>
                    <td><fmt:message key="taxi.car.${car}"/></td>
                    <td>Rs. <fmt:formatNumber value="${carBase[car] * zoneMultiplier['AIRPORT']}" pattern="#,##0"/></td>
                    <td>Rs. <fmt:formatNumber value="${carBase[car] * zoneMultiplier['STATION']}" pattern="#,##0"/></td>
                    <td>Rs. <fmt:formatNumber value="${carBase[car] * zoneMultiplier['OTHER']}" pattern="#,##0"/></td>
                </tr>
            </c:forEach>
        </table>
    </div>

    <div class="form-box" style="margin:0; flex:1.3; min-width:300px; max-width:none;">
        <form action="${pageContext.request.contextPath}/taxi" method="post" id="taxiForm">
            <input type="hidden" name="action" value="book">
            <input type="hidden" name="reservationId" value="${r.id}">

            <div class="form-group">
                <label for="pickupType"><fmt:message key="taxi.pickupFrom"/></label>
                <select id="pickupType" name="pickupType" onchange="updateFare()">
                    <c:forEach var="z" items="${['AIRPORT','STATION','OTHER']}">
                        <option value="${z}" ${form.pickupType == z ? 'selected' : ''}><fmt:message key="taxi.zone.${z}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="form-group">
                <label for="pickupLocation"><fmt:message key="taxi.pickupLocation"/></label>
                <input type="text" id="pickupLocation" name="pickupLocation" required maxlength="255"
                       value="<c:out value='${form.pickupLocation}'/>" placeholder="<fmt:message key='taxi.locationHint'/>">
            </div>
            <div class="form-group">
                <label for="pickupTime"><fmt:message key="taxi.pickupTime"/></label>
                <input type="datetime-local" id="pickupTime" name="pickupTime" required
                       min="${minPickup}" max="${maxPickup}" value="${defaultPickup}">
            </div>

            <div class="form-group">
                <label><fmt:message key="taxi.car"/></label>
                <div class="car-options">
                    <c:forEach var="car" items="${['STANDARD','PREMIUM','VAN']}">
                        <label class="car-option" data-car="${car}">
                            <input type="radio" name="carType" value="${car}"
                                   ${form.carType == car or (empty form and car == 'STANDARD') ? 'checked' : ''} onchange="updateFare()">
                            <div class="icon">${car == 'VAN' ? '&#128656;' : (car == 'PREMIUM' ? '&#128664;' : '&#128661;')}</div>
                            <div><strong><fmt:message key="taxi.car.${car}"/></strong></div>
                            <div class="muted"><fmt:message key="taxi.seatsN"><fmt:param value="${carSeats[car]}"/></fmt:message></div>
                        </label>
                    </c:forEach>
                </div>
            </div>

            <div style="display:flex; gap:12px;">
                <div class="form-group" style="flex:1;">
                    <label for="passengers"><fmt:message key="taxi.passengers"/></label>
                    <input type="number" id="passengers" name="passengers" min="1" max="7"
                           value="${empty form ? (r.guests > 7 ? 7 : r.guests) : form.passengers}" required>
                </div>
                <div class="form-group" style="flex:1;">
                    <label for="luggage"><fmt:message key="taxi.luggage"/></label>
                    <input type="number" id="luggage" name="luggage" min="0" max="20" value="${empty form ? 1 : form.luggage}">
                </div>
            </div>
            <div class="form-group">
                <label for="notes"><fmt:message key="taxi.notes"/></label>
                <textarea id="notes" name="notes" rows="2" maxlength="500" placeholder="<fmt:message key='taxi.notesHint'/>"><c:out value="${form.notes}"/></textarea>
            </div>

            <div class="fare-box"><fmt:message key="taxi.fare"/>: Rs. <span id="fare">-</span></div>
            <button type="submit" class="btn btn-primary btn-block"><fmt:message key="taxi.confirmBtn"/></button>
            <p class="muted" style="margin-top:8px;"><fmt:message key="taxi.payNote"/></p>
        </form>
    </div>
</div>

<script>
var BASE = { STANDARD: ${carBase['STANDARD']}, PREMIUM: ${carBase['PREMIUM']}, VAN: ${carBase['VAN']} };
var MULT = { AIRPORT: ${zoneMultiplier['AIRPORT']}, STATION: ${zoneMultiplier['STATION']}, OTHER: ${zoneMultiplier['OTHER']} };
var SEATS = { STANDARD: ${carSeats['STANDARD']}, PREMIUM: ${carSeats['PREMIUM']}, VAN: ${carSeats['VAN']} };
function updateFare() {
    var car = (document.querySelector('input[name=carType]:checked') || {}).value || 'STANDARD';
    var zone = document.getElementById('pickupType').value;
    document.getElementById('fare').textContent = Math.round(BASE[car] * MULT[zone]).toLocaleString();
    document.getElementById('passengers').max = SEATS[car];
    document.querySelectorAll('.car-option').forEach(function (el) {
        el.classList.toggle('selected', el.getAttribute('data-car') === car);
    });
}
updateFare();
</script>

<%@ include file="common/footer.jsp" %>
