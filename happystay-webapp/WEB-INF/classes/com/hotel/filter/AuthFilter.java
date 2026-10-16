package com.hotel.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Requires a logged-in user for booking / reservations / account pages. Redirects to /login otherwise.
 * The page the guest was trying to open is remembered so they return to it after logging in.
 */
@WebFilter(urlPatterns = {"/book", "/reservations", "/reservation", "/taxi", "/taxis", "/notifications",
        "/change-password", "/settings", "/delete-account", "/review"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            if ("GET".equalsIgnoreCase(req.getMethod())) {
                String target = req.getServletPath() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
                req.getSession(true).setAttribute("redirectAfterLogin", target);
            }
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        chain.doFilter(request, response);
    }
}
