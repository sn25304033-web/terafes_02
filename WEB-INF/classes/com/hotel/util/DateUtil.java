package com.hotel.util;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/** Helpers for the yyyy-MM-dd values sent by &lt;input type="date"&gt;. */
public class DateUtil {

    /** Returns null for blank / malformed input instead of throwing. */
    public static Date parse(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Date.valueOf(LocalDate.parse(value.trim()));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static int nights(Date checkIn, Date checkOut) {
        return (int) ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
    }

    /** Returns an I18n message KEY, or null when the stay dates are valid. */
    public static String validateStay(Date checkIn, Date checkOut) {
        if (checkIn == null || checkOut == null) return "msg.stay.bothDates";
        if (checkIn.toLocalDate().isBefore(LocalDate.now())) return "msg.stay.past";
        if (!checkOut.after(checkIn)) return "msg.stay.order";
        if (nights(checkIn, checkOut) > 30) return "msg.stay.max";
        return null;
    }

    public static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }
}
