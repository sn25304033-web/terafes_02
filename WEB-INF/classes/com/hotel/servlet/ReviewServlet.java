package com.hotel.servlet;

import com.hotel.dao.ReservationDAO;
import com.hotel.dao.ReviewDAO;
import com.hotel.util.DateUtil;
import com.hotel.util.I18n;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** POST /review  (hotelId, rating 1-5, comment) - only guests who have stayed at the hotel. */
@WebServlet(name = "ReviewServlet", urlPatterns = {"/review"})
public class ReviewServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int userId = (int) req.getSession().getAttribute("userId");
        int hotelId = DateUtil.parseInt(req.getParameter("hotelId"), -1);
        int rating = DateUtil.parseInt(req.getParameter("rating"), 0);
        String comment = req.getParameter("comment");

        try {
            if (rating < 1 || rating > 5) {
                req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.review.rating"));
            } else if (!new ReservationDAO().hasStayed(userId, hotelId)) {
                req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.review.notStayed"));
            } else {
                if (comment != null && comment.length() > 1000) comment = comment.substring(0, 1000);
                new ReviewDAO().save(hotelId, userId, rating, comment);
                req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.review.saved"));
            }
        } catch (SQLException e) {
            throw new ServletException("Database error saving review", e);
        }
        resp.sendRedirect(req.getContextPath() + "/hotel?id=" + hotelId + "#reviews");
    }
}
