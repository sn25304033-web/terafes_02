package com.hotel.servlet;

import com.hotel.dao.EmailVerificationDAO;
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

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");

        if (fullName == null || fullName.isBlank() || email == null || email.isBlank()
                || password == null || password.length() < 4) {
            req.setAttribute("error", I18n.t(req, "msg.register.required"));
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }
        if (phone != null && !phone.isBlank() && !PaymentValidationUtil.isValidPhone(phone)) {
            req.setAttribute("error", I18n.t(req, "msg.phone.invalidOptional"));
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        User u = new User();
        u.setFullName(fullName.trim());
        u.setEmail(email.trim().toLowerCase());
        u.setPhone(phone);
        u.setAddress(address);
        u.setLang(I18n.lang(req));

        try {
            int newUserId = new UserDAO().register(u, password);
            if (newUserId == -1) {
                req.setAttribute("error", I18n.t(req, "msg.register.emailTaken"));
                req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
                return;
            }
            String code = new EmailVerificationDAO().createCode(newUserId);
            Mails.verificationCode(I18n.lang(req), u.getEmail(), u.getFullName(), code);
        } catch (SQLException e) {
            throw new ServletException("Database error during registration", e);
        }
        resp.sendRedirect(req.getContextPath() + "/verify-email?email=" +
                java.net.URLEncoder.encode(u.getEmail(), java.nio.charset.StandardCharsets.UTF_8));
    }
}
