package com.hotel.filter;

import com.hotel.dao.NotificationDAO;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Exposes the logged-in user's unread notification count as a request attribute
 * ("unreadNotificationCount") so the header bell icon can always show a badge.
 */
@WebFilter(urlPatterns = {"/*"})
public class NotificationCountFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            try {
                int userId = (int) session.getAttribute("userId");
                req.setAttribute("unreadNotificationCount", new NotificationDAO().countUnread(userId));
            } catch (Exception e) {
                // Non-fatal: badge just won't show a count if this fails.
            }
        }
        chain.doFilter(request, response);
    }
}
