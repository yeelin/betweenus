package com.example.betweenus.backend.servlet;

import com.example.betweenus.backend.utils.ServletUtils;
import com.example.betweenus.backend.yelp.YelpApiHelper;
import com.example.betweenus.backend.yelp.YelpConstants;
import com.github.scribejava.core.model.Response;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by ninjakiki on 2/17/16.
 * Responds to request for place details from Yelp.
 */
public class YelpFetchServlet extends HttpServlet {

    /**
     * Test query:
     * http://localhost:8080/yelp/fetch?id=facing-east-bellevue
     * https://betweenus-3636.appspot.com/yelp/fetch?id=facing-east-bellevue
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Get parameters from request
        String id = req.getParameter(YelpConstants.ID);

        //Contact yelp
        YelpApiHelper helper = new YelpApiHelper();
        Response yelpResponse = helper.searchByBusinessId(id);

        //copy bytes over
        ServletUtils.copyBytes(yelpResponse.getStream(), resp.getOutputStream());
        copyHeaders(yelpResponse.getHeaders(), resp);

        //Return yelp response
        //resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Helper method to copy headers from input stream to an output stream
     * @param headerFields
     * @param resp
     */
    private void copyHeaders(Map<String, String> headerFields, HttpServletResponse resp) {
        for (Map.Entry<String, String> entry : headerFields.entrySet()) {
            final String headerKey = entry.getKey();
            final String headerValue = entry.getValue();
            resp.addHeader(headerKey, headerValue);
        }
    }
}
