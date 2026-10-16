package com.hotel.job;

import com.hotel.dao.ReservationDAO;
import com.hotel.dao.TaxiDAO;
import com.hotel.model.Reservation;
import com.hotel.model.TaxiBooking;
import com.hotel.util.I18n;
import com.hotel.util.Mails;

/**
 * Runs every 15 minutes (started by AppLifecycleListener):
 *  - check-in reminder to guests whose stay starts tomorrow (with their taxi, or a "book a taxi" button),
 *  - pickup reminder about 3 hours before each taxi ride.
 * reminder_sent columns make sure each email goes out only once, even after a restart.
 */
public class ReminderJob implements Runnable {

    @Override
    public void run() {
        try {
            ReservationDAO rDao = new ReservationDAO();
            TaxiDAO tDao = new TaxiDAO();
            for (Reservation r : rDao.getDueCheckInReminders()) {
                rDao.markReminderSent(r.getId());   // mark first: never send twice
                Mails.checkInReminder(I18n.normalize(r.getUserLang()), r.getUserEmail(), r, tDao.getByReservation(r.getId()));
            }
            for (TaxiBooking t : tDao.getDuePickupReminders()) {
                tDao.markReminderSent(t.getId());
                Mails.taxiReminder(I18n.normalize(t.getUserLang()), t.getUserEmail(), t);
            }
        } catch (Exception e) {
            // Never let an exception kill the scheduler; try again next time.
            System.err.println("[ReminderJob] " + e.getMessage());
        }
    }
}
