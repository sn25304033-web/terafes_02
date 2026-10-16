package com.hotel.servlet;

import com.hotel.dao.NotificationDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications"})
public class NotificationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int userId = (int) req.getSession().getAttribute("userId");
        try {
            NotificationDAO dao = new NotificationDAO();
            req.setAttribute("notifications", dao.getRecentForUser(userId, 30));
            dao.markAllRead(userId); // viewing the page marks everything as read
        } catch (SQLException e) {
            throw new ServletException("Database error loading notifications", e);
        }
        req.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(req, resp);
    }

    /**
     *  POST /notifications?action=delete&id=5 -> delete one notification
     *  POST /notifications?action=deleteAll   -> delete all of this user's notifications
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int userId = (int) req.getSession().getAttribute("userId");
        String action = req.getParameter("action");
        try {
            NotificationDAO dao = new NotificationDAO();
            if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("id"));
                dao.delete(userId, id);
            } else if ("deleteAll".equals(action)) {
                dao.deleteAllForUser(userId);
            }
        } catch (NumberFormatException ignored) {
            // ignore malformed input
        } catch (SQLException e) {
            throw new ServletException("Database error deleting notification", e);
        }
        resp.sendRedirect(req.getContextPath() + "/notifications");
    }
}
