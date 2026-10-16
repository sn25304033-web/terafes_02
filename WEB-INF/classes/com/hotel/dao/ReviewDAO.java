package com.hotel.dao;

import com.hotel.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public List<Review> getByHotel(int hotelId) throws SQLException {
        String sql = "SELECT rv.*, u.full_name FROM sn_reviews rv JOIN sn_users u ON u.id = rv.user_id " +
                     "WHERE rv.hotel_id = ? ORDER BY rv.created_at DESC";
        List<Review> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, hotelId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getInt("id"));
                    r.setHotelId(rs.getInt("hotel_id"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setUserName(rs.getString("full_name"));
                    r.setRating(rs.getInt("rating"));
                    r.setCommentText(rs.getString("comment_text"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(r);
                }
            }
        }
        return list;
    }

    /** Insert or update the user's single review for this hotel (Oracle MERGE). */
    public void save(int hotelId, int userId, int rating, String comment) throws SQLException {
        String sql = "MERGE INTO sn_reviews t USING (SELECT ? AS hotel_id, ? AS user_id FROM dual) s " +
                     "ON (t.hotel_id = s.hotel_id AND t.user_id = s.user_id) " +
                     "WHEN MATCHED THEN UPDATE SET t.rating = ?, t.comment_text = ?, t.created_at = CURRENT_TIMESTAMP " +
                     "WHEN NOT MATCHED THEN INSERT (hotel_id, user_id, rating, comment_text) VALUES (s.hotel_id, s.user_id, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, hotelId);
            ps.setInt(2, userId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            ps.setInt(5, rating);
            ps.setString(6, comment);
            ps.executeUpdate();
        }
        new HotelDAO().refreshRating(hotelId);
    }
}
