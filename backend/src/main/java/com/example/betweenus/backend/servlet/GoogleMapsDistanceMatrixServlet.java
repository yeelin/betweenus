package com.example.betweenus.backend.servlet;

import com.example.betweenus.backend.google.GoogleConstants;
import com.example.betweenus.backend.utils.ServletUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
public class GoogleMapsDistanceMatrixServlet extends HttpServlet {
    public static final Logger log = LoggerFactory.getLogger(GoogleMapsDistanceMatrixServlet.class);

    /**
     * Test query:
     * http://localhost:8080/google/distancematrix?origins=47.637901,-122.360631|45.520705,-122.630396&destinations=37.768264,-122.414942|49.288362,-123.136961
     * https://betweenus-3636.appspot.com/google/distancematrix?origins=47.637901,-122.360631|45.520705,-122.630396&destinations=37.768264,-122.414942|49.288362,-123.136961
     *
     * Documentation on how to handle response:
     * https://developers.google.com/maps/documentation/distance-matrix/intro
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //get parameters from request
        final String origins = req.getParameter(GoogleConstants.DistanceMatrixParamNames.ORIGINS);
        final String destinations = req.getParameter(GoogleConstants.DistanceMatrixParamNames.DESTINATIONS);

        //contact distance matrix api
        HttpURLConnection urlConnection = null;

        try {
            final URL url = buildDirectionsUrl(origins, destinations);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(GoogleConstants.REQUEST_METHOD);
            urlConnection.setConnectTimeout(GoogleConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(GoogleConstants.READ_TIMEOUT_MILLIS);
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //copy response onto output stream
                ServletUtils.copyBytes(urlConnection.getInputStream(), resp.getOutputStream());
                ServletUtils.copyHeaders(urlConnection.getHeaderFields(), resp);
                //resp.setContentType("application/json");
                resp.setStatus(HttpServletResponse.SC_OK);
            }
            else {
                log.warn("GoogleMapsDistanceMatrixServlet:doGet: ResponseStatus:" + responseCode);
                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        }
        catch (MalformedURLException e) {
            log.error("GoogleMapsDistanceMatrixServlet:doGet: Unexpected MalformedURLException", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (Exception e) {
            log.error("GoogleMapsDistanceMatrixServlet:doGet: Unexpected Exception", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    /**
     * Builds the url to call the distance matrix api
     * https://maps.googleapis.com/maps/api/distancematrix/json?origins=Vancouver+BC|Seattle&destinations=San+Francisco|Victoria+BC&key=YOUR_API_KEY
     * @param origins
     * @param destinations
     * @return
     * @throws MalformedURLException
     */
    private URL buildDirectionsUrl(String origins, String destinations) throws MalformedURLException {
        StringBuilder urlStringBuilder = new StringBuilder()
                .append(GoogleConstants.DISTANCE_MATRIX_URL)
                .append(String.format("%s=%s", GoogleConstants.DistanceMatrixParamNames.ORIGINS, origins))
                .append("&")
                .append(String.format("%s=%s", GoogleConstants.DistanceMatrixParamNames.DESTINATIONS, destinations))
                .append("&")
                .append(String.format("%s=%s", GoogleConstants.ParamNames.KEY, GoogleConstants.API_KEY));
        return new URL(urlStringBuilder.toString());
    }
}
