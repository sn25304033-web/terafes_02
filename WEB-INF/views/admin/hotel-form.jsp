<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="${empty hotel.id or hotel.id == 0 ? 'admin.addHotel' : 'admin.editHotel'}" scope="request"/>
<%@ include file="../common/header.jsp" %>

<div class="form-box" style="max-width:640px;">
    <h2><fmt:message key="${titleKey}"/></h2>
    <form action="${pageContext.request.contextPath}/admin/hotels" method="post">
        <input type="hidden" name="action" value="save">
        <input type="hidden" name="id" value="${hotel.id}">
        <div class="form-group">
            <label for="name"><fmt:message key="admin.hotelName"/></label>
            <input type="text" id="name" name="name" value="<c:out value='${hotel.name}'/>" required maxlength="150">
        </div>
        <div style="display:flex; gap:12px;">
            <div class="form-group" style="flex:2;">
                <label for="city"><fmt:message key="label.city"/></label>
                <input type="text" id="city" name="city" value="<c:out value='${hotel.city}'/>" required maxlength="80">
            </div>
            <div class="form-group" style="flex:1;">
                <label for="stars"><fmt:message key="admin.stars"/></label>
                <select id="stars" name="stars">
                    <c:forEach begin="1" end="5" var="s">
                        <option value="${s}" ${(empty hotel and s == 3) or hotel.stars == s ? 'selected' : ''}>${s} &#9733;</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="form-group">
            <label for="address"><fmt:message key="label.address"/></label>
            <input type="text" id="address" name="address" value="<c:out value='${hotel.address}'/>" required maxlength="255">
        </div>
        <div class="form-group">
            <label for="description"><fmt:message key="admin.description"/></label>
            <textarea id="description" name="description" rows="4" maxlength="2000"><c:out value="${hotel.description}"/></textarea>
        </div>
        <div class="form-group">
            <label for="imageUrl"><fmt:message key="admin.imageUrl"/></label>
            <input type="url" id="imageUrl" name="imageUrl" value="<c:out value='${hotel.imageUrl}'/>" maxlength="500">
        </div>
        <div class="form-group">
            <label for="amenities"><fmt:message key="admin.amenities"/></label>
            <input type="text" id="amenities" name="amenities" value="<c:out value='${hotel.amenities}'/>" placeholder="Free WiFi,Pool,Parking" maxlength="500">
        </div>
        <div class="form-group">
            <label><input type="checkbox" name="active" style="width:auto;" ${empty hotel or hotel.active ? 'checked' : ''}> <fmt:message key="admin.visibleToGuests"/></label>
        </div>
        <button type="submit" class="btn btn-primary"><fmt:message key="btn.save"/></button>
        <a class="btn" href="${pageContext.request.contextPath}/admin/hotels"><fmt:message key="btn.cancel"/></a>
    </form>
</div>

<%@ include file="../common/footer.jsp" %>
