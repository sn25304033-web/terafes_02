package com.hotel.dao;

import com.hotel.model.PendingBooking;
import com.hotel.model.Reservation;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private static final String BASE_SELECT =
            "SELECT r.*, rt.name AS room_type_name, rt.image_url AS room_image, h.id AS hotel_id, " +
            "       h.name AS hotel_name, h.city AS hotel_city, h.address AS hotel_address, " +
            "       u.email AS user_email, u.lang AS user_lang " +
            "FROM sn_reservations r " +
            "JOIN sn_room_types rt ON rt.id = r.room_type_id " +
            "JOIN sn_hotels h ON h.id = rt.hotel_id " +
            "LEFT JOIN sn_users u ON u.id = r.user_id ";   // LEFT: keep history of deleted accounts

    /** Thrown when the rooms were taken by someone else between the search and the confirm click. */
    public static class NotAvailableException extends Exception {
        private final int available;
        public NotAvailableException(int available) {
            super("Only " + available + " room(s) left");
            this.available = available;
        }
        public int getAvailable() { return available; }
    }

    /**
     * Creates a reservation inside one transaction.
     * The room_types row is locked with SELECT ... FOR UPDATE so two people can't book the
     * last room at the same moment (the second one waits, then sees 0 available).
     */
    public int create(int userId, PendingBooking b) throws SQLException, NotAvailableException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement lock = con.prepareStatement("SELECT id FROM sn_room_types WHERE id = ? FOR UPDATE")) {
                    lock.setInt(1, b.getRoomTypeId());
                    lock.executeQuery().close();
                }
                int available = new RoomTypeDAO().countAvailable(con, b.getRoomTypeId(), b.getCheckIn(), b.getCheckOut());
                if (available < b.getRoomsCount()) {
                    con.rollback();
                    throw new NotAvailableException(Math.max(available, 0));
                }

                String sql = "INSERT INTO sn_reservations (user_id, room_type_id, check_in, check_out, rooms_count, guests, " +
                             "nights, total_amount, guest_name, guest_phone, special_requests, payment_method, card_last4, status) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                int newId;
                try (PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"})) {
                    ps.setInt(1, userId);
                    ps.setInt(2, b.getRoomTypeId());
                    ps.setDate(3, b.getCheckIn());
                    ps.setDate(4, b.getCheckOut());
                    ps.setInt(5, b.getRoomsCount());
                    ps.setInt(6, b.getGuests());
                    ps.setInt(7, b.getNights());
                    ps.setBigDecimal(8, b.getTotalAmount());
                    ps.setString(9, b.getGuestName());
                    ps.setString(10, b.getGuestPhone());
                    ps.setString(11, b.getSpecialRequests());
                    ps.setString(12, b.getPaymentMethod());
                    ps.setString(13, b.getCardLast4());
                    // Paid online -> confirmed straight away; pay at hotel -> admin confirms.
                    ps.setString(14, "PAY_AT_HOTEL".equals(b.getPaymentMethod()) ? "PENDING" : "CONFIRMED");
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        newId = keys.getInt(1);
                    }
                }
                con.commit();
                return newId;
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<Reservation> getByUser(int userId) throws SQLException {
        String sql = BASE_SELECT + "WHERE r.user_id = ? ORDER BY r.check_in DESC, r.id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return mapList(ps);
        }
    }

    public List<Reservation> getAll(String statusFilter) throws SQLException {
        boolean filter = statusFilter != null && !statusFilter.isBlank();
        String sql = BASE_SELECT + (filter ? "WHERE r.status = ? " : "") + "ORDER BY r.created_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (filter) ps.setString(1, statusFilter);
            return mapList(ps);
        }
    }

    /** Guests arriving today (for the admin dashboard). */
    public List<Reservation> getArrivalsToday() throws SQLException {
        String sql = BASE_SELECT + "WHERE r.check_in = TRUNC(SYSDATE) AND r.status IN ('PENDING','CONFIRMED') ORDER BY h.name";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return mapList(ps);
        }
    }

    public Reservation findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE r.id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            List<Reservation> list = mapList(ps);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE sn_reservations SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Guest-side cancel: only their own, only while PENDING/CONFIRMED and before check-in. */
    public boolean cancelByUser(int id, int userId) throws SQLException {
        String sql = "UPDATE sn_reservations SET status = 'CANCELLED' WHERE id = ? AND user_id = ? " +
                     "AND status IN ('PENDING','CONFIRMED') AND check_in >= TRUNC(SYSDATE)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    /** A guest may review a hotel once they've stayed there (checked in or completed). */
    public boolean hasStayed(int userId, int hotelId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sn_reservations r JOIN sn_room_types rt ON rt.id = r.room_type_id " +
                     "WHERE r.user_id = ? AND rt.hotel_id = ? AND r.status IN ('CHECKED_IN','COMPLETED')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, hotelId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Active stays starting tomorrow whose reminder email has not been sent yet. */
    public List<Reservation> getDueCheckInReminders() throws SQLException {
        String sql = BASE_SELECT + "WHERE r.status IN ('PENDING','CONFIRMED') AND r.check_in = TRUNC(SYSDATE) + 1 " +
                     "AND r.reminder_sent = 0 AND r.user_id IS NOT NULL";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return mapList(ps);
        }
    }

    public void markReminderSent(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE sn_reservations SET reminder_sent = 1 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public BigDecimal totalRevenue() throws SQLException {
        String sql = "SELECT NVL(SUM(total_amount), 0) FROM sn_reservations WHERE status <> 'CANCELLED'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }

    public int countByStatus(String status) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM sn_reservations WHERE status = ?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private List<Reservation> mapList(PreparedStatement ps) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setUserId(rs.getInt("user_id"));
                r.setRoomTypeId(rs.getInt("room_type_id"));
                r.setCheckIn(rs.getDate("check_in"));
                r.setCheckOut(rs.getDate("check_out"));
                r.setRoomsCount(rs.getInt("rooms_count"));
                r.setGuests(rs.getInt("guests"));
                r.setNights(rs.getInt("nights"));
                r.setTotalAmount(rs.getBigDecimal("total_amount"));
                r.setGuestName(rs.getString("guest_name"));
                r.setGuestPhone(rs.getString("guest_phone"));
                r.setSpecialRequests(rs.getString("special_requests"));
                r.setPaymentMethod(rs.getString("payment_method"));
                r.setCardLast4(rs.getString("card_last4"));
                r.setStatus(rs.getString("status"));
                r.setCreatedAt(rs.getTimestamp("created_at"));
                r.setRoomTypeName(rs.getString("room_type_name"));
                r.setImageUrl(rs.getString("room_image"));
                r.setHotelId(rs.getInt("hotel_id"));
                r.setHotelName(rs.getString("hotel_name"));
                r.setHotelCity(rs.getString("hotel_city"));
                r.setUserEmail(rs.getString("user_email"));
                r.setUserLang(rs.getString("user_lang"));
                r.setHotelAddress(rs.getString("hotel_address"));
                list.add(r);
            }
        }
        return list;
    }
}
