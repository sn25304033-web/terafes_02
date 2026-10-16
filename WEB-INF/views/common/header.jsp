<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="${lang}" data-theme="${cookie.theme.value == 'dark' ? 'dark' : (cookie.theme.value == 'light' ? 'light' : '')}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:choose><c:when test="${not empty pageTitle}"><c:out value="${pageTitle}"/> - </c:when><c:when test="${not empty titleKey}"><fmt:message key="${titleKey}"/> - </c:when></c:choose>HappyStay</title>
    <script>
        // Dark mode: "theme" cookie = light | dark; no cookie = follow the operating system.
        (function () {
            var root = document.documentElement;
            if (!root.getAttribute('data-theme')) {
                var dark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
                root.setAttribute('data-theme', dark ? 'dark' : 'light');
            }
        })();
        function setTheme(mode) {
            var root = document.documentElement;
            if (mode === 'system') {
                document.cookie = 'theme=; path=/; max-age=0; SameSite=Lax';
                var dark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
                root.setAttribute('data-theme', dark ? 'dark' : 'light');
            } else {
                document.cookie = 'theme=' + mode + '; path=/; max-age=31536000; SameSite=Lax';
                root.setAttribute('data-theme', mode);
            }
            var btn = document.getElementById('themeToggle');
            if (btn) btn.textContent = root.getAttribute('data-theme') === 'dark' ? '☀' : '☾';
        }
        function toggleTheme() {
            setTheme(document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark');
        }
    </script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700;800&family=Noto+Sans+JP:wght@400;700&family=Noto+Sans+Devanagari:wght@400;700&family=Noto+Sans+Sinhala:wght@400;700&family=Noto+Sans+Myanmar:wght@400;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        body { font-family: "Poppins", "Noto Sans JP", "Noto Sans Devanagari", "Noto Sans Sinhala", "Noto Sans Myanmar", "Segoe UI", Arial, sans-serif; }
    </style>
</head>
<body>

<header class="site-header">
    <a class="logo" href="${pageContext.request.contextPath}/home">Happy<span>Stay</span></a>

    <form class="search-form" action="${pageContext.request.contextPath}/search" method="get">
        <input type="text" name="q" placeholder="<fmt:message key='nav.searchPlaceholder'/>" value="<c:out value='${param.q}'/>">
        <button type="submit"><fmt:message key="btn.search"/></button>
    </form>

    <div class="header-links">
        <c:choose>
            <c:when test="${not empty sessionScope.userId}">
                <span><fmt:message key="nav.hi"><fmt:param><c:out value="${sessionScope.userName}"/></fmt:param></fmt:message></span>
                <c:if test="${sessionScope.userRole == 'ADMIN'}">
                    <a href="${pageContext.request.contextPath}/admin"><fmt:message key="nav.admin"/></a>
                </c:if>
                <a href="${pageContext.request.contextPath}/reservations"><fmt:message key="nav.reservations"/></a>
                <a href="${pageContext.request.contextPath}/taxis">&#128661; <fmt:message key="nav.taxis"/></a>
                <a href="${pageContext.request.contextPath}/notifications" style="position:relative;">
                    &#128276; <fmt:message key="nav.notifications"/>
                    <c:if test="${unreadNotificationCount > 0}">
                        <span style="position:absolute; top:-8px; right:-14px; background:var(--accent);
                                     color:#fff; border-radius:10px; font-size:11px; padding:1px 6px;">
                            ${unreadNotificationCount}
                        </span>
                    </c:if>
                </a>
                <a href="${pageContext.request.contextPath}/settings"><fmt:message key="nav.settings"/></a>
                <a href="${pageContext.request.contextPath}/logout"><fmt:message key="nav.logout"/></a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/login"><fmt:message key="nav.login"/></a>
                <a href="${pageContext.request.contextPath}/register"><fmt:message key="nav.register"/></a>
            </c:otherwise>
        </c:choose>
        <div class="header-tools">
            <form action="${pageContext.request.contextPath}/lang" method="get" style="margin:0;">
                <select name="code" class="lang-select" aria-label="Language" onchange="this.form.submit()">
                    <c:forEach var="l" items="${languages}">
                        <option value="${l.key}" ${l.key == lang ? 'selected' : ''}>&#127760; ${l.value}</option>
                    </c:forEach>
                </select>
                <noscript><button type="submit">OK</button></noscript>
            </form>
            <button type="button" id="themeToggle" class="theme-toggle" onclick="toggleTheme()"
                    title="<fmt:message key='nav.theme'/>" aria-label="<fmt:message key='nav.theme'/>">&#9790;</button>
        </div>
    </div>
</header>
<script>
    document.getElementById('themeToggle').textContent =
        document.documentElement.getAttribute('data-theme') === 'dark' ? '☀' : '☾';
</script>

<nav class="category-bar">
    <a href="${pageContext.request.contextPath}/search"><fmt:message key="nav.allHotels"/></a>
    <c:forEach var="city" items="${cities}">
        <a href="${pageContext.request.contextPath}/search?q=<c:out value='${city}'/>">&#128205; <c:out value="${city}"/></a>
    </c:forEach>
</nav>

<main class="container">
    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="flash"><c:out value="${sessionScope.flashMessage}"/></div>
        <c:remove var="flashMessage" scope="session"/>
    </c:if>
    <c:if test="${not empty error}">
        <div class="error-box"><c:out value="${error}"/></div>
    </c:if>
