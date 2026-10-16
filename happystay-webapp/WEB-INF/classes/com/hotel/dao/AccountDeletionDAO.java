package com.hotel.dao;

import java.security.SecureRandom;
import java.sql.*;

public class AccountDeletionDAO {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_VALID_MINUTES = 10;

    /** Generates a random 6-digit code, stores it (valid for 10 minutes), and returns it to email to the user. */
    public String createCode(int userId) throws SQLException {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        String sql = "INSERT INTO sn_account_deletion_codes (user_id, code, expires_at) " +
                     "VALUES (?, ?, CURRENT_TIMESTAMP + INTERVAL '" + CODE_VALID_MINUTES + "' MINUTE)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, code);
            ps.executeUpdate();
        }
        return code;
    }

    /** Returns true and marks the code used if it matches, isn't expired, and hasn't been used already. */
    public boolean validateAndConsume(int userId, String code) throws SQLException {
        String selectSql = "SELECT id FROM sn_account_deletion_codes " +
                            "WHERE user_id = ? AND code = ? AND used = 0 AND expires_at > CURRENT_TIMESTAMP";
        try (Connection con = DBConnection.getConnection()) {
            int foundId = -1;
            try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                ps.setInt(1, userId);
                ps.setString(2, code);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) foundId = rs.getInt("id");
                }
            }
            if (foundId == -1) return false;

            try (PreparedStatement ps = con.prepareStatement("UPDATE sn_account_deletion_codes SET used = 1 WHERE id = ?")) {
                ps.setInt(1, foundId);
                ps.executeUpdate();
            }
            return true;
        }
    }
}
