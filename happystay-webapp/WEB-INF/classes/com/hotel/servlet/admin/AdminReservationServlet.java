package com.hotel.servlet.admin;

import com.hotel.dao.NotificationDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.dao.TaxiDAO;
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
import java.util.Set;

/**
 *  GET  /admin/reservations?status=PENDING   -> list (optionally filtered)
 *  POST /admin/reservations  (id, status)    -> change status, notify + email the guest in their language
 */
@WebServlet(name = "AdminReservationServlet", urlPatterns = {"/admin/reservations"})
public class AdminReservationServlet extends HttpServlet {

    static final String[] STATUS_LIST = {"PENDING", "CONFIRMED", "CHECKED_IN", "COMPLETED", "CANCELLED"};
    private static final Set<String> STATUSES = Set.of(STATUS_LIST);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String status = req.getParameter("status");
        if (status != null && !STATUSES.contains(status)) status = null;
        try {
            req.setAttribute("reservations", new ReservationDAO().getAll(status));
        } catch (SQLException e) {
            throw new ServletException("Database error loading reservations", e);
        }
        req.setAttribute("statusFilter", status);
        req.setAttribute("statuses", STATUS_LIST);
        req.getRequestDispatcher("/WEB-INF/views/admin/reservations.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = DateUtil.parseInt(req.getParameter("id"), -1);
        String status = req.getParameter("status");
        if (STATUSES.contains(status)) {
            try {
                ReservationDAO dao = new ReservationDAO();
                Reservation r = dao.findById(id);
                if (r != null && !status.equals(r.getStatus())) {
                    dao.updateStatus(id, status);
                    r.setStatus(status);
                    String guestLang = I18n.normalize(r.getUserLang());

                    if ("CANCELLED".equals(status)) {
                        int taxis = new TaxiDAO().cancelForReservation(id);
                        Mails.reservationCancelled(guestLang, r.getUserEmail(), r, false, taxis);
                    } else {
                        Mails.reservationStatus(guestLang, r.getUserEmail(), r);
                    }
                    if (r.getUserId() > 0) {
                        new NotificationDAO().create(r.getUserId(),
                                I18n.t(guestLang, "notif.statusChanged", id, r.getHotelName(), I18n.t(guestLang, "status." + status)),
                                "/reservation?id=" + id);
                    }
                    req.getSession().setAttribute("flashMessage",
                            I18n.t(req, "msg.admin.resUpdated", id, I18n.t(req, "status." + status)));
                }
            } catch (SQLException e) {
                throw new ServletException("Database error updating reservation", e);
            }
        }
        String back = req.getParameter("filter");
        resp.sendRedirect(req.getContextPath() + "/admin/reservations" +
                (back != null && STATUSES.contains(back) ? "?status=" + back : ""));
    }
}
