<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<c:set var="titleKey" value="nav.notifications" scope="request"/>
<%@ include file="common/header.jsp" %>

<h2 class="section-title"><fmt:message key="nav.notifications"/></h2>

<c:choose>
    <c:when test="${empty notifications}">
        <p><fmt:message key="notif.none"/></p>
    </c:when>
    <c:otherwise>
        <form action="${pageContext.request.contextPath}/notifications" method="post"
              onsubmit="return confirm('<fmt:message key="notif.clearConfirm"/>');" style="margin-bottom:14px;">
            <input type="hidden" name="action" value="deleteAll">
            <button type="submit" class="btn btn-danger"><fmt:message key="notif.clearAll"/></button>
        </form>
        <div style="display:flex; flex-direction:column; gap:10px;">
            <c:forEach var="n" items="${notifications}">
                <div class="form-box" style="margin:0; max-width:none; padding:16px 20px;
                            display:flex; justify-content:space-between; align-items:center; gap:16px;">
                    <div>
                        <p style="margin:0;">
                            <c:choose>
                                <c:when test="${not empty n.link}">
                                    <a href="${pageContext.request.contextPath}${n.link}"><c:out value="${n.message}"/></a>
                                </c:when>
                                <c:otherwise><c:out value="${n.message}"/></c:otherwise>
                            </c:choose>
                        </p>
                        <p class="muted" style="margin:4px 0 0; font-size:12px;">
                            <fmt:formatDate value="${n.createdAt}" type="both" dateStyle="medium" timeStyle="short"/>
                        </p>
                    </div>
                    <form action="${pageContext.request.contextPath}/notifications" method="post">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${n.id}">
                        <button type="submit" class="btn btn-danger"><fmt:message key="btn.delete"/></button>
                    </form>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="common/footer.jsp" %>
