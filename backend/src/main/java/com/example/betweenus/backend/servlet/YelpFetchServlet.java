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
 * Created by ninjakiki on 2/17/16.
 */
public class YelpFetchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //TODO: Get parameters from request
        String id = req.getParameter(YelpConstants.ID);

        //TODO: Contact yelp
        YelpApiHelper helper = new YelpApiHelper();
        InputStream inputStream = helper.searchByBusinessId(id);
        ServletUtils.copyBytes(inputStream, resp.getOutputStream());

        //TODO: Return yelp response
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
