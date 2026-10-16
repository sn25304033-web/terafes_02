package com.hotel.filter;

import com.hotel.util.AppUrl;
import com.hotel.util.I18n;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Locale;

/**
 * Decides the UI language for every request:
 *   session "lang"  ->  "lang" cookie  ->  browser Accept-Language  ->  English.
 * Then tells JSTL (&lt;fmt:message&gt;, &lt;fmt:formatDate&gt;) to use that locale and the "messages" bundle,
 * using the request-scoped JSTL config attributes (same as Config.set(request, ...)).
 */
@WebFilter(urlPatterns = {"/*"})
public class LanguageFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        request.setCharacterEncoding("UTF-8");   // so Japanese / Nepali / Sinhala / Burmese form input isn't garbled

        AppUrl.remember(req);   // site address for links in emails

        HttpSession session = req.getSession();
        String lang = (String) session.getAttribute("lang");
        if (!I18n.isSupported(lang)) {
            lang = null;
            if (req.getCookies() != null) {
                for (Cookie c : req.getCookies()) {
                    if ("lang".equals(c.getName()) && I18n.isSupported(c.getValue())) lang = c.getValue();
                }
            }
            if (lang == null) {
                Locale browser = req.getLocale();
                lang = I18n.normalize(browser == null ? null : browser.getLanguage());
            }
            session.setAttribute("lang", lang);
        }

        req.setAttribute("jakarta.servlet.jsp.jstl.fmt.locale.request", I18n.locale(lang));   // a Locale object
        req.setAttribute("jakarta.servlet.jsp.jstl.fmt.localizationContext.request", "messages");
        req.setAttribute("lang", lang);
        req.setAttribute("languages", I18n.LANGUAGES);
        chain.doFilter(request, response);
    }
}
