package com.hotel.servlet;

import com.hotel.dao.EmailVerificationDAO;
import com.hotel.dao.UserDAO;
import com.hotel.model.User;
import com.hotel.util.I18n;
import com.hotel.util.Mails;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 *  GET  /verify-email?email=...          -> show the code entry form
 *  POST /verify-email?action=confirm     -> verify the code, mark the account verified
 *  POST /verify-email?action=resend      -> send a new code
 */
@WebServlet(name = "VerifyEmailServlet", urlPatterns = {"/verify-email"})
public class VerifyEmailServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/verify-email.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("email", req.getParameter("email"));
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        String email = req.getParameter("email");
        email = (email == null) ? "" : email.trim().toLowerCase();
        req.setAttribute("email", email);

        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.findByEmail(email);
            if (user == null) {
                req.setAttribute("error", I18n.t(req, "msg.verify.noAccount"));
                req.getRequestDispatcher(VIEW).forward(req, resp);
                return;
            }

            if ("resend".equals(action)) {
                String code = new EmailVerificationDAO().createCode(user.getId());
                if (Mails.verificationCode(I18n.lang(req), user.getEmail(), user.getFullName(), code)) {
                    req.setAttribute("info", I18n.t(req, "msg.codeSentTo", user.getEmail()));
                } else {
                    req.setAttribute("error", I18n.t(req, "msg.mailFailed"));
                }
                req.getRequestDispatcher(VIEW).forward(req, resp);
                return;
            }

            if ("confirm".equals(action)) {
                String code = req.getParameter("code");
                if (code == null || code.isBlank()) {
                    req.setAttribute("error", I18n.t(req, "msg.code.enter"));
                    req.getRequestDispatcher(VIEW).forward(req, resp);
                    return;
                }
                if (!new EmailVerificationDAO().validateAndConsume(user.getId(), code.trim())) {
                    req.setAttribute("error", I18n.t(req, "msg.code.invalid"));
                    req.getRequestDispatcher(VIEW).forward(req, resp);
                    return;
                }
                userDAO.markEmailVerified(user.getId());
                Mails.welcome(I18n.lang(req), user.getEmail(), user.getFullName());
                req.getSession(true).setAttribute("flashMessage", I18n.t(req, "msg.verify.done"));
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/verify-email?email=" +
                    java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8));
        } catch (SQLException e) {
            throw new ServletException("Database error during email verification", e);
        }
    }
}
