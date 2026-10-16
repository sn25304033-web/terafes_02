package com.hotel.dao;

import com.hotel.model.Hotel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelDAO {

    /** Hotel columns + cheapest room price + review count, used by every list query. */
    private static final String BASE_SELECT =
            "SELECT h.id, h.name, h.city, h.address, h.description, h.stars, h.rating, h.image_url, " +
            "       h.amenities, h.active, " +
            "       (SELECT MIN(rt.price_per_night) FROM sn_room_types rt WHERE rt.hotel_id = h.id) AS min_price, " +
            "       (SELECT COUNT(*) FROM sn_reviews rv WHERE rv.hotel_id = h.id) AS review_count " +
            "FROM sn_hotels h ";

    public List<Hotel> getTopRated(int limit) throws SQLException {
        // ROWNUM subquery (same approach as NovaCart's ProductDAO) for wide Oracle driver compatibility.
        String sql = "SELECT * FROM (" + BASE_SELECT + "WHERE h.active = 1 ORDER BY h.rating DESC, h.id) WHERE ROWNUM <= ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            return mapList(ps);
        }
    }

    public List<Hotel> getAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY h.id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return mapList(ps);
        }
    }

    /**
     * Search by city / hotel name keyword and minimum guest capacity.
     * If dates are given, only hotels that still have at least one free room of a type
     * that fits the guests for the whole stay are returned.
     */
    public List<Hotel> search(String keyword, Date checkIn, Date checkOut, int guests) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE h.active = 1 ");
        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (LOWER(h.city) LIKE ? OR LOWER(h.name) LIKE ?) ");
            String like = "%" + keyword.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
        }
        if (checkIn != null && checkOut != null) {
            sql.append("AND EXISTS (SELECT 1 FROM sn_room_types rt WHERE rt.hotel_id = h.id AND rt.capacity >= ? ")
               .append("AND rt.total_rooms - NVL((SELECT SUM(r.rooms_count) FROM sn_reservations r ")
               .append("   WHERE r.room_type_id = rt.id AND r.status <> 'CANCELLED' ")
               .append("   AND r.check_in < ? AND r.check_out > ?), 0) > 0) ");
            params.add(Math.max(guests, 1));
            params.add(checkOut);
            params.add(checkIn);
        } else if (guests > 0) {
            sql.append("AND EXISTS (SELECT 1 FROM sn_room_types rt WHERE rt.hotel_id = h.id AND rt.capacity >= ?) ");
            params.add(guests);
        }
        sql.append("ORDER BY h.rating DESC, h.name");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            return mapList(ps);
        }
    }

    public List<String> getCities() throws SQLException {
        List<String> cities = new ArrayList<>();
        String sql = "SELECT DISTINCT city FROM sn_hotels WHERE active = 1 ORDER BY city";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) cities.add(rs.getString(1));
        }
        return cities;
    }

    public Hotel findById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE h.id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            List<Hotel> list = mapList(ps);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    public int insert(Hotel h) throws SQLException {
        String sql = "INSERT INTO sn_hotels (name, city, address, description, stars, image_url, amenities, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"})) {
            fill(ps, h);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void update(Hotel h) throws SQLException {
        String sql = "UPDATE sn_hotels SET name = ?, city = ?, address = ?, description = ?, stars = ?, " +
                     "image_url = ?, amenities = ?, active = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            fill(ps, h);
            ps.setInt(9, h.getId());
            ps.executeUpdate();
        }
    }

    /** Hotels with reservations can't be hard-deleted (FK), so we just hide them. */
    public void setActive(int id, boolean active) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE sn_hotels SET active = ? WHERE id = ?")) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Recalculates hotels.rating from the reviews table (called after a review is saved). */
    public void refreshRating(int hotelId) throws SQLException {
        String sql = "UPDATE sn_hotels SET rating = NVL((SELECT ROUND(AVG(rating), 1) FROM sn_reviews WHERE hotel_id = ?), 0) WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, hotelId);
            ps.setInt(2, hotelId);
            ps.executeUpdate();
        }
    }

    private void fill(PreparedStatement ps, Hotel h) throws SQLException {
        ps.setString(1, h.getName());
        ps.setString(2, h.getCity());
        ps.setString(3, h.getAddress());
        ps.setString(4, h.getDescription());
        ps.setInt(5, h.getStars());
        ps.setString(6, h.getImageUrl());
        ps.setString(7, h.getAmenities());
        ps.setInt(8, h.isActive() ? 1 : 0);
    }

    private List<Hotel> mapList(PreparedStatement ps) throws SQLException {
        List<Hotel> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Hotel h = new Hotel();
                h.setId(rs.getInt("id"));
                h.setName(rs.getString("name"));
                h.setCity(rs.getString("city"));
                h.setAddress(rs.getString("address"));
                h.setDescription(rs.getString("description"));
                h.setStars(rs.getInt("stars"));
                h.setRating(rs.getBigDecimal("rating"));
                h.setImageUrl(rs.getString("image_url"));
                h.setAmenities(rs.getString("amenities"));
                h.setActive(rs.getInt("active") == 1);
                h.setMinPrice(rs.getBigDecimal("min_price"));
                h.setReviewCount(rs.getInt("review_count"));
                list.add(h);
            }
        }
        return list;
    }
}
