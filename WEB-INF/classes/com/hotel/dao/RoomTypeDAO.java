package com.hotel.dao;

import com.hotel.model.RoomType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomTypeDAO {

    /**
     * Rooms of one room type already booked (not cancelled) on any night between checkIn and checkOut.
     * Two stays overlap when existing.check_in < new.check_out AND existing.check_out > new.check_in.
     */
    static final String BOOKED_SUBQUERY =
            "NVL((SELECT SUM(r.rooms_count) FROM sn_reservations r " +
            "     WHERE r.room_type_id = rt.id AND r.status <> 'CANCELLED' " +
            "     AND r.check_in < ? AND r.check_out > ?), 0)";

    /** All room types for a hotel. If dates are given, availableRooms is filled in for that stay. */
    public List<RoomType> getByHotel(int hotelId, Date checkIn, Date checkOut) throws SQLException {
        boolean withDates = checkIn != null && checkOut != null;
        String sql = "SELECT rt.*, " +
                     (withDates ? "rt.total_rooms - " + BOOKED_SUBQUERY : "rt.total_rooms") + " AS available_rooms " +
                     "FROM sn_room_types rt WHERE rt.hotel_id = ? ORDER BY rt.price_per_night";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            if (withDates) {
                ps.setDate(i++, checkOut);
                ps.setDate(i++, checkIn);
            }
            ps.setInt(i, hotelId);
            List<RoomType> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        }
    }

    public RoomType findById(int id) throws SQLException {
        String sql = "SELECT rt.*, rt.total_rooms AS available_rooms FROM sn_room_types rt WHERE rt.id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Free rooms of this type for the whole stay. */
    public int countAvailable(Connection con, int roomTypeId, Date checkIn, Date checkOut) throws SQLException {
        String sql = "SELECT rt.total_rooms - " + BOOKED_SUBQUERY + " FROM sn_room_types rt WHERE rt.id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, checkOut);
            ps.setDate(2, checkIn);
            ps.setInt(3, roomTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countAvailable(int roomTypeId, Date checkIn, Date checkOut) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            return countAvailable(con, roomTypeId, checkIn, checkOut);
        }
    }

    public void insert(RoomType r) throws SQLException {
        String sql = "INSERT INTO sn_room_types (hotel_id, name, description, price_per_night, capacity, total_rooms, image_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getHotelId());
            ps.setString(2, r.getName());
            ps.setString(3, r.getDescription());
            ps.setBigDecimal(4, r.getPricePerNight());
            ps.setInt(5, r.getCapacity());
            ps.setInt(6, r.getTotalRooms());
            ps.setString(7, r.getImageUrl());
            ps.executeUpdate();
        }
    }

    public void update(RoomType r) throws SQLException {
        String sql = "UPDATE sn_room_types SET name = ?, description = ?, price_per_night = ?, capacity = ?, " +
                     "total_rooms = ?, image_url = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getName());
            ps.setString(2, r.getDescription());
            ps.setBigDecimal(3, r.getPricePerNight());
            ps.setInt(4, r.getCapacity());
            ps.setInt(5, r.getTotalRooms());
            ps.setString(6, r.getImageUrl());
            ps.setInt(7, r.getId());
            ps.executeUpdate();
        }
    }

    /** Returns false if the room type has reservations and therefore can't be deleted. */
    public boolean delete(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM sn_reservations WHERE room_type_id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return false;
                }
            }
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM sn_room_types WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            return true;
        }
    }

    private RoomType map(ResultSet rs) throws SQLException {
        RoomType r = new RoomType();
        r.setId(rs.getInt("id"));
        r.setHotelId(rs.getInt("hotel_id"));
        r.setName(rs.getString("name"));
        r.setDescription(rs.getString("description"));
        r.setPricePerNight(rs.getBigDecimal("price_per_night"));
        r.setCapacity(rs.getInt("capacity"));
        r.setTotalRooms(rs.getInt("total_rooms"));
        r.setImageUrl(rs.getString("image_url"));
        r.setAvailableRooms(Math.max(0, rs.getInt("available_rooms")));
        return r;
    }
}
