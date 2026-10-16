package com.hotel.filter;

import com.hotel.dao.HotelDAO;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

/** Puts the list of cities on every request so the header's city bar can render (like NovaCart's CategoryNavFilter). */
@WebFilter(urlPatterns = {"/*"})
public class CityNavFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            request.setAttribute("cities", new HotelDAO().getCities());
        } catch (Exception e) {
            // Non-fatal: the city bar will just be empty.
        }
        chain.doFilter(request, response);
    }
}
