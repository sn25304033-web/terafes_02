package com.hotel.servlet;

import com.hotel.dao.HotelDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("topHotels", new HotelDAO().getTopRated(8));
        } catch (SQLException e) {
            throw new ServletException("Database error loading home page", e);
        }
        // Default dates for the search box: tomorrow -> day after.
        req.setAttribute("defaultCheckIn", LocalDate.now().plusDays(1).toString());
        req.setAttribute("defaultCheckOut", LocalDate.now().plusDays(2).toString());
        req.setAttribute("today", LocalDate.now().toString());
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
