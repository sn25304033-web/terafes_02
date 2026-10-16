package com.hotel.servlet.admin;

import com.hotel.dao.HotelDAO;
import com.hotel.model.Hotel;
import com.hotel.util.DateUtil;
import com.hotel.util.I18n;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 *  GET  /admin/hotels                 -> list
 *  GET  /admin/hotels?action=new      -> empty form
 *  GET  /admin/hotels?action=edit&id= -> edit form
 *  POST /admin/hotels action=save     -> insert / update
 *  POST /admin/hotels action=toggle   -> show / hide a hotel
 */
@WebServlet(name = "AdminHotelServlet", urlPatterns = {"/admin/hotels"})
public class AdminHotelServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        try {
            HotelDAO dao = new HotelDAO();
            if ("new".equals(action) || "edit".equals(action)) {
                if ("edit".equals(action)) {
                    Hotel h = dao.findById(DateUtil.parseInt(req.getParameter("id"), -1));
                    if (h == null) {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                    }
                    req.setAttribute("hotel", h);
                }
                req.getRequestDispatcher("/WEB-INF/views/admin/hotel-form.jsp").forward(req, resp);
                return;
            }
            req.setAttribute("hotels", dao.getAll());
        } catch (SQLException e) {
            throw new ServletException("Database error loading hotels", e);
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/hotels.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        int id = DateUtil.parseInt(req.getParameter("id"), 0);
        try {
            HotelDAO dao = new HotelDAO();
            if ("toggle".equals(action)) {
                Hotel h = dao.findById(id);
                if (h != null) dao.setActive(id, !h.isActive());
                resp.sendRedirect(req.getContextPath() + "/admin/hotels");
                return;
            }

            Hotel h = new Hotel();
            h.setId(id);
            h.setName(trim(req.getParameter("name")));
            h.setCity(trim(req.getParameter("city")));
            h.setAddress(trim(req.getParameter("address")));
            h.setDescription(trim(req.getParameter("description")));
            h.setStars(Math.min(5, Math.max(1, DateUtil.parseInt(req.getParameter("stars"), 3))));
            h.setImageUrl(trim(req.getParameter("imageUrl")));
            h.setAmenities(trim(req.getParameter("amenities")));
            h.setActive(req.getParameter("active") != null);

            if (h.getName() == null || h.getCity() == null || h.getAddress() == null) {
                req.setAttribute("error", I18n.t(req, "msg.hotel.required"));
                req.setAttribute("hotel", h);
                req.getRequestDispatcher("/WEB-INF/views/admin/hotel-form.jsp").forward(req, resp);
                return;
            }
            if (id > 0) {
                dao.update(h);
                req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.hotel.updated"));
            } else {
                id = dao.insert(h);
                req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.hotel.created"));
            }
        } catch (SQLException e) {
            throw new ServletException("Database error saving hotel", e);
        }
        resp.sendRedirect(req.getContextPath() + "/admin/rooms?hotelId=" + id);
    }

    private static String trim(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
