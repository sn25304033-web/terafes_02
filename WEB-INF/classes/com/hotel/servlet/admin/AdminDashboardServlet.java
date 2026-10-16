package com.hotel.servlet.admin;

import com.hotel.dao.HotelDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.dao.TaxiDAO;
import com.hotel.model.Reservation;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin"})
public class AdminDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            ReservationDAO rDao = new ReservationDAO();
            List<Reservation> all = rDao.getAll(null);
            req.setAttribute("hotelCount", new HotelDAO().getAll().size());
            req.setAttribute("reservationCount", all.size());
            req.setAttribute("pendingCount", rDao.countByStatus("PENDING"));
            req.setAttribute("taxiRequestedCount", new TaxiDAO().countByStatus("REQUESTED"));
            req.setAttribute("totalRevenue", rDao.totalRevenue());
            req.setAttribute("arrivalsToday", rDao.getArrivalsToday());
            req.setAttribute("recentReservations", all.size() > 10 ? all.subList(0, 10) : all);
        } catch (SQLException e) {
            throw new ServletException("Database error loading admin dashboard", e);
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
