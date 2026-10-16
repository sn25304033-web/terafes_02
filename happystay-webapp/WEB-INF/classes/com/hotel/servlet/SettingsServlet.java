package com.hotel.servlet;

import com.hotel.dao.NotificationDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.dao.TaxiDAO;
import com.hotel.dao.UserDAO;
import com.hotel.model.User;
import com.hotel.util.I18n;
import com.hotel.util.Mails;
import com.hotel.util.PaymentValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 *  GET  /settings                       -> profile, language, theme, security, delete account
 *  POST /settings?action=updateProfile  -> update full name / phone / address
 */
@WebServlet(name = "SettingsServlet", urlPatterns = {"/settings"})
public class SettingsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int userId = (int) req.getSession().getAttribute("userId");
        try {
            User user = new UserDAO().findById(userId);
            req.setAttribute("user", user);
            req.setAttribute("reservationCount", new ReservationDAO().getByUser(userId).size());
            req.setAttribute("taxiCount", new TaxiDAO().getByUser(userId).size());
            req.setAttribute("unreadCount", new NotificationDAO().countUnread(userId));
        } catch (SQLException e) {
            throw new ServletException("Database error loading settings", e);
        }
        req.getRequestDispatcher("/WEB-INF/views/settings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        int userId = (int) session.getAttribute("userId");

        if ("updateProfile".equals(req.getParameter("action"))) {
            String fullName = req.getParameter("fullName");
            String phone = req.getParameter("phone");
            String address = req.getParameter("address");

            if (fullName == null || fullName.isBlank()) {
                req.setAttribute("error", I18n.t(req, "msg.profile.nameRequired"));
                doGet(req, resp);
                return;
            }
            if (phone != null && !phone.isBlank() && !PaymentValidationUtil.isValidPhone(phone)) {
                req.setAttribute("error", I18n.t(req, "msg.phone.invalidOptional"));
                doGet(req, resp);
                return;
            }
            try {
                UserDAO dao = new UserDAO();
                dao.updateProfile(userId, fullName.trim(), phone, address);
                User u = dao.findById(userId);
                Mails.profileUpdated(I18n.lang(req), u.getEmail(), u.getFullName());
            } catch (SQLException e) {
                throw new ServletException("Database error saving settings", e);
            }
            session.setAttribute("userName", fullName.trim());
            session.setAttribute("flashMessage", I18n.t(req, "msg.profile.saved"));
        }
        resp.sendRedirect(req.getContextPath() + "/settings");
    }
}
