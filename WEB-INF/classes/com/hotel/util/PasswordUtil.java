package com.hotel.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Basic SHA-256 + fixed salt password hashing.
 * NOTE: for a real production system prefer BCrypt or Argon2 (per-user random salt +
 * work factor) via a library such as org.mindrot:jbcrypt. SHA-256 with a fixed salt is
 * used here to avoid adding an extra dependency for a learning/demo project.
 */
public class PasswordUtil {

    // Must match the salt used in database/schema.sql's SHA2(CONCAT('hotelsalt2026', ...))
    private static final String SALT = "hotelsalt2026";

    public static String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest((SALT + plainPassword).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
