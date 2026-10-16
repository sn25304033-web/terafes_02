package com.hotel.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Tiny i18n helper. Texts live in WEB-INF/classes/messages_XX.properties (UTF-8, Java 9+).
 * JSPs use &lt;fmt:message key="..."/&gt; (the LanguageFilter tells JSTL which locale to use);
 * Java code (flash messages, errors, emails) uses I18n.t(request, "key", args...).
 */
public class I18n {

    public static final String DEFAULT = "en";

    /** code -> name shown in the language menu (each in its own language). */
    public static final Map<String, String> LANGUAGES = new LinkedHashMap<>();
    static {
        LANGUAGES.put("en", "English");
        LANGUAGES.put("ja", "日本語");
        LANGUAGES.put("ne", "नेपाली");
        LANGUAGES.put("si", "සිංහල");
        LANGUAGES.put("my", "မြန်မာ");
    }

    public static boolean isSupported(String code) {
        return code != null && LANGUAGES.containsKey(code);
    }

    public static String normalize(String code) {
        return isSupported(code) ? code : DEFAULT;
    }

    /** Current language of this visitor (set by LanguageFilter). */
    public static String lang(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        Object l = (s == null) ? null : s.getAttribute("lang");
        return normalize(l == null ? null : l.toString());
    }

    /**
     * Locale used for texts, numbers and dates. "-u-nu-latn" keeps digits as 0-9 in every language
     * (otherwise Nepali / Burmese would show prices and booking numbers in their own digits).
     */
    public static Locale locale(String lang) {
        return Locale.forLanguageTag(normalize(lang) + "-u-nu-latn");
    }

    public static String t(HttpServletRequest req, String key, Object... args) {
        return t(lang(req), key, args);
    }

    /** Translate for an explicit language (e.g. an email to a guest in their saved language). */
    public static String t(String lang, String key, Object... args) {
        String pattern;
        try {
            pattern = ResourceBundle.getBundle("messages", locale(lang)).getString(key);
        } catch (MissingResourceException e) {
            return "???" + key + "???";
        }
        if (args == null || args.length == 0) return pattern;
        return new MessageFormat(pattern, locale(lang)).format(args);
    }
}
