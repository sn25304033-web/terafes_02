package com.hotel.dao;

import com.hotel.model.User;
import com.hotel.util.PasswordUtil;

import java.sql.*;

public class UserDAO {

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, full_name, email, password_hash, phone, address, role, email_verified, lang FROM sn_users WHERE email = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, full_name, email, password_hash, phone, address, role, email_verified, lang FROM sn_users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    /** Registers a new customer. Returns the new user's ID, or -1 if the email is already taken. */
    public int register(User u, String plainPassword) throws SQLException {
        if (findByEmail(u.getEmail()) != null) {
            return -1;
        }
        String sql = "INSERT INTO sn_users (full_name, email, password_hash, phone, address, role, lang) " +
                     "VALUES (?, ?, ?, ?, ?, 'CUSTOMER', ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"id"})) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, PasswordUtil.hash(plainPassword));
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getAddress());
            ps.setString(6, u.getLang() == null ? "en" : u.getLang());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void markEmailVerified(int userId) throws SQLException {
        String sql = "UPDATE sn_users SET email_verified = 1 WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    /** Returns the User if email+password match, otherwise null. */
    public User login(String email, String plainPassword) throws SQLException {
        User u = findByEmail(email);
        if (u == null) return null;
        if (PasswordUtil.hash(plainPassword).equals(u.getPasswordHash())) {
            return u;
        }
        return null;
    }

    public void updateProfile(int userId, String fullName, String phone, String address) throws SQLException {
        String sql = "UPDATE sn_users SET full_name = ?, phone = ?, address = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setInt(4, userId);
            ps.executeUpdate();
        }
    }

    public java.util.List<String> getAllAdminEmails() throws SQLException {
        java.util.List<String> emails = new java.util.ArrayList<>();
        String sql = "SELECT email FROM sn_users WHERE role = 'ADMIN'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) emails.add(rs.getString("email"));
        }
        return emails;
    }

    public java.util.List<Integer> getAllAdminIds() throws SQLException {
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        String sql = "SELECT id FROM sn_users WHERE role = 'ADMIN'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) ids.add(rs.getInt("id"));
        }
        return ids;
    }

    /** Updates a user's password hash directly (already-hashed value). */
    public void updatePasswordHash(int userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE sn_users SET password_hash = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** All administrators (email + language) - used for admin alert emails. */
    public java.util.List<User> getAdmins() throws SQLException {
        java.util.List<User> list = new java.util.ArrayList<>();
        String sql = "SELECT id, full_name, email, password_hash, phone, address, role, email_verified, lang " +
                     "FROM sn_users WHERE role = 'ADMIN'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void updateLang(int userId, String lang) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE sn_users SET lang = ? WHERE id = ?")) {
            ps.setString(1, lang);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Permanently deletes an account in one transaction:
     *  - upcoming PENDING/CONFIRMED reservations and open taxi rides are cancelled,
     *  - the user row is deleted (codes, notifications and reviews cascade;
     *    reservation / taxi history is kept for the hotel with user_id = NULL).
     * Returns the ids of the reservations that were cancelled (so admins can be notified).
     */
    public java.util.List<Integer> deleteAccount(int userId) throws SQLException {
        java.util.List<Integer> cancelled = new java.util.ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id FROM sn_reservations WHERE user_id = ? AND status IN ('PENDING','CONFIRMED') " +
                        "AND check_out > TRUNC(SYSDATE)")) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) cancelled.add(rs.getInt(1));
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE sn_reservations SET status = 'CANCELLED' WHERE user_id = ? AND status IN ('PENDING','CONFIRMED') " +
                        "AND check_out > TRUNC(SYSDATE)")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE sn_taxi_bookings SET status = 'CANCELLED' WHERE user_id = ? AND status IN ('REQUESTED','CONFIRMED')")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM sn_users WHERE id = ?")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
        return cancelled;
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPhone(rs.getString("phone"));
        u.setAddress(rs.getString("address"));
        u.setRole(rs.getString("role"));
        u.setEmailVerified(rs.getInt("email_verified") == 1);
        u.setLang(rs.getString("lang"));
        return u;
    }
}
