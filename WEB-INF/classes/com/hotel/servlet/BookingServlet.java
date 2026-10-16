package com.hotel.servlet;

import com.hotel.dao.HotelDAO;
import com.hotel.dao.NotificationDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.dao.RoomTypeDAO;
import com.hotel.dao.UserDAO;
import com.hotel.model.Hotel;
import com.hotel.model.PendingBooking;
import com.hotel.model.Reservation;
import com.hotel.model.RoomType;
import com.hotel.model.User;
import com.hotel.util.DateUtil;
import com.hotel.util.I18n;
import com.hotel.util.Mails;
import com.hotel.util.PaymentValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;

/**
 * Two-step booking (same pattern as NovaCart's checkout):
 *  GET  /book?roomTypeId=&checkIn=&checkOut=&rooms=&guests=  -> step 1: guest + payment form
 *  POST /book                                                -> validate, store PendingBooking in session
 *  GET  /book?review=1                                       -> step 2: review & confirm
 *  POST /book?action=confirm                                 -> create the reservation (availability re-checked)
 */
@WebServlet(name = "BookingServlet", urlPatterns = {"/book"})
public class BookingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        PendingBooking pending = (PendingBooking) session.getAttribute("pendingBooking");

        try {
            if ("1".equals(req.getParameter("review")) && pending != null
                    && pending.getGuestName() != null && !pending.getGuestName().isBlank()) {
                loadRoomAndHotel(req, pending.getRoomTypeId());
                req.setAttribute("pending", pending);
                req.getRequestDispatcher("/WEB-INF/views/booking-review.jsp").forward(req, resp);
                return;
            }

            // Fresh start from the hotel page (query params) or "Edit" from review (session).
            if (req.getParameter("roomTypeId") != null) {
                pending = new PendingBooking();
                pending.setRoomTypeId(DateUtil.parseInt(req.getParameter("roomTypeId"), -1));
                pending.setCheckIn(DateUtil.parse(req.getParameter("checkIn")));
                pending.setCheckOut(DateUtil.parse(req.getParameter("checkOut")));
                pending.setRoomsCount(DateUtil.parseInt(req.getParameter("rooms"), 1));
                pending.setGuests(DateUtil.parseInt(req.getParameter("guests"), 1));
            }
            if (pending == null) {
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }

            RoomType room = loadRoomAndHotel(req, pending.getRoomTypeId());
            if (room == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String err = validateStayAndRooms(req, pending, room);
            if (err != null) {
                session.setAttribute("flashMessage", err);
                resp.sendRedirect(req.getContextPath() + "/hotel?id=" + room.getHotelId());
                return;
            }
            pending.setNights(DateUtil.nights(pending.getCheckIn(), pending.getCheckOut()));
            pending.setTotalAmount(calcTotal(room, pending));
            session.setAttribute("pendingBooking", pending);

            // Pre-fill guest name / phone from the profile the first time.
            if (pending.getGuestName() == null || pending.getGuestName().isBlank()) {
                User u = new UserDAO().findById((int) session.getAttribute("userId"));
                req.setAttribute("profile", u);
            }
            req.setAttribute("pending", pending);
            req.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Database error loading booking page", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        int userId = (int) session.getAttribute("userId");
        PendingBooking pending = (PendingBooking) session.getAttribute("pendingBooking");
        if (pending == null) {
            resp.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        if ("confirm".equals(req.getParameter("action"))) {
            confirm(req, resp, session, userId, pending);
            return;
        }

        // ---- Step 1 submit: guest details + payment ----
        String guestName = req.getParameter("guestName");
        String guestPhone = req.getParameter("guestPhone");
        String specialRequests = req.getParameter("specialRequests");
        String paymentMethod = req.getParameter("paymentMethod");
        if (paymentMethod == null || paymentMethod.isBlank()) paymentMethod = "PAY_AT_HOTEL";

        pending.setGuestName(guestName == null ? null : guestName.trim());
        pending.setGuestPhone(guestPhone == null ? null : guestPhone.trim());
        pending.setSpecialRequests(specialRequests == null || specialRequests.isBlank() ? null
                : specialRequests.trim().substring(0, Math.min(1000, specialRequests.trim().length())));
        pending.setPaymentMethod(paymentMethod);
        pending.setCardHolderName(null);
        pending.setCardLast4(null);

        String error = null;
        if (!PaymentValidationUtil.isValidCardholderName(guestName)) {
            error = I18n.t(req, "msg.book.guestName");
        } else if (!PaymentValidationUtil.isValidPhone(guestPhone)) {
            error = I18n.t(req, "msg.phone.invalid");
        } else if ("CREDIT_CARD".equals(paymentMethod)) {
            String cardNumber = req.getParameter("cardNumber");
            String cardHolderName = req.getParameter("cardHolderName");
            if (!PaymentValidationUtil.isValidCardholderName(cardHolderName)) {
                error = I18n.t(req, "msg.card.holder");
            } else if (!PaymentValidationUtil.isValidCardNumber(cardNumber)) {
                error = I18n.t(req, "msg.card.number");
            } else if (!PaymentValidationUtil.isValidExpiry(req.getParameter("cardExpiry"))) {
                error = I18n.t(req, "msg.card.expiry");
            } else if (!PaymentValidationUtil.isValidCvv(req.getParameter("cardCvv"))) {
                error = I18n.t(req, "msg.card.cvv");
            } else {
                pending.setCardHolderName(cardHolderName.trim());
                // Full card number / CVV are validated and then discarded - never stored.
                pending.setCardLast4(PaymentValidationUtil.maskCardNumber(cardNumber));
            }
        }

        if (error != null) {
            req.setAttribute("error", error);
            try {
                loadRoomAndHotel(req, pending.getRoomTypeId());
            } catch (SQLException e) {
                throw new ServletException(e);
            }
            req.setAttribute("pending", pending);
            req.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(req, resp);
            return;
        }
        session.setAttribute("pendingBooking", pending);
        resp.sendRedirect(req.getContextPath() + "/book?review=1");
    }

    private void confirm(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
                         int userId, PendingBooking pending) throws ServletException, IOException {
        if (pending.getGuestName() == null || pending.getGuestName().isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/book");
            return;
        }
        try {
            RoomType room = new RoomTypeDAO().findById(pending.getRoomTypeId());
            String err = validateStayAndRooms(req, pending, room);
            if (err != null) {
                session.setAttribute("flashMessage", err);
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }
            // Recalculate on the server - never trust a price that came from the browser.
            pending.setNights(DateUtil.nights(pending.getCheckIn(), pending.getCheckOut()));
            pending.setTotalAmount(calcTotal(room, pending));

            int reservationId;
            try {
                reservationId = new ReservationDAO().create(userId, pending);
            } catch (ReservationDAO.NotAvailableException na) {
                session.setAttribute("flashMessage", I18n.t(req, "msg.book.onlyLeft", na.getAvailable(), room.getName()));
                resp.sendRedirect(req.getContextPath() + "/hotel?id=" + room.getHotelId()
                        + "&checkIn=" + pending.getCheckIn() + "&checkOut=" + pending.getCheckOut());
                return;
            }
            session.removeAttribute("pendingBooking");

            Hotel hotel = new HotelDAO().findById(room.getHotelId());
            new NotificationDAO().create(userId, I18n.t(req, "notif.booking.received", reservationId, hotel.getName()),
                    "/reservation?id=" + reservationId);
            new NotificationDAO().notifyAdmins("notif.admin.newReservation", "/admin/reservations",
                    reservationId, hotel.getName(), pending.getCheckIn().toString(), pending.getCheckOut().toString());

            Reservation saved = new ReservationDAO().findById(reservationId);
            Mails.bookingReceived(I18n.lang(req), saved.getUserEmail(), saved);
            Mails.adminNewReservation(new UserDAO().getAdmins(), saved);
            resp.sendRedirect(req.getContextPath() + "/reservation?id=" + reservationId + "&placed=1");
        } catch (SQLException e) {
            throw new ServletException("Database error creating reservation", e);
        }
    }

    private RoomType loadRoomAndHotel(HttpServletRequest req, int roomTypeId) throws SQLException {
        RoomType room = new RoomTypeDAO().findById(roomTypeId);
        if (room != null) {
            req.setAttribute("room", room);
            req.setAttribute("hotel", new HotelDAO().findById(room.getHotelId()));
        }
        return room;
    }

    /** Returns an error message or null. Checks dates, room count, capacity and current availability. */
    private String validateStayAndRooms(HttpServletRequest req, PendingBooking p, RoomType room) throws SQLException {
        if (room == null) return I18n.t(req, "msg.book.roomGone");
        String err = DateUtil.validateStay(p.getCheckIn(), p.getCheckOut());
        if (err != null) return I18n.t(req, err);
        if (p.getRoomsCount() < 1 || p.getRoomsCount() > 10) return I18n.t(req, "msg.book.roomsRange");
        if (p.getGuests() < 1) return I18n.t(req, "msg.book.minGuests");
        if (p.getGuests() > room.getCapacity() * p.getRoomsCount()) {
            return I18n.t(req, "msg.book.capacity", room.getName(), room.getCapacity());
        }
        Date in = p.getCheckIn(), out = p.getCheckOut();
        int available = new RoomTypeDAO().countAvailable(room.getId(), in, out);
        if (available < p.getRoomsCount()) {
            return I18n.t(req, "msg.book.onlyLeft", Math.max(0, available), room.getName());
        }
        return null;
    }

    private BigDecimal calcTotal(RoomType room, PendingBooking p) {
        return room.getPricePerNight()
                .multiply(BigDecimal.valueOf(p.getNights()))
                .multiply(BigDecimal.valueOf(p.getRoomsCount()));
    }
}
