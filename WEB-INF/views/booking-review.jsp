<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="title.bookReview" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="section-title"><fmt:message key="title.bookReview"/> <span class="muted">&middot; <fmt:message key="book.step2"/></span></h2>

<div style="display:flex; gap:24px; flex-wrap:wrap; align-items:flex-start;">
    <%@ include file="common/booking-summary.jspf" %>

    <div class="form-box" style="margin:0; flex:1; min-width:280px;">
        <h3 style="margin-top:0;"><fmt:message key="book.guestPayment"/></h3>
        <div class="summary-row"><span><fmt:message key="label.guest"/></span><strong><c:out value="${pending.guestName}"/></strong></div>
        <div class="summary-row"><span><fmt:message key="label.phone"/></span><strong><c:out value="${pending.guestPhone}"/></strong></div>
        <c:if test="${not empty pending.specialRequests}">
            <div class="summary-row"><span><fmt:message key="label.requests"/></span><span><c:out value="${pending.specialRequests}"/></span></div>
        </c:if>
        <div class="summary-row"><span><fmt:message key="label.payment"/></span><strong><fmt:message key="pay.${pending.paymentMethod}"/></strong></div>
        <c:if test="${not empty pending.cardLast4}">
            <div class="summary-row"><span><fmt:message key="label.card"/></span><strong>${pending.cardLast4}</strong></div>
        </c:if>

        <p class="muted" style="margin-top:14px;">
            <fmt:message key="book.freeCancel"/>
            <c:if test="${pending.paymentMethod == 'PAY_AT_HOTEL'}"><fmt:message key="book.confirmSoon"/></c:if>
        </p>

        <form action="${pageContext.request.contextPath}/book" method="post">
            <input type="hidden" name="action" value="confirm">
            <button type="submit" class="btn btn-primary btn-block"><fmt:message key="book.confirmBtn"/></button>
        </form>
        <a class="btn btn-block" style="margin-top:10px;" href="${pageContext.request.contextPath}/book"><fmt:message key="book.edit"/></a>
    </div>
</div>

<%@ include file="common/footer.jsp" %>
