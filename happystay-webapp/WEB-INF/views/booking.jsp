<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="title.book" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="section-title"><fmt:message key="title.book"/> <span class="muted">&middot; <fmt:message key="book.step1"/></span></h2>

<div style="display:flex; gap:24px; flex-wrap:wrap; align-items:flex-start;">
    <%@ include file="common/booking-summary.jspf" %>

    <div class="form-box" style="margin:0; flex:1; min-width:280px;">
        <h3 style="margin-top:0;"><fmt:message key="book.guestDetails"/></h3>
        <form action="${pageContext.request.contextPath}/book" method="post">
            <div class="form-group">
                <label for="guestName"><fmt:message key="book.guestName"/></label>
                <input type="text" id="guestName" name="guestName" required
                       value="<c:out value='${not empty pending.guestName ? pending.guestName : profile.fullName}'/>">
            </div>
            <div class="form-group">
                <label for="guestPhone"><fmt:message key="label.phone"/></label>
                <input type="text" id="guestPhone" name="guestPhone" required
                       value="<c:out value='${not empty pending.guestPhone ? pending.guestPhone : profile.phone}'/>">
            </div>
            <div class="form-group">
                <label for="specialRequests"><fmt:message key="book.requests"/></label>
                <textarea id="specialRequests" name="specialRequests" rows="2" maxlength="1000"
                          placeholder="<fmt:message key='book.requestsHint'/>"><c:out value="${pending.specialRequests}"/></textarea>
            </div>
            <div class="form-group">
                <label for="paymentMethod"><fmt:message key="label.payment"/></label>
                <select id="paymentMethod" name="paymentMethod" onchange="toggleCardFields()">
                    <option value="PAY_AT_HOTEL" ${empty pending.paymentMethod || pending.paymentMethod == 'PAY_AT_HOTEL' ? 'selected' : ''}><fmt:message key="pay.PAY_AT_HOTEL"/></option>
                    <option value="CREDIT_CARD" ${pending.paymentMethod == 'CREDIT_CARD' ? 'selected' : ''}><fmt:message key="pay.CREDIT_CARD"/></option>
                    <option value="PAYPAY" ${pending.paymentMethod == 'PAYPAY' ? 'selected' : ''}><fmt:message key="pay.PAYPAY"/></option>
                    <option value="BANK_TRANSFER" ${pending.paymentMethod == 'BANK_TRANSFER' ? 'selected' : ''}><fmt:message key="pay.BANK_TRANSFER"/></option>
                </select>
            </div>

            <div id="cardFields">
                <div class="form-group">
                    <label for="cardHolderName"><fmt:message key="card.holder"/></label>
                    <input type="text" id="cardHolderName" name="cardHolderName" value="<c:out value='${pending.cardHolderName}'/>" placeholder="<fmt:message key='card.holderHint'/>">
                </div>
                <div class="form-group">
                    <label for="cardNumber"><fmt:message key="card.number"/></label>
                    <input type="text" id="cardNumber" name="cardNumber" placeholder="1234 5678 9012 3456" maxlength="23" inputmode="numeric">
                </div>
                <div style="display:flex; gap:12px;">
                    <div class="form-group" style="flex:1;">
                        <label for="cardExpiry"><fmt:message key="card.expiry"/></label>
                        <input type="text" id="cardExpiry" name="cardExpiry" placeholder="08/29" maxlength="5">
                    </div>
                    <div class="form-group" style="flex:1;">
                        <label for="cardCvv">CVV</label>
                        <input type="password" id="cardCvv" name="cardCvv" placeholder="123" maxlength="4" inputmode="numeric">
                    </div>
                </div>
                <p style="font-size:12px; color:var(--text-muted); margin-top:-8px;">
                    <fmt:message key="card.demo"/>
                </p>
            </div>

            <button type="submit" class="btn btn-primary btn-block"><fmt:message key="book.reviewBtn"/></button>
        </form>
    </div>
</div>

<script>
function toggleCardFields() {
    var isCard = document.getElementById('paymentMethod').value === 'CREDIT_CARD';
    document.getElementById('cardFields').style.display = isCard ? 'block' : 'none';
    ['cardNumber', 'cardExpiry', 'cardCvv', 'cardHolderName'].forEach(function (id) {
        document.getElementById(id).required = isCard;
    });
}
document.getElementById('cardNumber').addEventListener('input', function (e) {
    var digits = e.target.value.replace(/\D/g, '').slice(0, 19);
    e.target.value = digits.replace(/(.{4})/g, '$1 ').trim();
});
document.getElementById('cardExpiry').addEventListener('input', function (e) {
    var digits = e.target.value.replace(/\D/g, '').slice(0, 4);
    e.target.value = digits.length > 2 ? digits.slice(0, 2) + '/' + digits.slice(2) : digits;
});
toggleCardFields();
</script>

<%@ include file="common/footer.jsp" %>
