package com.example.betweenus.backend.servlet;

import com.example.betweenus.backend.utils.ServletUtils;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by ninjakiki on 2/18/16.
 */
public class FbFetchServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Get parameters from request

        //Contact fb
        InputStream inputStream = null;

        //Return fb response
        ServletUtils.copyBytes(inputStream, resp.getOutputStream());
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
