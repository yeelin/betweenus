package com.example.betweenus.backend.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by ninjakiki on 2/16/16.
 */
public class FetchYelpDataServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //TODO: Get parameters from request
        String name = (String) req.getParameter("name");

        //TODO: Contact yelp
        //TODO: Return yelp response

        resp.setContentType("text/plain");
        if (name == null) {
            resp.getWriter().println("Please enter a name");
        }
        else {
            resp.getWriter().println("Hello " + name);
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
