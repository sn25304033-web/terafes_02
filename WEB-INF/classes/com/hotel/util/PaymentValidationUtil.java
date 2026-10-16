package com.hotel.util;

import java.time.YearMonth;

/**
 * Basic client-independent format validation for card details entered at checkout.
 * This is FORMAT validation only (correct number of digits, passes the Luhn checksum,
 * expiry date not in the past) - it does NOT verify the card is real or has funds,
 * since that requires a real payment gateway (Stripe, PayPal, etc).
 */
public class PaymentValidationUtil {

    /** Luhn checksum - catches typos in card numbers (same check real card forms use client-side). */
    public static boolean isValidCardNumber(String rawNumber) {
        if (rawNumber == null) return false;
        String digits = rawNumber.replaceAll("[\\s-]", "");
        if (!digits.matches("\\d{13,19}")) return false;

        int sum = 0;
        boolean doubleDigit = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }

    /** Expects MM/YY format, e.g. "08/29". Rejects blank, malformed, or already-expired dates. */
    public static boolean isValidExpiry(String mmYY) {
        if (mmYY == null || !mmYY.matches("(0[1-9]|1[0-2])/\\d{2}")) return false;
        int month = Integer.parseInt(mmYY.substring(0, 2));
        int year = 2000 + Integer.parseInt(mmYY.substring(3, 5));
        YearMonth expiry = YearMonth.of(year, month);
        return !expiry.isBefore(YearMonth.now());
    }

    /** Accepts digits, spaces, dashes, parentheses, and an optional leading "+"; 7-15 digits total. */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String trimmed = phone.trim();
        if (!trimmed.matches("\\+?[0-9\\-\\s()]{7,20}")) return false;
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        return digitsOnly.length() >= 7 && digitsOnly.length() <= 15;
    }

    public static boolean isValidCvv(String cvv) {
        return cvv != null && cvv.matches("\\d{3,4}");
    }

    public static boolean isValidCardholderName(String name) {
        return name != null && !name.isBlank() && name.trim().length() >= 2;
    }

    /** Returns e.g. "**** **** **** 4242" for safe display - never store or log the full number. */
    public static String maskCardNumber(String rawNumber) {
        String digits = rawNumber.replaceAll("[\\s-]", "");
        String last4 = digits.substring(digits.length() - 4);
        return "**** **** **** " + last4;
    }
}
