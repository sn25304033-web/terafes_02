package com.hotel.util;

/**
 * Wraps email body content in a styled, branded HTML shell so HappyStay emails
 * look like real e-commerce notification emails rather than plain text.
 */
public class MailTemplates {

    private static final String PRIMARY = "#0F766E";
    private static final String PRIMARY_DARK = "#134E4A";
    private static final String ACCENT = "#D97706";
    private static final String TEXT = "#1E1B2E";
    private static final String TEXT_MUTED = "#6B7280";
    private static final String BG = "#F7F7FB";
    private static final String BORDER = "#E5E7EB";

    /** Wraps an HTML body fragment in the full branded email shell. */
    public static String wrap(String bodyHtml) {
        return wrap(bodyHtml, "This is an automated message &mdash; please do not reply directly to this email.");
    }

    /** Same shell with a translated footer line. */
    public static String wrap(String bodyHtml, String footerNote) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset=\"UTF-8\"></head>" +
                "<body style=\"margin:0;padding:0;background:" + BG + ";font-family:Arial,Helvetica,sans-serif;\">" +
                "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:" + BG + ";padding:32px 16px;\">" +
                "<tr><td align=\"center\">" +
                "<table role=\"presentation\" width=\"520\" cellpadding=\"0\" cellspacing=\"0\" " +
                "style=\"background:#ffffff;border-radius:14px;overflow:hidden;max-width:520px;width:100%;box-shadow:0 2px 10px rgba(30,27,46,0.08);\">" +
                "<tr><td style=\"background:linear-gradient(135deg," + PRIMARY_DARK + "," + PRIMARY + ");padding:24px;text-align:center;\">" +
                "<span style=\"font-size:24px;font-weight:800;color:#ffffff;letter-spacing:0.5px;\">Happy<span style=\"color:" + ACCENT + ";\">Stay</span></span>" +
                "</td></tr>" +
                "<tr><td style=\"padding:32px 28px;color:" + TEXT + ";font-size:14px;line-height:1.6;\">" +
                bodyHtml +
                "</td></tr>" +
                "<tr><td style=\"background:" + TEXT + ";color:#C4C1D6;text-align:center;padding:18px;font-size:12px;\">" +
                "&copy; 2026 HappyStay. All rights reserved.<br>" +
                footerNote +
                "</td></tr>" +
                "</table>" +
                "</td></tr>" +
                "</table>" +
                "</body></html>";
    }

    /** A large, centered verification/reset code display box. */
    public static String codeBox(String code) {
        return "<div style=\"text-align:center;margin:20px 0;\">" +
                "<div style=\"display:inline-block;background:" + BG + ";border:1px dashed " + PRIMARY + ";" +
                "border-radius:10px;padding:16px 32px;font-size:28px;font-weight:800;letter-spacing:8px;color:" + PRIMARY_DARK + ";\">" +
                code +
                "</div></div>";
    }

    /** A styled call-to-action button/link. */
    public static String button(String url, String label) {
        return "<div style=\"text-align:center;margin:24px 0;\">" +
                "<a href=\"" + url + "\" style=\"display:inline-block;background:" + ACCENT + ";color:#ffffff;" +
                "text-decoration:none;font-weight:700;padding:12px 28px;border-radius:24px;font-size:14px;\">" +
                label + "</a></div>";
    }

    /** A simple two-column key/value info table (e.g. order summary rows). */
    public static String infoRow(String label, String value) {
        return "<tr>" +
                "<td style=\"padding:6px 0;color:" + TEXT_MUTED + ";font-size:13px;width:40%;\">" + label + "</td>" +
                "<td style=\"padding:6px 0;color:" + TEXT + ";font-size:13px;font-weight:600;\">" + value + "</td>" +
                "</tr>";
    }

    public static String infoTable(String rowsHtml) {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" " +
                "style=\"border-top:1px solid " + BORDER + ";border-bottom:1px solid " + BORDER + ";margin:16px 0;padding:8px 0;\">" +
                rowsHtml + "</table>";
    }

    /** One order-item line for an order summary table. */
    public static String orderItemRow(String name, int qty, String subtotal) {
        return "<tr>" +
                "<td style=\"padding:8px 0;border-bottom:1px solid " + BORDER + ";font-size:13px;\">" + name + " &times; " + qty + "</td>" +
                "<td style=\"padding:8px 0;border-bottom:1px solid " + BORDER + ";font-size:13px;text-align:right;font-weight:600;\">" + subtotal + "</td>" +
                "</tr>";
    }

    public static String orderItemsTable(String rowsHtml) {
        return "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:16px 0;\">" +
                rowsHtml + "</table>";
    }
}
