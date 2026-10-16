package com.hotel.servlet;

import com.hotel.dao.UserDAO;
import com.hotel.util.I18n;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URI;

/** GET /lang?code=ja -> switch language (session + 1-year cookie + saved on the account), then go back. */
@WebServlet(name = "LanguageServlet", urlPatterns = {"/lang"})
public class LanguageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String code = I18n.normalize(req.getParameter("code"));
        req.getSession().setAttribute("lang", code);

        Cookie cookie = new Cookie("lang", code);
        cookie.setMaxAge(60 * 60 * 24 * 365);
        cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
        resp.addCookie(cookie);

        Object userId = req.getSession().getAttribute("userId");
        if (userId != null) {
            try {
                new UserDAO().updateLang((int) userId, code);   // emails will use this language too
            } catch (Exception ignored) {
                // not critical
            }
        }
        resp.sendRedirect(safeBack(req));
    }

    /** Only redirect back to a page of this site (never to another host). */
    private String safeBack(HttpServletRequest req) {
        String home = req.getContextPath() + "/home";
        String referer = req.getHeader("Referer");
        if (referer == null) return home;
        try {
            URI uri = URI.create(referer);
            if (uri.getHost() != null && !uri.getHost().equalsIgnoreCase(req.getServerName())) return home;
            String path = uri.getRawPath();
            if (path == null || !path.startsWith(req.getContextPath() + "/") || path.contains("/lang")) return home;
            return path + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
        } catch (IllegalArgumentException e) {
            return home;
        }
    }
}
