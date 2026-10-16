package com.hotel.util;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small SMTP client that uses only the JDK (no JavaMail / Jakarta Mail jar needed).
 *
 * Port 587 -> STARTTLS (Gmail's normal submission port).
 * Port 465 -> implicit TLS (use this if your network blocks 587).
 *
 * Configured via WEB-INF/classes/mail.properties (read again on every send, so a changed
 * file is picked up after a redeploy without relying on static state):
 *   mail.smtp.host=smtp.gmail.com
 *   mail.smtp.port=587
 *   mail.smtp.username=you@gmail.com
 *   mail.smtp.password=your-16-letter-app-password   (spaces are ignored)
 *   mail.smtp.from=you@gmail.com
 *   mail.smtp.fromName=HappyStay
 *   mail.admin.notify=owner@gmail.com   (optional: extra address(es) that also get every admin alert)
 *   mail.admin.lang=en                  (optional: language of those admin mails)
 *
 * What the messages look like (so Gmail and other providers accept and display them correctly):
 *   - every line ends with CRLF and no line is longer than 78 characters (bodies are base64),
 *   - HTML mails carry a plain-text alternative,
 *   - Subject / sender name use RFC 2047 encoding (Japanese, Nepali, Sinhala, Burmese all work),
 *   - Date and Message-ID headers are present.
 *
 * NOTE: Gmail needs an "App Password" (2-Step Verification must be on); the normal account
 * password is rejected with "535 Username and Password not accepted".
 */
public class MailUtil {

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 20_000;
    private static final String EHLO_NAME = "localhost";
    private static final String CRLF = "\r\n";

    private static final class Config {
        String host;
        int port;
        String username;
        String password;
        String from;
        String fromName;
    }

    public static void sendEmail(String toAddress, String subject, String bodyText) throws IOException {
        send(toAddress, subject, bodyText, false);
    }

    /** Sends an HTML email. Wrap the body with MailTemplates.wrap(...) for the branded HappyStay look. */
    public static void sendHtmlEmail(String toAddress, String subject, String bodyHtml) throws IOException {
        send(toAddress, subject, bodyHtml, true);
    }

    // =====================================================================
    //  Extra admin addresses (optional)
    // =====================================================================

    /**
     * Addresses from mail.admin.notify (comma, semicolon or space separated). These also receive every
     * admin alert (new booking, cancellation, taxi ...) in addition to the ADMIN users stored in the database.
     * Never throws: if the file or key is missing, or an address is invalid, it is simply skipped.
     */
    public static java.util.List<String> adminNotifyAddresses() {
        java.util.List<String> result = new java.util.ArrayList<>();
        try {
            Properties p = readProperties();
            String raw = value(p, "mail.admin.notify");
            for (String part : raw.split("[,;\\s]+")) {
                if (part.isBlank()) continue;
                try {
                    String a = cleanAddress(part);
                    if (!result.contains(a)) result.add(a);
                } catch (IOException invalid) {
                    System.err.println("[MailUtil] Ignoring invalid mail.admin.notify address: " + part);
                }
            }
        } catch (IOException e) {
            // mail.properties missing: nothing extra to add
        }
        return result;
    }

    /** Language for the extra admin addresses (mail.admin.lang, default "en"). */
    public static String adminNotifyLang() {
        try {
            String l = value(readProperties(), "mail.admin.lang");
            return l.isEmpty() ? "en" : l;
        } catch (IOException e) {
            return "en";
        }
    }

    // =====================================================================
    //  Sending
    // =====================================================================

    private static void send(String toAddress, String subject, String body, boolean isHtml) throws IOException {
        Config cfg = loadConfig();
        String to = cleanAddress(toAddress);          // also blocks CR/LF "command injection" through the address
        String message = buildMessage(cfg, to, subject, body, isHtml);

        try (Smtp smtp = Smtp.open(cfg)) {
            smtp.command("AUTH LOGIN", "334");
            smtp.command(base64(cfg.username), "334");
            smtp.command(base64(cfg.password), "235");
            smtp.command("MAIL FROM:<" + cfg.from + ">", "250");
            smtp.command("RCPT TO:<" + to + ">", "250");
            smtp.command("DATA", "354");
            smtp.data(message);                       // server answers 250 = accepted for delivery
            try {
                smtp.command("QUIT", "221");
            } catch (IOException ignored) {
                // The mail was already accepted; a late QUIT problem must not report "not sent".
            }
        }
        System.out.println("[MailUtil] Sent \"" + subject + "\" to " + to);
    }

    /** One SMTP connection: plain or implicit-TLS connect, optional STARTTLS upgrade, line commands. */
    private static final class Smtp implements AutoCloseable {
        private Socket socket;
        private BufferedReader in;
        private OutputStream out;

        static Smtp open(Config cfg) throws IOException {
            Smtp s = new Smtp();
            Socket plain = new Socket();
            s.socket = plain;                          // so close() works even if connect() fails
            try {
                plain.connect(new InetSocketAddress(cfg.host, cfg.port), CONNECT_TIMEOUT_MS);
                plain.setSoTimeout(READ_TIMEOUT_MS);   // never hang a request thread forever
                if (cfg.port == 465) {
                    s.use(upgrade(plain, cfg.host, cfg.port));      // TLS from the first byte
                    s.expect("220");
                    s.command("EHLO " + EHLO_NAME, "250");
                } else {
                    s.use(plain);
                    s.expect("220");
                    s.command("EHLO " + EHLO_NAME, "250");
                    s.command("STARTTLS", "220");
                    s.use(upgrade(plain, cfg.host, cfg.port));      // now encrypted
                    s.command("EHLO " + EHLO_NAME, "250");          // must say EHLO again after STARTTLS
                }
                return s;
            } catch (IOException | RuntimeException e) {
                s.close();
                throw e;
            }
        }

        private void use(Socket sock) throws IOException {
            this.socket = sock;
            this.in = new BufferedReader(new InputStreamReader(sock.getInputStream(), StandardCharsets.UTF_8));
            this.out = sock.getOutputStream();
        }

        String command(String line, String expectedCode) throws IOException {
            writeRaw(line + CRLF);
            return expect(expectedCode);
        }

        /** Sends the message after "354", dot-stuffed and terminated with CRLF.CRLF. */
        void data(String message) throws IOException {
            String m = message.startsWith(".") ? "." + message : message;
            m = m.replace(CRLF + ".", CRLF + "..");
            if (!m.endsWith(CRLF)) m += CRLF;
            writeRaw(m + "." + CRLF);
            expect("250");
        }

        private void writeRaw(String text) throws IOException {
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        /** Reads a (possibly multi-line "250-..." / "250 ...") reply and checks its code. */
        String expect(String expectedCode) throws IOException {
            StringBuilder all = new StringBuilder();
            String line;
            String last;
            do {
                line = in.readLine();
                if (line == null) {
                    throw new IOException("SMTP server closed the connection unexpectedly"
                            + (all.length() > 0 ? " (last reply: " + all + ")" : ""));
                }
                if (all.length() > 0) all.append(" | ");
                all.append(line);
                last = line;
            } while (line.length() >= 4 && line.charAt(3) == '-');

            if (!last.startsWith(expectedCode)) {
                throw new IOException(explain(expectedCode, last));
            }
            return all.toString();
        }

        @Override
        public void close() {
            try {
                if (socket != null) socket.close();
            } catch (IOException ignored) {
                // nothing useful to do
            }
        }
    }

    /** Upgrades a connected socket to TLS and checks that the certificate belongs to the host name. */
    private static SSLSocket upgrade(Socket plain, String host, int port) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket ssl = (SSLSocket) factory.createSocket(plain, host, port, true);
        SSLParameters params = ssl.getSSLParameters();
        params.setEndpointIdentificationAlgorithm("HTTPS");   // reject a certificate for another host
        ssl.setSSLParameters(params);
        ssl.setSoTimeout(READ_TIMEOUT_MS);
        ssl.startHandshake();
        return ssl;
    }

    private static String explain(String expectedCode, String reply) {
        String text = "Unexpected SMTP response (expected " + expectedCode + "): " + reply;
        if (reply.startsWith("535") || reply.startsWith("534") || reply.startsWith("530")) {
            text += "  --> Login was refused. For Gmail: turn on 2-Step Verification, create an App Password at "
                    + "https://myaccount.google.com/apppasswords and put it in mail.smtp.password; "
                    + "mail.smtp.username must be the full Gmail address.";
        }
        return text;
    }

    // =====================================================================
    //  Configuration and address checks
    // =====================================================================

    private static Properties readProperties() throws IOException {
        Properties p = new Properties();
        try (InputStream in = MailUtil.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (in == null) {
                throw new IOException("Mail is not configured: WEB-INF/classes/mail.properties was not found");
            }
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return p;
    }

    private static Config loadConfig() throws IOException {
        Properties p = readProperties();

        Config c = new Config();
        c.host = value(p, "mail.smtp.host");
        c.username = value(p, "mail.smtp.username");
        // Google shows App Passwords as "abcd efgh ijkl mnop"; the real password has no spaces.
        c.password = value(p, "mail.smtp.password").replaceAll("\\s+", "");
        c.from = value(p, "mail.smtp.from");
        if (c.from.isEmpty()) c.from = c.username;
        c.fromName = value(p, "mail.smtp.fromName");
        if (c.fromName.isEmpty()) c.fromName = "HappyStay";

        String portText = value(p, "mail.smtp.port");
        try {
            c.port = portText.isEmpty() ? 587 : Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            throw new IOException("Mail is not configured: mail.smtp.port must be a number (587 or 465)");
        }

        if (c.host.isEmpty() || c.username.isEmpty() || c.password.isEmpty()) {
            throw new IOException("Mail is not configured: mail.smtp.host, username and password are required in mail.properties");
        }
        if (c.username.equalsIgnoreCase("you@gmail.com") || c.password.equals("your-16-char-app-password")) {
            throw new IOException("Mail is not configured: mail.properties still contains the sample username/password");
        }
        c.from = cleanAddress(c.from);
        return c;
    }

    private static String value(Properties p, String key) {
        String v = p.getProperty(key);
        return v == null ? "" : v.trim();
    }

    /** Accepts only a plain ASCII address (no spaces, quotes, angle brackets, CR or LF). */
    private static String cleanAddress(String address) throws IOException {
        String a = address == null ? "" : address.trim();
        int at = a.indexOf('@');
        if (a.length() > 254 || at < 1 || at != a.lastIndexOf('@') || at == a.length() - 1
                || !a.matches("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~@-]+")) {
            throw new IOException("Invalid e-mail address: " + a.replaceAll("[\\r\\n]", " "));
        }
        return a;
    }

    // =====================================================================
    //  Building the message
    // =====================================================================

    private static String buildMessage(Config c, String to, String subject, String body, boolean isHtml) {
        String safeSubject = (subject == null ? "" : subject).replaceAll("[\\r\\n]+", " ").trim();
        String safeBody = body == null ? "" : body;
        String domain = c.from.substring(c.from.indexOf('@') + 1);

        StringBuilder m = new StringBuilder();
        m.append("Date: ").append(DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now())).append(CRLF);
        m.append("From: ").append(formatFrom(c)).append(CRLF);
        m.append("To: ").append(to).append(CRLF);
        m.append("Subject: ").append(encodeHeader(safeSubject)).append(CRLF);
        m.append("Message-ID: <").append(UUID.randomUUID()).append('@').append(domain).append('>').append(CRLF);
        m.append("MIME-Version: 1.0").append(CRLF);

        if (isHtml) {
            String boundary = "=_hs_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
            m.append("Content-Type: multipart/alternative; boundary=\"").append(boundary).append('"').append(CRLF).append(CRLF);
            m.append("--").append(boundary).append(CRLF).append(part("text/plain", htmlToText(safeBody)));
            m.append("--").append(boundary).append(CRLF).append(part("text/html", safeBody));
            m.append("--").append(boundary).append("--").append(CRLF);
        } else {
            m.append(part("text/plain", safeBody.replace("\r\n", "\n").replace('\r', '\n').replace("\n", CRLF)));
        }
        return m.toString();
    }

    /** MIME part headers + blank line + base64 body (76-character lines, so no line is ever too long). */
    private static String part(String mimeType, String text) {
        String encoded = Base64.getMimeEncoder(76, CRLF.getBytes(StandardCharsets.US_ASCII))
                .encodeToString(text.getBytes(StandardCharsets.UTF_8));
        return "Content-Type: " + mimeType + "; charset=UTF-8" + CRLF
                + "Content-Transfer-Encoding: base64" + CRLF + CRLF
                + encoded + CRLF;
    }

    private static String formatFrom(Config c) {
        String name = c.fromName.replaceAll("[\\r\\n]+", " ").trim();
        if (name.isEmpty()) return "<" + c.from + ">";
        if (isPlainAscii(name)) {
            return "\"" + name.replace("\\", "\\\\").replace("\"", "\\\"") + "\" <" + c.from + ">";
        }
        return encodeHeader(name) + " <" + c.from + ">";
    }

    private static boolean isPlainAscii(String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch < 0x20 || ch > 0x7E) return false;
        }
        return true;
    }

    /** RFC 2047 "encoded words" (=?UTF-8?B?...?=), split so every word stays under 75 characters. */
    private static String encodeHeader(String text) {
        if (isPlainAscii(text)) return text;
        StringBuilder out = new StringBuilder();
        StringBuilder chunk = new StringBuilder();
        int chunkBytes = 0;
        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            String ch = new String(Character.toChars(cp));
            int n = ch.getBytes(StandardCharsets.UTF_8).length;
            if (chunkBytes + n > 42) {
                appendEncodedWord(out, chunk.toString());
                chunk.setLength(0);
                chunkBytes = 0;
            }
            chunk.append(ch);
            chunkBytes += n;
            i += Character.charCount(cp);
        }
        if (chunk.length() > 0) appendEncodedWord(out, chunk.toString());
        return out.toString();
    }

    private static void appendEncodedWord(StringBuilder out, String text) {
        if (out.length() > 0) out.append(CRLF).append(' ');      // header folding between words
        out.append("=?UTF-8?B?").append(base64(text)).append("?=");
    }

    private static String base64(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    // =====================================================================
    //  Plain-text version of an HTML mail
    // =====================================================================

    private static final Pattern HEAD_STYLE_SCRIPT = Pattern.compile("(?is)<(head|style|script)\\b.*?</\\1>");
    private static final Pattern ANCHOR = Pattern.compile("(?is)<a\\s[^>]*?href=\"([^\"]*)\"[^>]*>(.*?)</a>");
    private static final Pattern BLOCK_END = Pattern.compile("(?i)<br\\s*/?>|</(p|div|tr|h[1-6]|table)>");
    private static final Pattern CELL_END = Pattern.compile("(?i)</t[dh]>");
    private static final Pattern ANY_TAG = Pattern.compile("(?s)<[^>]*>");
    private static final Pattern NUMERIC_ENTITY = Pattern.compile("&#(\\d{1,7});");

    static String htmlToText(String html) {
        String t = HEAD_STYLE_SCRIPT.matcher(html).replaceAll("");
        t = ANCHOR.matcher(t).replaceAll("$2 ($1)");
        t = BLOCK_END.matcher(t).replaceAll("\n");
        t = CELL_END.matcher(t).replaceAll("  ");
        t = ANY_TAG.matcher(t).replaceAll("");

        t = t.replace("&nbsp;", " ").replace("&mdash;", "\u2014").replace("&times;", "\u00d7")
                .replace("&middot;", "\u00b7").replace("&copy;", "\u00a9")
                .replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
        Matcher m = NUMERIC_ENTITY.matcher(t);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            int cp = Integer.parseInt(m.group(1));
            String rep = Character.isValidCodePoint(cp) ? new String(Character.toChars(cp)) : "";
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        t = sb.toString().replace("&amp;", "&");

        StringBuilder out = new StringBuilder();
        int blank = 0;
        for (String line : t.split("\n")) {
            String l = line.replaceAll("[ \\t\\u00a0]+", " ").trim();
            if (l.isEmpty()) {
                if (++blank > 1) continue;
            } else {
                blank = 0;
            }
            out.append(l).append(CRLF);
        }
        return out.toString().trim();
    }
}
