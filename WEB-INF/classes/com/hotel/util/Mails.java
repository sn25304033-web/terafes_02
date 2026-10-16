package com.hotel.util;

import com.hotel.model.Reservation;
import com.hotel.model.TaxiBooking;
import com.hotel.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Every HappyStay email in one place, written in the receiver's language (I18n "mail.*" keys).
 *
 * <ul>
 *   <li>Codes the user is waiting for (verification / password / delete) are sent immediately
 *       so the page can say "sent" or "could not send".</li>
 *   <li>All other emails (confirmations, cancellations, reminders, admin alerts) go into a background
 *       queue, so a slow SMTP server never makes a booking or cancel page slow. A failure is only logged.</li>
 * </ul>
 */
public class Mails {

    /** One background thread sends queued emails in order. Shut down by AppLifecycleListener. */
    private static final ExecutorService QUEUE = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "happystay-mail");
        t.setDaemon(true);
        return t;
    });

    // =====================================================================
    //  Account
    // =====================================================================

    public static boolean verificationCode(String lang, String to, String name, String code) {
        return sendNow(lang, to, I18n.t(lang, "mail.verify.subject"),
                hello(lang, name) + p(I18n.t(lang, "mail.verify.body")) + MailTemplates.codeBox(code)
                        + small(I18n.t(lang, "mail.codeExpires", 15)));
    }

    public static boolean passwordCode(String lang, String to, String name, String code) {
        return sendNow(lang, to, I18n.t(lang, "mail.password.subject"),
                hello(lang, name) + p(I18n.t(lang, "mail.password.body")) + MailTemplates.codeBox(code)
                        + small(I18n.t(lang, "mail.codeExpires", 10) + " " + I18n.t(lang, "mail.ignoreIfNotYou")));
    }

    public static boolean deleteAccountCode(String lang, String to, String name, String code) {
        return sendNow(lang, to, I18n.t(lang, "mail.delete.subject"),
                hello(lang, name) + p(I18n.t(lang, "mail.delete.body")) + MailTemplates.codeBox(code)
                        + small(I18n.t(lang, "mail.codeExpires", 10) + " " + I18n.t(lang, "mail.delete.warn")));
    }

    /** After the email address is verified. */
    public static boolean welcome(String lang, String to, String name) {
        return queue(lang, to, I18n.t(lang, "mail.welcome.subject"),
                hello(lang, name) + p(I18n.t(lang, "mail.welcome.body"))
                        + button(lang, "/home", "mail.btn.findHotel") + small(I18n.t(lang, "mail.welcome.tip")));
    }

    /** Security notice after a password reset or change. */
    public static boolean passwordChanged(String lang, String to, String name) {
        return queue(lang, to, I18n.t(lang, "mail.pwChanged.subject"),
                hello(lang, name) + p(I18n.t(lang, "mail.pwChanged.body", now(lang)))
                        + small(I18n.t(lang, "mail.pwChanged.warn")));
    }

    /** Notice after name / phone / address changed in Settings. */
    public static boolean profileUpdated(String lang, String to, String name) {
        return queue(lang, to, I18n.t(lang, "mail.profile.subject"),
                hello(lang, name) + p(I18n.t(lang, "mail.profile.body", now(lang)))
                        + button(lang, "/settings", "mail.btn.settings") + small(I18n.t(lang, "mail.pwChanged.warn")));
    }

    public static boolean accountDeleted(String lang, String to, String name) {
        return queue(lang, to, I18n.t(lang, "mail.deleted.subject"),
                hello(lang, name) + p(I18n.t(lang, "mail.deleted.body")));
    }

    // =====================================================================
    //  Reservations (guest)
    // =====================================================================

    /** Right after booking: "confirmed" when paid online, "received - waiting for the hotel" when paying at the hotel. */
    public static boolean bookingReceived(String lang, String to, Reservation r) {
        boolean confirmed = "CONFIRMED".equals(r.getStatus());
        return queue(lang, to,
                I18n.t(lang, confirmed ? "mail.booking.subjectConfirmed" : "mail.booking.subject", r.getId()),
                hello(lang, r.getGuestName())
                        + p(I18n.t(lang, confirmed ? "mail.booking.bodyConfirmed" : "mail.booking.bodyPending"))
                        + reservationTable(lang, r)
                        + button(lang, "/reservation?id=" + r.getId(), "mail.btn.viewReservation")
                        + small(I18n.t(lang, "mail.booking.footer")));
    }

    /** byGuest = the guest cancelled it themselves; otherwise the hotel did. */
    public static boolean reservationCancelled(String lang, String to, Reservation r, boolean byGuest, int taxisCancelled) {
        String body = hello(lang, r.getGuestName())
                + p(I18n.t(lang, byGuest ? "mail.cancel.byGuest" : "mail.cancel.byHotel", r.getId()))
                + reservationTable(lang, r);
        if (taxisCancelled > 0) body += p(I18n.t(lang, "mail.cancel.taxis", taxisCancelled));
        body += small(I18n.t(lang, "mail.cancel.footer")) + button(lang, "/search", "mail.btn.findHotel");
        return queue(lang, to, I18n.t(lang, "mail.cancel.subject", r.getId()), body);
    }

    /**
     * Admin changed the status. Each status has its own email:
     * CONFIRMED = booking confirmed, CHECKED_IN = welcome, COMPLETED = thank you + review link, PENDING = back to pending.
     */
    public static boolean reservationStatus(String lang, String to, Reservation r) {
        String s = r.getStatus();
        String body = hello(lang, r.getGuestName()) + p(I18n.t(lang, "mail.status." + s + ".body", r.getId(), r.getHotelName()))
                + reservationTable(lang, r);
        if ("COMPLETED".equals(s)) {
            body += button(lang, "/hotel?id=" + r.getHotelId() + "#reviews", "mail.btn.review");
        } else {
            body += button(lang, "/reservation?id=" + r.getId(), "mail.btn.viewReservation");
        }
        if ("CONFIRMED".equals(s)) body += small(I18n.t(lang, "mail.status.CONFIRMED.taxiTip"));
        return queue(lang, to, I18n.t(lang, "mail.status." + s + ".subject", r.getId(), r.getHotelName()), body);
    }

    /** Sent automatically the day before check-in (ReminderJob). */
    public static boolean checkInReminder(String lang, String to, Reservation r, List<TaxiBooking> taxis) {
        String body = hello(lang, r.getGuestName())
                + p(I18n.t(lang, "mail.reminder.body", r.getHotelName()))
                + reservationTable(lang, r)
                + p(I18n.t(lang, "mail.reminder.address", esc(r.getHotelAddress())));
        boolean hasTaxi = false;
        for (TaxiBooking t : taxis) {
            if (!"CANCELLED".equals(t.getStatus())) {
                body += p("<b>" + I18n.t(lang, "mail.reminder.yourTaxi") + "</b>") + taxiTable(lang, t);
                hasTaxi = true;
            }
        }
        if (!hasTaxi) {
            body += p(I18n.t(lang, "mail.reminder.noTaxi"))
                    + button(lang, "/taxi?reservationId=" + r.getId(), "mail.btn.bookTaxi");
        }
        return queue(lang, to, I18n.t(lang, "mail.reminder.subject", r.getHotelName()), body);
    }

    // =====================================================================
    //  Taxi (guest)
    // =====================================================================

    public static boolean taxiBooked(String lang, String to, TaxiBooking t) {
        return queue(lang, to, I18n.t(lang, "mail.taxi.subject", t.getId()),
                hello(lang, t.getGuestName()) + p(I18n.t(lang, "mail.taxi.body")) + taxiTable(lang, t)
                        + button(lang, "/taxis", "mail.btn.myTaxis") + small(I18n.t(lang, "mail.taxi.footer")));
    }

    public static boolean taxiCancelled(String lang, String to, TaxiBooking t) {
        return queue(lang, to, I18n.t(lang, "mail.taxiCancel.subject", t.getId()),
                hello(lang, t.getGuestName()) + p(I18n.t(lang, "mail.taxiCancel.body", t.getId())) + taxiTable(lang, t));
    }

    /** Admin changed the taxi status or assigned a driver. */
    public static boolean taxiStatus(String lang, String to, TaxiBooking t) {
        String key = "CONFIRMED".equals(t.getStatus()) && t.getDriverName() != null ? "mail.taxiStatus.driverBody"
                : "COMPLETED".equals(t.getStatus()) ? "mail.taxiStatus.completedBody" : "mail.taxiStatus.body";
        return queue(lang, to, I18n.t(lang, "mail.taxiStatus.subject", t.getId()),
                hello(lang, t.getGuestName())
                        + p(I18n.t(lang, key, t.getId(), I18n.t(lang, "taxiStatus." + t.getStatus())))
                        + taxiTable(lang, t) + button(lang, "/taxis", "mail.btn.myTaxis"));
    }

    /** Sent automatically about 3 hours before pickup (ReminderJob). */
    public static boolean taxiReminder(String lang, String to, TaxiBooking t) {
        return queue(lang, to, I18n.t(lang, "mail.taxiReminder.subject", t.getId()),
                hello(lang, t.getGuestName()) + p(I18n.t(lang, "mail.taxiReminder.body")) + taxiTable(lang, t));
    }

    // =====================================================================
    //  Admin emails (every ADMIN user, each in their own language)
    // =====================================================================

    public static void adminNewReservation(List<User> admins, Reservation r) {
        for (User a : withExtraAdmins(admins)) {
            String l = I18n.normalize(a.getLang());
            queue(l, a.getEmail(), I18n.t(l, "mail.admin.newRes.subject", r.getId(), r.getHotelName()),
                    p(I18n.t(l, "mail.admin.newRes.body")) + reservationTable(l, r) + guestRows(l, r)
                            + button(l, "/admin/reservations?status=" + r.getStatus(), "mail.btn.admin"));
        }
    }

    public static void adminReservationCancelled(List<User> admins, Reservation r, String reasonKey) {
        for (User a : withExtraAdmins(admins)) {
            String l = I18n.normalize(a.getLang());
            queue(l, a.getEmail(), I18n.t(l, "mail.admin.cancel.subject", r.getId(), r.getHotelName()),
                    p(I18n.t(l, reasonKey, r.getId())) + reservationTable(l, r) + guestRows(l, r)
                            + button(l, "/admin/reservations?status=CANCELLED", "mail.btn.admin"));
        }
    }

    public static void adminNewTaxi(List<User> admins, TaxiBooking t) {
        for (User a : withExtraAdmins(admins)) {
            String l = I18n.normalize(a.getLang());
            String notes = t.getNotes() == null ? "" : p("&#128221; " + esc(t.getNotes()));
            queue(l, a.getEmail(), I18n.t(l, "mail.admin.newTaxi.subject", t.getId(), t.getHotelName()),
                    p(I18n.t(l, "mail.admin.newTaxi.body", esc(t.getGuestName()), esc(t.getGuestPhone())))
                            + taxiTable(l, t) + notes + button(l, "/admin/taxis?status=REQUESTED", "mail.btn.admin"));
        }
    }

    public static void adminTaxiCancelled(List<User> admins, TaxiBooking t) {
        for (User a : withExtraAdmins(admins)) {
            String l = I18n.normalize(a.getLang());
            queue(l, a.getEmail(), I18n.t(l, "mail.admin.taxiCancel.subject", t.getId()),
                    p(I18n.t(l, "mail.admin.taxiCancel.body", t.getId())) + taxiTable(l, t)
                            + button(l, "/admin/taxis", "mail.btn.admin"));
        }
    }

    public static void adminAccountDeleted(List<User> admins, String deletedEmail, int cancelledCount) {
        for (User a : withExtraAdmins(admins)) {
            String l = I18n.normalize(a.getLang());
            queue(l, a.getEmail(), I18n.t(l, "mail.admin.deleted.subject"),
                    p(I18n.t(l, "mail.admin.deleted.body", esc(deletedEmail), cancelledCount))
                            + button(l, "/admin/reservations?status=CANCELLED", "mail.btn.admin"));
        }
    }

    /**
     * The ADMIN users from the database plus the addresses listed in mail.admin.notify
     * (duplicates and blank addresses removed), so the owner always gets the alert even when the
     * address stored on the admin account is wrong.
     */
    private static List<User> withExtraAdmins(List<User> admins) {
        List<User> all = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (admins != null) {
            for (User a : admins) {
                if (a.getEmail() == null || a.getEmail().isBlank()) continue;
                if (seen.add(a.getEmail().trim().toLowerCase())) all.add(a);
            }
        }
        String lang = I18n.normalize(MailUtil.adminNotifyLang());
        for (String address : MailUtil.adminNotifyAddresses()) {
            if (seen.add(address.toLowerCase())) {
                User extra = new User();
                extra.setEmail(address);
                extra.setLang(lang);
                all.add(extra);
            }
        }
        return all;
    }

    // =====================================================================
    //  Helpers
    // =====================================================================

    private static String reservationTable(String lang, Reservation r) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd (EEE)", I18n.locale(lang));
        return MailTemplates.infoTable(
                MailTemplates.infoRow(I18n.t(lang, "label.reservationNo"), "#" + r.getId())
                + MailTemplates.infoRow(I18n.t(lang, "label.hotel"), esc(r.getHotelName()) + ", " + esc(r.getHotelCity()))
                + MailTemplates.infoRow(I18n.t(lang, "label.room"), esc(r.getRoomTypeName()) + " &times; " + r.getRoomsCount())
                + MailTemplates.infoRow(I18n.t(lang, "label.checkIn"), df.format(r.getCheckIn()))
                + MailTemplates.infoRow(I18n.t(lang, "label.checkOut"), df.format(r.getCheckOut())
                        + " (" + I18n.t(lang, "label.nightsN", r.getNights()) + ")")
                + MailTemplates.infoRow(I18n.t(lang, "label.guests"), String.valueOf(r.getGuests()))
                + MailTemplates.infoRow(I18n.t(lang, "label.payment"), I18n.t(lang, "pay." + r.getPaymentMethod()))
                + MailTemplates.infoRow(I18n.t(lang, "label.total"), "Rs. " + String.format("%,.2f", r.getTotalAmount()))
                + MailTemplates.infoRow(I18n.t(lang, "label.status"), I18n.t(lang, "status." + r.getStatus())));
    }

    /** Guest contact rows, only in admin emails. */
    private static String guestRows(String lang, Reservation r) {
        String rows = MailTemplates.infoRow(I18n.t(lang, "label.guest"), esc(r.getGuestName()))
                + MailTemplates.infoRow(I18n.t(lang, "label.phone"), esc(r.getGuestPhone()))
                + MailTemplates.infoRow(I18n.t(lang, "label.email"), r.getUserEmail() == null ? "-" : esc(r.getUserEmail()));
        if (r.getSpecialRequests() != null) {
            rows += MailTemplates.infoRow(I18n.t(lang, "label.requests"), esc(r.getSpecialRequests()));
        }
        return MailTemplates.infoTable(rows);
    }

    private static String taxiTable(String lang, TaxiBooking t) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd (EEE) HH:mm", I18n.locale(lang));
        String rows = MailTemplates.infoRow(I18n.t(lang, "label.taxiNo"), "#" + t.getId())
                + MailTemplates.infoRow(I18n.t(lang, "taxi.pickupTime"), df.format(t.getPickupTime()))
                + MailTemplates.infoRow(I18n.t(lang, "taxi.pickup"), I18n.t(lang, "taxi.zone." + t.getPickupType())
                        + " &mdash; " + esc(t.getPickupLocation()))
                + MailTemplates.infoRow(I18n.t(lang, "taxi.dropoff"), esc(t.getHotelName()) + "<br>" + esc(t.getHotelAddress()))
                + MailTemplates.infoRow(I18n.t(lang, "taxi.car"), I18n.t(lang, "taxi.car." + t.getCarType()))
                + MailTemplates.infoRow(I18n.t(lang, "taxi.passengers"), String.valueOf(t.getPassengers()))
                + MailTemplates.infoRow(I18n.t(lang, "taxi.fare"), "Rs. " + String.format("%,.0f", t.getEstimatedFare()))
                + MailTemplates.infoRow(I18n.t(lang, "label.status"), I18n.t(lang, "taxiStatus." + t.getStatus()));
        if (t.getDriverName() != null) {
            rows += MailTemplates.infoRow(I18n.t(lang, "taxi.driver"),
                    esc(t.getDriverName()) + (t.getDriverPhone() != null ? " (" + esc(t.getDriverPhone()) + ")" : ""));
        }
        return MailTemplates.infoTable(rows);
    }

    /** A button linking back to the site (only when the site address is known - see AppUrl). */
    private static String button(String lang, String path, String labelKey) {
        String base = AppUrl.get();
        return base == null ? "" : MailTemplates.button(base + path, I18n.t(lang, labelKey));
    }

    private static String now(String lang) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", I18n.locale(lang)).format(new java.util.Date());
    }

    private static String hello(String lang, String name) {
        return p(I18n.t(lang, "mail.hello", esc(name)));
    }

    private static String p(String html) {
        return "<p>" + html + "</p>";
    }

    private static String small(String text) {
        return "<p style=\"color:#6B7280;font-size:13px;\">" + text + "</p>";
    }

    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /** Sends right now; returns true if the SMTP server accepted it. */
    private static boolean sendNow(String lang, String to, String subject, String bodyHtml) {
        if (to == null || to.isBlank()) return false;
        try {
            MailUtil.sendHtmlEmail(to, subject, MailTemplates.wrap(bodyHtml, I18n.t(lang, "mail.footer")));
            return true;
        } catch (Exception e) {
            System.err.println("[Mails] Could not send \"" + subject + "\" to " + to + ": " + e);
            return false;
        }
    }

    /** Puts the email on the background queue; returns true if there was an address to send to. */
    private static boolean queue(String lang, String to, String subject, String bodyHtml) {
        if (to == null || to.isBlank()) return false;     // e.g. the guest deleted their account
        try {
            QUEUE.submit(() -> sendNow(lang, to, subject, bodyHtml));
            return true;
        } catch (java.util.concurrent.RejectedExecutionException e) {
            return sendNow(lang, to, subject, bodyHtml);   // app is shutting down: send directly
        }
    }

    /** Called when the web app stops: finish sending what is queued (max 20 s). */
    public static void shutdown() {
        QUEUE.shutdown();
        try {
            QUEUE.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
