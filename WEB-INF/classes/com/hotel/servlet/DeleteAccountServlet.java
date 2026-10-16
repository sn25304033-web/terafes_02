package com.hotel.servlet;

import com.hotel.dao.AccountDeletionDAO;
import com.hotel.dao.NotificationDAO;
import com.hotel.dao.UserDAO;
import com.hotel.model.User;
import com.hotel.util.I18n;
import com.hotel.util.Mails;
import com.hotel.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Settings -> Delete account (two steps, like change-password):
 *  GET  /delete-account                  -> warning page + "send code" button
 *  POST /delete-account?action=sendCode  -> email a 6-digit code (valid 10 min)
 *  POST /delete-account?action=confirm   -> code + password + typed confirmation -> delete everything
 */
@WebServlet(name = "DeleteAccountServlet", urlPatterns = {"/delete-account"})
public class DeleteAccountServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/delete-account.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        int userId = (int) session.getAttribute("userId");
        String action = req.getParameter("action");

        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.findById(userId);

            // An admin can't delete the last admin account (nobody could manage the hotels any more).
            if (user.isAdmin() && userDAO.getAllAdminIds().size() <= 1) {
                req.setAttribute("error", I18n.t(req, "msg.delete.lastAdmin"));
                req.getRequestDispatcher(VIEW).forward(req, resp);
                return;
            }

            if ("sendCode".equals(action)) {
                String code = new AccountDeletionDAO().createCode(userId);
                if (Mails.deleteAccountCode(I18n.lang(req), user.getEmail(), user.getFullName(), code)) {
                    req.setAttribute("info", I18n.t(req, "msg.codeSentTo", user.getEmail()));
                } else {
                    req.setAttribute("error", I18n.t(req, "msg.mailFailed"));
                }
                req.setAttribute("codeSent", true);
                req.getRequestDispatcher(VIEW).forward(req, resp);
                return;
            }

            if ("confirm".equals(action)) {
                String code = req.getParameter("code");
                String password = req.getParameter("password");
                String typed = req.getParameter("confirmText");
                req.setAttribute("codeSent", true);

                String error = null;
                if (code == null || code.isBlank()) {
                    error = I18n.t(req, "msg.code.enter");
                } else if (!"DELETE".equals(typed == null ? "" : typed.trim())) {
                    error = I18n.t(req, "msg.delete.typeDelete");
                } else if (password == null || !PasswordUtil.hash(password).equals(user.getPasswordHash())) {
                    error = I18n.t(req, "msg.delete.wrongPassword");
                } else if (!new AccountDeletionDAO().validateAndConsume(userId, code.trim())) {
                    error = I18n.t(req, "msg.code.invalid");
                }
                if (error != null) {
                    req.setAttribute("error", error);
                    req.getRequestDispatcher(VIEW).forward(req, resp);
                    return;
                }

                String lang = I18n.lang(req);
                List<Integer> cancelled = userDAO.deleteAccount(userId);
                for (int resId : cancelled) {
                    new NotificationDAO().notifyAdmins("notif.admin.cancelledByDeletion", "/admin/reservations", resId);
                }
                Mails.accountDeleted(lang, user.getEmail(), user.getFullName());
                Mails.adminAccountDeleted(userDAO.getAdmins(), user.getEmail(), cancelled.size());

                session.invalidate();
                HttpSession fresh = req.getSession(true);
                fresh.setAttribute("lang", lang);
                fresh.setAttribute("flashMessage", I18n.t(lang, "msg.delete.done"));
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/delete-account");
        } catch (SQLException e) {
            throw new ServletException("Database error deleting account", e);
        }
    }
}
