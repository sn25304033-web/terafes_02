package com.hotel.dao;

import com.hotel.model.TaxiBooking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaxiDAO {

    private static final String BASE_SELECT =
            "SELECT t.*, h.name AS hotel_name, h.address AS hotel_address, r.guest_name, r.guest_phone, " +
            "       r.check_in, u.email AS user_email, u.lang AS user_lang " +
            "FROM sn_taxi_bookings t " +
            "JOIN sn_reservations r ON r.id = t.reservation_id " +
            "JOIN sn_room_types rt ON rt.id = r.room_type_id " +
            "JOIN sn_hotels h ON h.id = rt.hotel_id " +
            "LEFT JOIN sn_users u ON u.id = t.user_id ";

    public int create(TaxiBooking t) throws SQLException {
        String sql = "INSERT INTO sn_taxi_bookings (user_id, reservation_id, pickup_type, pickup_location, pickup_time, " +
                     "passengers, luggage, car_type, estimated_fare, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"})) {
            ps.setInt(1, t.getUserId());
            ps.setInt(2, t.getReservationId());
            ps.setString(3, t.getPickupType());
            ps.setString(4, t.getPickupLocation());
            ps.setTimestamp(5, t.getPickupTime());
            ps.setInt(6, t.getPassengers());
            ps.setInt(7, t.getLuggage());
            ps.setString(8, t.getCarType());
            ps.setBigDecimal(9, t.getEstimatedFare());
            ps.setString(10, t.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public List<TaxiBooking> getByUser(int userId) throws SQLException {
        return query(BASE_SELECT + "WHERE t.user_id = ? ORDER BY t.pickup_time DESC", userId);
    }

    public List<TaxiBooking> getByReservation(int reservationId) throws SQLException {
        return query(BASE_SELECT + "WHERE t.reservation_id = ? ORDER BY t.pickup_time", reservationId);
    }

    public List<TaxiBooking> getAll(String status) throws SQLException {
        boolean filter = status != null && !status.isBlank();
        String sql = BASE_SELECT + (filter ? "WHERE t.status = ? " : "") + "ORDER BY t.pickup_time DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (filter) ps.setString(1, status);
            return mapList(ps);
        }
    }

    public TaxiBooking findById(int id) throws SQLException {
        List<TaxiBooking> list = query(BASE_SELECT + "WHERE t.id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    /** Guest cancel: own ride, still REQUESTED/CONFIRMED, at least 2 hours before pickup. */
    public boolean cancelByUser(int id, int userId) throws SQLException {
        String sql = "UPDATE sn_taxi_bookings SET status = 'CANCELLED' WHERE id = ? AND user_id = ? " +
                     "AND status IN ('REQUESTED','CONFIRMED') AND pickup_time > CURRENT_TIMESTAMP + INTERVAL '2' HOUR";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    /** When a hotel reservation is cancelled its open taxi rides are cancelled too. Returns how many. */
    public int cancelForReservation(int reservationId) throws SQLException {
        String sql = "UPDATE sn_taxi_bookings SET status = 'CANCELLED' WHERE reservation_id = ? " +
                     "AND status IN ('REQUESTED','CONFIRMED')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            return ps.executeUpdate();
        }
    }

    /** Admin: change status and (optionally) assign the driver. */
    public void updateByAdmin(int id, String status, String driverName, String driverPhone) throws SQLException {
        String sql = "UPDATE sn_taxi_bookings SET status = ?, driver_name = ?, driver_phone = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, driverName);
            ps.setString(3, driverPhone);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    /** Open rides picking up within the next 3 hours whose reminder has not been sent yet. */
    public List<TaxiBooking> getDuePickupReminders() throws SQLException {
        String sql = BASE_SELECT + "WHERE t.status IN ('REQUESTED','CONFIRMED') AND t.reminder_sent = 0 " +
                     "AND t.pickup_time > CURRENT_TIMESTAMP AND t.pickup_time <= CURRENT_TIMESTAMP + INTERVAL '3' HOUR";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return mapList(ps);
        }
    }

    public void markReminderSent(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE sn_taxi_bookings SET reminder_sent = 1 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int countByStatus(String status) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM sn_taxi_bookings WHERE status = ?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private List<TaxiBooking> query(String sql, int param) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, param);
            return mapList(ps);
        }
    }

    private List<TaxiBooking> mapList(PreparedStatement ps) throws SQLException {
        List<TaxiBooking> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TaxiBooking t = new TaxiBooking();
                t.setId(rs.getInt("id"));
                t.setUserId(rs.getInt("user_id"));
                t.setReservationId(rs.getInt("reservation_id"));
                t.setPickupType(rs.getString("pickup_type"));
                t.setPickupLocation(rs.getString("pickup_location"));
                t.setPickupTime(rs.getTimestamp("pickup_time"));
                t.setPassengers(rs.getInt("passengers"));
                t.setLuggage(rs.getInt("luggage"));
                t.setCarType(rs.getString("car_type"));
                t.setEstimatedFare(rs.getBigDecimal("estimated_fare"));
                t.setNotes(rs.getString("notes"));
                t.setStatus(rs.getString("status"));
                t.setDriverName(rs.getString("driver_name"));
                t.setDriverPhone(rs.getString("driver_phone"));
                t.setCreatedAt(rs.getTimestamp("created_at"));
                t.setHotelName(rs.getString("hotel_name"));
                t.setHotelAddress(rs.getString("hotel_address"));
                t.setGuestName(rs.getString("guest_name"));
                t.setGuestPhone(rs.getString("guest_phone"));
                t.setCheckIn(rs.getDate("check_in"));
                t.setUserEmail(rs.getString("user_email"));
                t.setUserLang(rs.getString("user_lang"));
                list.add(t);
            }
        }
        return list;
    }
}
