package com.hotel.servlet;

import com.hotel.dao.PasswordResetDAO;
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

/**
 *  GET  /change-password                    -> page
 *  POST /change-password?action=sendCode    -> email a code to the logged-in user
 *  POST /change-password?action=confirm     -> verify the code and set the new password
 */
@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/change-password"})
public class ChangePasswordServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/change-password.jsp";

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

            if ("sendCode".equals(action)) {
                String code = new PasswordResetDAO().createCode(userId);
                if (Mails.passwordCode(I18n.lang(req), user.getEmail(), user.getFullName(), code)) {
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
                String newPassword = req.getParameter("newPassword");
                String confirmPassword = req.getParameter("confirmPassword");
                req.setAttribute("codeSent", true);

                String error = null;
                if (code == null || code.isBlank() || newPassword == null || newPassword.length() < 4) {
                    error = I18n.t(req, "msg.password.codeAndLength");
                } else if (!newPassword.equals(confirmPassword)) {
                    error = I18n.t(req, "msg.password.mismatch");
                } else if (!new PasswordResetDAO().validateAndConsume(userId, code.trim())) {
                    error = I18n.t(req, "msg.code.invalid");
                }
                if (error != null) {
                    req.setAttribute("error", error);
                    req.getRequestDispatcher(VIEW).forward(req, resp);
                    return;
                }
                userDAO.updatePasswordHash(userId, PasswordUtil.hash(newPassword));
                Mails.passwordChanged(I18n.lang(req), user.getEmail(), user.getFullName());
                session.setAttribute("flashMessage", I18n.t(req, "msg.password.changed"));
                resp.sendRedirect(req.getContextPath() + "/settings");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/change-password");
        } catch (SQLException e) {
            throw new ServletException("Database error during password change", e);
        }
    }
}
