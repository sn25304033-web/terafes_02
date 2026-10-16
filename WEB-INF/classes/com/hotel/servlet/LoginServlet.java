package com.hotel.servlet;

import com.hotel.dao.UserDAO;
import com.hotel.model.User;
import com.hotel.util.I18n;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            UserDAO dao = new UserDAO();
            User user = dao.login(email == null ? "" : email.trim().toLowerCase(), password);
            if (user == null) {
                req.setAttribute("error", I18n.t(req, "msg.login.failed"));
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }
            if (!user.isEmailVerified()) {
                resp.sendRedirect(req.getContextPath() + "/verify-email?email=" +
                        java.net.URLEncoder.encode(user.getEmail(), java.nio.charset.StandardCharsets.UTF_8));
                return;
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getFullName());
            session.setAttribute("userRole", user.getRole());

            // Remember the language the guest is using now, so emails from the hotel use it too.
            String lang = I18n.lang(req);
            if (!lang.equals(user.getLang())) dao.updateLang(user.getId(), lang);

            String target = (String) session.getAttribute("redirectAfterLogin");
            session.removeAttribute("redirectAfterLogin");
            if (target != null && target.startsWith("/") && !target.startsWith("//")) {
                resp.sendRedirect(req.getContextPath() + target);   // back to the hotel/booking page
            } else if (user.isAdmin()) {
                resp.sendRedirect(req.getContextPath() + "/admin");
            } else {
                resp.sendRedirect(req.getContextPath() + "/home");
            }
        } catch (SQLException e) {
            throw new ServletException("Database error during login", e);
        }
    }
}
