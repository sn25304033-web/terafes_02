package com.hotel.dao;

import com.hotel.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public void create(int userId, String message, String link) throws SQLException {
        String sql = "INSERT INTO sn_notifications (user_id, message, link) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.setString(3, link);
            ps.executeUpdate();
        }
    }

    /** Sends the same notification to every ADMIN user (e.g. "new order placed"). */
    public void notifyAllAdmins(String message, String link) throws SQLException {
        List<Integer> adminIds = new UserDAO().getAllAdminIds();
        String sql = "INSERT INTO sn_notifications (user_id, message, link) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int adminId : adminIds) {
                ps.setInt(1, adminId);
                ps.setString(2, message);
                ps.setString(3, link);
                ps.addBatch();
            }
            if (!adminIds.isEmpty()) ps.executeBatch();
        }
    }

    /** Notifies every ADMIN, each in their own saved language (message = I18n key + arguments). */
    public void notifyAdmins(String key, String link, Object... args) throws SQLException {
        String sql = "SELECT id, lang FROM sn_users WHERE role = 'ADMIN'";
        java.util.Map<Integer, String> admins = new java.util.LinkedHashMap<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) admins.put(rs.getInt(1), rs.getString(2));
        }
        for (java.util.Map.Entry<Integer, String> a : admins.entrySet()) {
            create(a.getKey(), com.hotel.util.I18n.t(a.getValue(), key, args), link);
        }
    }

    public List<Notification> getRecentForUser(int userId, int limit) throws SQLException {
        List<Notification> list = new ArrayList<>();
        // ROWNUM subquery used instead of "FETCH FIRST ? ROWS ONLY" for wider driver compatibility.
        String sql = "SELECT * FROM (SELECT id, user_id, message, link, is_read, created_at " +
                     "FROM sn_notifications WHERE user_id = ? ORDER BY created_at DESC) WHERE ROWNUM <= ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int countUnread(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sn_notifications WHERE user_id = ? AND is_read = 0";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void delete(int userId, int notificationId) throws SQLException {
        String sql = "DELETE FROM sn_notifications WHERE id = ? AND user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void deleteAllForUser(int userId) throws SQLException {
        String sql = "DELETE FROM sn_notifications WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void markAllRead(int userId) throws SQLException {
        String sql = "UPDATE sn_notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setUserId(rs.getInt("user_id"));
        n.setMessage(rs.getString("message"));
        n.setLink(rs.getString("link"));
        n.setRead(rs.getInt("is_read") == 1);
        n.setCreatedAt(rs.getTimestamp("created_at"));
        return n;
    }
}
