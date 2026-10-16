package com.hotel.servlet;

import com.hotel.dao.HotelDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.dao.ReviewDAO;
import com.hotel.dao.RoomTypeDAO;
import com.hotel.model.Hotel;
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

/** GET /hotel?id=1&checkIn=...&checkOut=...&guests=2 -> hotel detail + room availability for the dates. */
@WebServlet(name = "HotelServlet", urlPatterns = {"/hotel"})
public class HotelServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = DateUtil.parseInt(req.getParameter("id"), -1);
        Date checkIn = DateUtil.parse(req.getParameter("checkIn"));
        Date checkOut = DateUtil.parse(req.getParameter("checkOut"));
        int guests = DateUtil.parseInt(req.getParameter("guests"), 1);

        if (checkIn == null && checkOut == null) {           // sensible default: tomorrow, 1 night
            checkIn = Date.valueOf(LocalDate.now().plusDays(1));
            checkOut = Date.valueOf(LocalDate.now().plusDays(2));
        }
        String err = DateUtil.validateStay(checkIn, checkOut);
        if (err != null) {
            req.setAttribute("error", I18n.t(req, err));
            checkIn = Date.valueOf(LocalDate.now().plusDays(1));
            checkOut = Date.valueOf(LocalDate.now().plusDays(2));
        }

        try {
            Hotel hotel = new HotelDAO().findById(id);
            if (hotel == null || (!hotel.isActive() && !"ADMIN".equals(req.getSession().getAttribute("userRole")))) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("hotel", hotel);
            req.setAttribute("roomTypes", new RoomTypeDAO().getByHotel(id, checkIn, checkOut));
            req.setAttribute("reviews", new ReviewDAO().getByHotel(id));

            Object userId = req.getSession().getAttribute("userId");
            if (userId != null) {
                req.setAttribute("canReview", new ReservationDAO().hasStayed((int) userId, id));
            }
        } catch (SQLException e) {
            throw new ServletException("Database error loading hotel", e);
        }
        req.setAttribute("checkIn", checkIn);
        req.setAttribute("checkOut", checkOut);
        req.setAttribute("nights", DateUtil.nights(checkIn, checkOut));
        req.setAttribute("guests", guests);
        req.setAttribute("today", LocalDate.now().toString());
        req.getRequestDispatcher("/WEB-INF/views/hotel.jsp").forward(req, resp);
    }
}
