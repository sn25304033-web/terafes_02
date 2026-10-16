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

import java.io.IOException;
import java.sql.SQLException;

/**
 *  GET  /forgot-password                 -> "enter your email" form
 *  POST /forgot-password?action=sendCode -> email a code if the address is registered
 *  POST /forgot-password?action=confirm  -> verify the code and set a new password
 */
@WebServlet(name = "ForgotPasswordServlet", urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/forgot-password.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        String email = req.getParameter("email");
        email = (email == null) ? "" : email.trim().toLowerCase();

        try {
            UserDAO userDAO = new UserDAO();

            if ("sendCode".equals(action)) {
                User user = userDAO.findByEmail(email);
                // Same message whether or not the email exists (can't be used to probe accounts).
                if (user != null) {
                    String code = new PasswordResetDAO().createCode(user.getId());
                    Mails.passwordCode(I18n.lang(req), user.getEmail(), user.getFullName(), code);
                }
                req.setAttribute("email", email);
                req.setAttribute("codeSent", true);
                req.setAttribute("info", I18n.t(req, "msg.forgot.sent"));
                req.getRequestDispatcher(VIEW).forward(req, resp);
                return;
            }

            if ("confirm".equals(action)) {
                String code = req.getParameter("code");
                String newPassword = req.getParameter("newPassword");
                String confirmPassword = req.getParameter("confirmPassword");
                req.setAttribute("email", email);
                req.setAttribute("codeSent", true);

                User user = userDAO.findByEmail(email);
                String error = null;
                if (user == null) {
                    error = I18n.t(req, "msg.code.invalid");
                } else if (code == null || code.isBlank() || newPassword == null || newPassword.length() < 4) {
                    error = I18n.t(req, "msg.password.codeAndLength");
                } else if (!newPassword.equals(confirmPassword)) {
                    error = I18n.t(req, "msg.password.mismatch");
                } else if (!new PasswordResetDAO().validateAndConsume(user.getId(), code.trim())) {
                    error = I18n.t(req, "msg.code.invalid");
                }
                if (error != null) {
                    req.setAttribute("error", error);
                    req.getRequestDispatcher(VIEW).forward(req, resp);
                    return;
                }
                userDAO.updatePasswordHash(user.getId(), PasswordUtil.hash(newPassword));
                Mails.passwordChanged(I18n.lang(req), user.getEmail(), user.getFullName());
                req.getSession(true).setAttribute("flashMessage", I18n.t(req, "msg.forgot.done"));
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/forgot-password");
        } catch (SQLException e) {
            throw new ServletException("Database error during password reset", e);
        }
    }
}
