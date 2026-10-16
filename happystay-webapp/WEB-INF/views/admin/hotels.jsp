<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="admin.hotelsRooms" scope="request"/>
<%@ include file="../common/header.jsp" %>

<h2 class="section-title"><fmt:message key="admin.hotelsRooms"/></h2>
<%@ include file="nav.jspf" %>

<p><a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/hotels?action=new">+ <fmt:message key="admin.addHotel"/></a></p>

<table class="data-table">
    <thead><tr><th>ID</th><th><fmt:message key="label.hotel"/></th><th><fmt:message key="label.city"/></th><th><fmt:message key="admin.stars"/></th><th><fmt:message key="admin.rating"/></th><th><fmt:message key="label.from"/></th><th><fmt:message key="label.status"/></th><th></th></tr></thead>
    <tbody>
    <c:forEach var="h" items="${hotels}">
        <tr>
            <td>${h.id}</td>
            <td><a href="${pageContext.request.contextPath}/hotel?id=${h.id}"><c:out value="${h.name}"/></a></td>
            <td><c:out value="${h.city}"/></td>
            <td class="stars"><c:forEach begin="1" end="${h.stars}">&#9733;</c:forEach></td>
            <td><fmt:formatNumber value="${h.rating}" pattern="0.0"/></td>
            <td><c:if test="${not empty h.minPrice}">Rs. <fmt:formatNumber value="${h.minPrice}" pattern="#,##0"/></c:if></td>
            <td><span class="badge ${h.active ? 'badge-delivered' : 'badge-cancelled'}"><fmt:message key="${h.active ? 'admin.visible' : 'admin.hidden'}"/></span></td>
            <td style="white-space:nowrap;">
                <a class="btn" href="${pageContext.request.contextPath}/admin/hotels?action=edit&id=${h.id}"><fmt:message key="btn.edit"/></a>
                <a class="btn" href="${pageContext.request.contextPath}/admin/rooms?hotelId=${h.id}"><fmt:message key="hotel.rooms"/></a>
                <form action="${pageContext.request.contextPath}/admin/hotels" method="post" style="display:inline;">
                    <input type="hidden" name="action" value="toggle">
                    <input type="hidden" name="id" value="${h.id}">
                    <button type="submit" class="btn ${h.active ? 'btn-danger' : ''}"><fmt:message key="${h.active ? 'admin.hide' : 'admin.show'}"/></button>
                </form>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<%@ include file="../common/footer.jsp" %>
