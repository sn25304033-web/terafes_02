<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="admin.roomTypes" scope="request"/>
<%@ include file="../common/header.jsp" %>

<h2 class="section-title"><fmt:message key="admin.roomTypes"/> &middot; <c:out value="${hotel.name}"/></h2>
<%@ include file="nav.jspf" %>

<table class="data-table" style="margin-bottom:24px;">
    <thead><tr><th><fmt:message key="label.room"/></th><th><fmt:message key="admin.pricePerNight"/></th><th><fmt:message key="admin.capacity"/></th><th><fmt:message key="admin.totalRooms"/></th><th></th></tr></thead>
    <tbody>
    <c:forEach var="rt" items="${roomTypes}">
        <tr>
            <td><strong><c:out value="${rt.name}"/></strong><br><span class="muted"><c:out value="${rt.description}"/></span></td>
            <td>Rs. <fmt:formatNumber value="${rt.pricePerNight}" pattern="#,##0"/></td>
            <td>${rt.capacity}</td>
            <td>${rt.totalRooms}</td>
            <td style="white-space:nowrap;">
                <a class="btn" href="${pageContext.request.contextPath}/admin/rooms?hotelId=${hotel.id}&editId=${rt.id}#roomForm"><fmt:message key="btn.edit"/></a>
                <form action="${pageContext.request.contextPath}/admin/rooms" method="post" style="display:inline;"
                      onsubmit="return confirm('<fmt:message key="admin.deleteRoomConfirm"/>');">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="hotelId" value="${hotel.id}">
                    <input type="hidden" name="id" value="${rt.id}">
                    <button type="submit" class="btn btn-danger"><fmt:message key="btn.delete"/></button>
                </form>
            </td>
        </tr>
    </c:forEach>
    <c:if test="${empty roomTypes}"><tr><td colspan="5" class="muted"><fmt:message key="admin.noRoomTypes"/></td></tr></c:if>
    </tbody>
</table>

<div class="form-box" id="roomForm" style="margin:0; max-width:640px;">
    <h3 style="margin-top:0;"><fmt:message key="${empty editRoom ? 'admin.addRoomType' : 'admin.editRoomType'}"/></h3>
    <form action="${pageContext.request.contextPath}/admin/rooms" method="post">
        <input type="hidden" name="action" value="save">
        <input type="hidden" name="hotelId" value="${hotel.id}">
        <input type="hidden" name="id" value="${editRoom.id}">
        <div class="form-group">
            <label for="name"><fmt:message key="admin.roomName"/></label>
            <input type="text" id="name" name="name" value="<c:out value='${editRoom.name}'/>" required maxlength="100" placeholder="Deluxe Double">
        </div>
        <div class="form-group">
            <label for="description"><fmt:message key="admin.description"/></label>
            <input type="text" id="description" name="description" value="<c:out value='${editRoom.description}'/>" maxlength="1000">
        </div>
        <div style="display:flex; gap:12px;">
            <div class="form-group" style="flex:1;">
                <label for="pricePerNight"><fmt:message key="admin.pricePerNight"/> (Rs.)</label>
                <input type="number" id="pricePerNight" name="pricePerNight" min="0" step="0.01" value="${editRoom.pricePerNight}" required>
            </div>
            <div class="form-group" style="flex:1;">
                <label for="capacity"><fmt:message key="admin.capacity"/></label>
                <input type="number" id="capacity" name="capacity" min="1" max="20" value="${empty editRoom ? 2 : editRoom.capacity}" required>
            </div>
            <div class="form-group" style="flex:1;">
                <label for="totalRooms"><fmt:message key="admin.totalRooms"/></label>
                <input type="number" id="totalRooms" name="totalRooms" min="0" max="9999" value="${empty editRoom ? 1 : editRoom.totalRooms}" required>
            </div>
        </div>
        <div class="form-group">
            <label for="imageUrl"><fmt:message key="admin.imageUrl"/></label>
            <input type="url" id="imageUrl" name="imageUrl" value="<c:out value='${editRoom.imageUrl}'/>" maxlength="500">
        </div>
        <button type="submit" class="btn btn-primary"><fmt:message key="btn.save"/></button>
        <c:if test="${not empty editRoom}">
            <a class="btn" href="${pageContext.request.contextPath}/admin/rooms?hotelId=${hotel.id}"><fmt:message key="btn.cancel"/></a>
        </c:if>
    </form>
</div>

<%@ include file="../common/footer.jsp" %>
