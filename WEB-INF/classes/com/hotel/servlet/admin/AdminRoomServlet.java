package com.hotel.servlet.admin;

import com.hotel.dao.HotelDAO;
import com.hotel.dao.RoomTypeDAO;
import com.hotel.model.Hotel;
import com.hotel.model.RoomType;
import com.hotel.util.DateUtil;
import com.hotel.util.I18n;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

/**
 *  GET  /admin/rooms?hotelId=1              -> room types of a hotel + add form
 *  GET  /admin/rooms?hotelId=1&editId=3     -> same page with the edit form filled
 *  POST /admin/rooms action=save|delete
 */
@WebServlet(name = "AdminRoomServlet", urlPatterns = {"/admin/rooms"})
public class AdminRoomServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int hotelId = DateUtil.parseInt(req.getParameter("hotelId"), -1);
        try {
            Hotel hotel = new HotelDAO().findById(hotelId);
            if (hotel == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/hotels");
                return;
            }
            RoomTypeDAO dao = new RoomTypeDAO();
            req.setAttribute("hotel", hotel);
            req.setAttribute("roomTypes", dao.getByHotel(hotelId, null, null));
            int editId = DateUtil.parseInt(req.getParameter("editId"), -1);
            if (editId > 0) req.setAttribute("editRoom", dao.findById(editId));
        } catch (SQLException e) {
            throw new ServletException("Database error loading room types", e);
        }
        req.getRequestDispatcher("/WEB-INF/views/admin/rooms.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int hotelId = DateUtil.parseInt(req.getParameter("hotelId"), -1);
        int id = DateUtil.parseInt(req.getParameter("id"), 0);
        try {
            RoomTypeDAO dao = new RoomTypeDAO();
            if ("delete".equals(req.getParameter("action"))) {
                boolean ok = dao.delete(id);
                req.getSession().setAttribute("flashMessage", I18n.t(req, ok ? "msg.room.deleted" : "msg.room.hasReservations"));
            } else {
                RoomType r = new RoomType();
                r.setId(id);
                r.setHotelId(hotelId);
                r.setName(req.getParameter("name") == null ? "" : req.getParameter("name").trim());
                r.setDescription(req.getParameter("description"));
                r.setCapacity(Math.max(1, DateUtil.parseInt(req.getParameter("capacity"), 2)));
                r.setTotalRooms(Math.max(0, DateUtil.parseInt(req.getParameter("totalRooms"), 1)));
                r.setImageUrl(req.getParameter("imageUrl"));
                BigDecimal price;
                try {
                    price = new BigDecimal(req.getParameter("pricePerNight").trim());
                } catch (Exception e) {
                    price = null;
                }
                if (r.getName().isEmpty() || price == null || price.signum() < 0) {
                    req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.room.required"));
                } else {
                    r.setPricePerNight(price);
                    if (id > 0) dao.update(r); else dao.insert(r);
                    req.getSession().setAttribute("flashMessage", I18n.t(req, "msg.room.saved"));
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Database error saving room type", e);
        }
        resp.sendRedirect(req.getContextPath() + "/admin/rooms?hotelId=" + hotelId);
    }
}
