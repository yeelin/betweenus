package com.example.betweenus.backend.servlet;

import com.example.betweenus.backend.google.GoogleConstants;
import com.example.betweenus.backend.utils.ServletUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by ninjakiki on 2/19/16.
 */
public class GoogleMapsDirectionsServlet extends HttpServlet {
    public static final Logger log = LoggerFactory.getLogger(GoogleMapsDirectionsServlet.class);

    /**
     * Test query:
     * http://localhost:8080/google/directions?origin=47.7963002,-122.2889804&destination=47.7411496,-122.4036502
     * Documentation on how to handle response:
     * https://developers.google.com/maps/documentation/directions/intro#Routes
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //get parameters from request
        String origin = req.getParameter(GoogleConstants.ORIGIN);
        String destination = req.getParameter(GoogleConstants.DESTINATION);

        //contact directions api
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            final URL url = buildDirectionsUrl(origin, destination);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(GoogleConstants.REQUEST_METHOD);
            urlConnection.setConnectTimeout(GoogleConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(GoogleConstants.READ_TIMEOUT_MILLIS);
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //deserialize json response
                inputStream = urlConnection.getInputStream();
            }
            else {
                log.warn("GoogleMapsDirectionsServlet:doGet: ResponseStatus:" + responseCode);
            }
        }
        catch (MalformedURLException e) {
            log.error("GoogleMapsDirectionsServlet:doGet: Unexpected MalformedURLException", e);
        }
        catch (Exception e) {
            log.error("GoogleMapsDirectionsServlet:doGet: Unexpected Exception", e);
        }
        finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        //return response
        ServletUtils.copyBytes(inputStream, resp.getOutputStream());
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Builds the url to call the directions api
     * @param origin
     * @param destination
     * @return
     * @throws MalformedURLException
     */
    private URL buildDirectionsUrl(String origin, String destination) throws MalformedURLException {
        String urlString = String.format("https://maps.googleapis.com/maps/api/directions/json?origin=%s&destination=%s&key=%s",
                origin, destination, GoogleConstants.API_KEY);
        return new URL(urlString);
    }
}
