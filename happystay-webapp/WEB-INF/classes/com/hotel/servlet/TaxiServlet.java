package com.hotel.servlet;

import com.hotel.dao.NotificationDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.dao.TaxiDAO;
import com.hotel.dao.UserDAO;
import com.hotel.model.Reservation;
import com.hotel.model.TaxiBooking;
import com.hotel.util.DateUtil;
import com.hotel.util.I18n;
import com.hotel.util.Mails;
import com.hotel.util.TaxiFare;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Taxi to the booked hotel.
 *  GET  /taxis                          -> my taxi rides
 *  GET  /taxi?reservationId=5           -> booking form for that stay (drop-off = the hotel)
 *  POST /taxi  action=book              -> validate + create (fare is calculated on the server)
 *  POST /taxi  action=cancel&id=        -> cancel (up to 2 hours before pickup), email sent
 */
@WebServlet(name = "TaxiServlet", urlPatterns = {"/taxi", "/taxis"})
public class TaxiServlet extends HttpServlet {

    private static final String FORM = "/WEB-INF/views/taxi-form.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int userId = (int) req.getSession().getAttribute("userId");
        try {
            if ("/taxis".equals(req.getServletPath())) {
                req.setAttribute("taxis", new TaxiDAO().getByUser(userId));
                req.getRequestDispatcher("/WEB-INF/views/taxis.jsp").forward(req, resp);
                return;
            }
            Reservation r = ownReservation(req, userId);
            if (r == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (!r.isTaxiBookable()) {
                req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.taxi.notBookable"));
                resp.sendRedirect(req.getContextPath() + "/reservation?id=" + r.getId());
                return;
            }
            prepareForm(req, r);
            // Default pickup: check-in day 12:00
            req.setAttribute("defaultPickup", r.getCheckIn().toLocalDate().atTime(12, 0).toString());
            req.getRequestDispatcher(FORM).forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Database error loading taxi page", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int userId = (int) req.getSession().getAttribute("userId");
        try {
            if ("cancel".equals(req.getParameter("action"))) {
                cancel(req, resp, userId);
                return;
            }

            Reservation r = ownReservation(req, userId);
            if (r == null || !r.isTaxiBookable()) {
                req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.taxi.notBookable"));
                resp.sendRedirect(req.getContextPath() + "/reservations");
                return;
            }

            TaxiBooking t = new TaxiBooking();
            t.setUserId(userId);
            t.setReservationId(r.getId());
            t.setPickupType(req.getParameter("pickupType"));
            t.setCarType(req.getParameter("carType"));
            String location = req.getParameter("pickupLocation");
            t.setPickupLocation(location == null ? "" : location.trim());
            t.setPassengers(DateUtil.parseInt(req.getParameter("passengers"), 1));
            t.setLuggage(Math.max(0, Math.min(20, DateUtil.parseInt(req.getParameter("luggage"), 0))));
            String notes = req.getParameter("notes");
            t.setNotes(notes == null || notes.isBlank() ? null : notes.trim().substring(0, Math.min(500, notes.trim().length())));

            LocalDateTime pickup = null;
            try {
                pickup = LocalDateTime.parse(req.getParameter("pickupTime"));   // from <input type="datetime-local">
            } catch (DateTimeParseException | NullPointerException ignored) {
                // handled below
            }

            String error = null;
            if (!TaxiFare.isValidZone(t.getPickupType()) || !TaxiFare.isValidCar(t.getCarType())) {
                error = I18n.t(req, "msg.taxi.choose");
            } else if (t.getPickupLocation().length() < 3 || t.getPickupLocation().length() > 255) {
                error = I18n.t(req, "msg.taxi.location");
            } else if (pickup == null) {
                error = I18n.t(req, "msg.taxi.time");
            } else if (pickup.isBefore(LocalDateTime.now().plusHours(2))) {
                error = I18n.t(req, "msg.taxi.tooSoon");
            } else if (pickup.toLocalDate().isBefore(r.getCheckIn().toLocalDate().minusDays(1))
                    || pickup.toLocalDate().isAfter(r.getCheckOut().toLocalDate())) {
                error = I18n.t(req, "msg.taxi.window", r.getCheckIn().toLocalDate().minusDays(1).toString(),
                        r.getCheckOut().toString());
            } else if (t.getPassengers() < 1 || t.getPassengers() > TaxiFare.CAR_SEATS.get(t.getCarType())) {
                error = I18n.t(req, "msg.taxi.seats", TaxiFare.CAR_SEATS.get(t.getCarType()));
            }
            if (error != null) {
                req.setAttribute("error", error);
                req.setAttribute("form", t);
                req.setAttribute("defaultPickup", req.getParameter("pickupTime"));
                prepareForm(req, r);
                req.getRequestDispatcher(FORM).forward(req, resp);
                return;
            }

            t.setPickupTime(Timestamp.valueOf(pickup));
            t.setEstimatedFare(TaxiFare.estimate(t.getCarType(), t.getPickupType()));
            TaxiDAO dao = new TaxiDAO();
            int id = dao.create(t);
            TaxiBooking saved = dao.findById(id);

            Mails.taxiBooked(I18n.lang(req), saved.getUserEmail(), saved);
            new NotificationDAO().create(userId, I18n.t(req, "notif.taxi.booked", id, r.getHotelName()), "/taxis");
            new NotificationDAO().notifyAdmins("notif.admin.newTaxi", "/admin/taxis", id, r.getHotelName());
            Mails.adminNewTaxi(new UserDAO().getAdmins(), saved);

            req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.taxi.booked", id));
            resp.sendRedirect(req.getContextPath() + "/taxis");
        } catch (SQLException e) {
            throw new ServletException("Database error booking taxi", e);
        }
    }

    private void cancel(HttpServletRequest req, HttpServletResponse resp, int userId) throws SQLException, IOException {
        int id = DateUtil.parseInt(req.getParameter("id"), -1);
        TaxiDAO dao = new TaxiDAO();
        if (dao.cancelByUser(id, userId)) {
            TaxiBooking t = dao.findById(id);
            Mails.taxiCancelled(I18n.lang(req), t.getUserEmail(), t);
            new NotificationDAO().notifyAdmins("notif.admin.taxiCancelled", "/admin/taxis", id);
            Mails.adminTaxiCancelled(new UserDAO().getAdmins(), t);
            req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.taxi.cancelled", id));
        } else {
            req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.taxi.cannotCancel"));
        }
        resp.sendRedirect(req.getContextPath() + "/taxis");
    }

    private Reservation ownReservation(HttpServletRequest req, int userId) throws SQLException {
        Reservation r = new ReservationDAO().findById(DateUtil.parseInt(req.getParameter("reservationId"), -1));
        return (r != null && r.getUserId() == userId) ? r : null;
    }

    private void prepareForm(HttpServletRequest req, Reservation r) {
        req.setAttribute("r", r);
        req.setAttribute("carBase", TaxiFare.CAR_BASE);
        req.setAttribute("carSeats", TaxiFare.CAR_SEATS);
        req.setAttribute("zoneMultiplier", TaxiFare.ZONE_MULTIPLIER);
        req.setAttribute("minPickup", r.getCheckIn().toLocalDate().minusDays(1).atStartOfDay().toString());
        req.setAttribute("maxPickup", r.getCheckOut().toLocalDate().atTime(23, 59).toString());
    }
}
