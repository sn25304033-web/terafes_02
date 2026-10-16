package com.hotel.servlet;

import com.hotel.dao.NotificationDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.dao.TaxiDAO;
import com.hotel.dao.UserDAO;
import com.hotel.model.Reservation;
import com.hotel.util.DateUtil;
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
 *  GET  /reservations                  -> the guest's bookings
 *  GET  /reservation?id=5              -> one booking + its taxi rides (owner or admin only)
 *  POST /reservation?action=cancel&id= -> guest cancels before check-in: taxis cancelled, email sent
 */
@WebServlet(name = "ReservationServlet", urlPatterns = {"/reservations", "/reservation"})
public class ReservationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int userId = (int) req.getSession().getAttribute("userId");
        boolean isAdmin = "ADMIN".equals(req.getSession().getAttribute("userRole"));
        try {
            ReservationDAO dao = new ReservationDAO();
            if ("/reservations".equals(req.getServletPath())) {
                req.setAttribute("reservations", dao.getByUser(userId));
                req.getRequestDispatcher("/WEB-INF/views/reservations.jsp").forward(req, resp);
                return;
            }
            Reservation r = dao.findById(DateUtil.parseInt(req.getParameter("id"), -1));
            if (r == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (r.getUserId() != userId && !isAdmin) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            req.setAttribute("r", r);
            req.setAttribute("taxis", new TaxiDAO().getByReservation(r.getId()));
            req.setAttribute("justPlaced", "1".equals(req.getParameter("placed")));
            req.getRequestDispatcher("/WEB-INF/views/reservation-detail.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Database error loading reservations", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int userId = (int) req.getSession().getAttribute("userId");
        int id = DateUtil.parseInt(req.getParameter("id"), -1);
        if ("cancel".equals(req.getParameter("action"))) {
            try {
                ReservationDAO dao = new ReservationDAO();
                if (dao.cancelByUser(id, userId)) {
                    int taxis = new TaxiDAO().cancelForReservation(id);
                    Reservation r = dao.findById(id);

                    boolean mailed = Mails.reservationCancelled(I18n.lang(req), r.getUserEmail(), r, true, taxis);
                    new NotificationDAO().create(userId, I18n.t(req, "notif.cancelled", id), "/reservation?id=" + id);
                    new NotificationDAO().notifyAdmins("notif.admin.cancelledByGuest", "/admin/reservations", id, r.getHotelName());
                    Mails.adminReservationCancelled(new UserDAO().getAdmins(), r, "mail.admin.cancel.byGuest");

                    req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.res.cancelled", id)
                            + (mailed ? " " + I18n.t(req, "msg.res.cancelMailSent") : ""));
                } else {
                    req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.res.cannotCancel"));
                }
            } catch (SQLException e) {
                throw new ServletException("Database error cancelling reservation", e);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/reservation?id=" + id);
    }
}
