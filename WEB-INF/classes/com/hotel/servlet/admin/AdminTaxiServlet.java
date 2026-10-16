package com.hotel.servlet.admin;

import com.hotel.dao.NotificationDAO;
import com.hotel.dao.TaxiDAO;
import com.hotel.model.TaxiBooking;
import com.hotel.util.DateUtil;
import com.hotel.util.I18n;
import com.hotel.util.Mails;
import com.hotel.util.PaymentValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

/**
 *  GET  /admin/taxis?status=REQUESTED                 -> list
 *  POST /admin/taxis (id, status, driverName, driverPhone) -> update, guest is notified + emailed
 */
@WebServlet(name = "AdminTaxiServlet", urlPatterns = {"/admin/taxis"})
public class AdminTaxiServlet extends HttpServlet {

    static final String[] STATUS_LIST = {"REQUESTED", "CONFIRMED", "COMPLETED", "CANCELLED"};
    private static final Set<String> STATUSES = Set.of(STATUS_LIST);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String status = req.getParameter("status");
        if (status != null && !STATUSES.contains(status)) status = null;
        try {
            req.setAttribute("taxis", new TaxiDAO().getAll(status));
        } catch (SQLException e) {
            throw new ServletException("Database error loading taxis", e);
        }
        req.setAttribute("statusFilter", status);
        req.setAttribute("statuses", STATUS_LIST);
        req.getRequestDispatcher("/WEB-INF/views/admin/taxis.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = DateUtil.parseInt(req.getParameter("id"), -1);
        String status = req.getParameter("status");
        String driverName = blankToNull(req.getParameter("driverName"));
        String driverPhone = blankToNull(req.getParameter("driverPhone"));

        if (STATUSES.contains(status)) {
            if (driverPhone != null && !PaymentValidationUtil.isValidPhone(driverPhone)) {
                req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.phone.invalid"));
            } else {
                try {
                    TaxiDAO dao = new TaxiDAO();
                    TaxiBooking before = dao.findById(id);
                    if (before != null) {
                        dao.updateByAdmin(id, status, driverName, driverPhone);
                        TaxiBooking t = dao.findById(id);
                        boolean statusChanged = !status.equals(before.getStatus());
                        boolean driverChanged = !Objects.equals(driverName, before.getDriverName())
                                || !Objects.equals(driverPhone, before.getDriverPhone());
                        if (statusChanged || driverChanged) {
                            String lang = I18n.normalize(t.getUserLang());
                            if ("CANCELLED".equals(status) && statusChanged) {
                                Mails.taxiCancelled(lang, t.getUserEmail(), t);
                            } else {
                                Mails.taxiStatus(lang, t.getUserEmail(), t);
                            }
                            if (t.getUserId() > 0) {
                                new NotificationDAO().create(t.getUserId(),
                                        I18n.t(lang, "notif.taxi.status", id, I18n.t(lang, "taxiStatus." + status)), "/taxis");
                            }
                        }
                        req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.admin.taxiUpdated", id));
                    }
                } catch (SQLException e) {
                    throw new ServletException("Database error updating taxi", e);
                }
            }
        }
        String back = req.getParameter("filter");
        resp.sendRedirect(req.getContextPath() + "/admin/taxis" +
                (back != null && STATUSES.contains(back) ? "?status=" + back : ""));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
