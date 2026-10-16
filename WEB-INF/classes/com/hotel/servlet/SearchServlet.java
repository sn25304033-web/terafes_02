package com.hotel.servlet;

import com.hotel.dao.HotelDAO;
import com.hotel.util.DateUtil;
import com.hotel.util.I18n;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

/** GET /search?q=Tokyo&checkIn=2026-10-01&checkOut=2026-10-03&guests=2 */
@WebServlet(name = "SearchServlet", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String q = req.getParameter("q");
        Date checkIn = DateUtil.parse(req.getParameter("checkIn"));
        Date checkOut = DateUtil.parse(req.getParameter("checkOut"));
        int guests = DateUtil.parseInt(req.getParameter("guests"), 1);

        if (checkIn != null || checkOut != null) {
            String err = DateUtil.validateStay(checkIn, checkOut);
            if (err != null) {
                req.setAttribute("error", I18n.t(req, err));
                checkIn = null;
                checkOut = null;
            }
        }
        try {
            req.setAttribute("hotels", new HotelDAO().search(q, checkIn, checkOut, guests));
        } catch (SQLException e) {
            throw new ServletException("Database error searching hotels", e);
        }
        req.setAttribute("q", q);
        req.setAttribute("checkIn", checkIn);
        req.setAttribute("checkOut", checkOut);
        req.setAttribute("guests", guests);
        req.setAttribute("today", LocalDate.now().toString());
        req.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(req, resp);
    }
}
