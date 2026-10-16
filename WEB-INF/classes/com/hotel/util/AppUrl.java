package com.hotel.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Remembers the site's address (e.g. http://localhost:8080/happystay-webapp) from the first real request,
 * so emails - including ones sent later by the reminder job - can contain "View reservation" buttons.
 * To force a fixed address (e.g. a public domain), set app.baseUrl in WEB-INF/classes/app.properties.
 */
public class AppUrl {

    private static volatile String base;

    static {
        try (java.io.InputStream in = AppUrl.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (in != null) {
                java.util.Properties p = new java.util.Properties();
                p.load(in);
                String v = p.getProperty("app.baseUrl");
                if (v != null && !v.isBlank()) base = v.trim().replaceAll("/+$", "");
            }
        } catch (Exception ignored) {
            // optional file
        }
    }

    public static void remember(HttpServletRequest req) {
        if (base != null) return;
        String scheme = req.getScheme();
        int port = req.getServerPort();
        boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        base = scheme + "://" + req.getServerName() + (defaultPort ? "" : ":" + port) + req.getContextPath();
    }

    /** null until the first request (then emails simply have no button). */
    public static String get() {
        return base;
    }
}
