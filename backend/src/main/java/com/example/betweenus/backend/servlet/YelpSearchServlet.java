package com.example.betweenus.backend.servlet;

import com.example.betweenus.backend.utils.ServletUtils;
import com.example.betweenus.backend.yelp.YelpApiHelper;
import com.example.betweenus.backend.yelp.YelpConstants;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by ninjakiki on 2/16/16.
 */
public class YelpSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //TODO: Get parameters from request
        String term = req.getParameter(YelpConstants.TERM);
        String latitude = req.getParameter(YelpConstants.LATITUDE);
        String longitude = req.getParameter(YelpConstants.LONGITUDE);
        String radiusFilter = req.getParameter(YelpConstants.RADIUS_FILTER);
        String limit = req.getParameter(YelpConstants.LIMIT);

        //TODO: Contact yelp
        YelpApiHelper helper = new YelpApiHelper();
        InputStream inputStream = helper.searchForBusinessesByGeoCoords(term, radiusFilter, limit, latitude, longitude);
        ServletUtils.copyBytes(inputStream, resp.getOutputStream());

        //TODO: Return yelp response
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);

        //String name = req.getParameter("name");
//        resp.setContentType("text/plain");
//        if (name == null) {
//            resp.getWriter().println("Please enter a name");
//        }
//        else {
//            resp.getWriter().println("Hello " + name);
//        }
    }
}
