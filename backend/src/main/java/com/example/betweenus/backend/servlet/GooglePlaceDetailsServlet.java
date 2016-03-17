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
 * Created by ninjakiki on 3/17/16.
 * Responds to request for details about a place.
 */
public class GooglePlaceDetailsServlet extends HttpServlet {
    public static final Logger log = LoggerFactory.getLogger(GooglePlaceDetailsServlet.class);

    /**
     * Test query:
     * http://localhost:8080/google/placedetails?placeid=ChIJN1t_tDeuEmsRUsoyG83frY4
     * https://betweenus-3636.appspot.com/google/placedetails?placeid=ChIJN1t_tDeuEmsRUsoyG83frY4
     *
     * Documentation on how to handle response:
     * https://developers.google.com/places/web-service/details#PlaceDetailsResponses
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //get parameters from request
        final String placeId = req.getParameter(GoogleConstants.PlaceDetailsParamNames.PLACE_ID);

        //contact place details api
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            final URL url = buildPlaceDetailsUrl(placeId);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(GoogleConstants.REQUEST_METHOD);
            urlConnection.setConnectTimeout(GoogleConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(GoogleConstants.READ_TIMEOUT_MILLIS);
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                //copy response onto output stream
                ServletUtils.copyBytes(inputStream, resp.getOutputStream());
                resp.setContentType("application/json");
                resp.setStatus(HttpServletResponse.SC_OK);
            }
            else {
                log.warn("GooglePlaceDetailsServlet:doGet: ResponseStatus:" + responseCode);
                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        }
        catch (MalformedURLException e) {
            log.error("GooglePlaceDetailsServlet:doGet: Unexpected MalformedURLException", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (Exception e) {
            log.error("GooglePlaceDetailsServlet:doGet: Unexpected Exception", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            //close connection
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    /**
     * Builds the url to call the place details api
     * https://maps.googleapis.com/maps/api/place/details/json?placeid=ChIJN1t_tDeuEmsRUsoyG83frY4&key=YOUR_API_KEY
     *
     * @return
     * @throws MalformedURLException
     */
    private URL buildPlaceDetailsUrl(String placeId) throws MalformedURLException {
        StringBuilder urlStringBuilder = new StringBuilder()
                .append(GoogleConstants.PLACE_DETAILS_URL)
                .append(String.format("%s=%s", GoogleConstants.PlaceDetailsParamNames.PLACE_ID, placeId))
                .append("&")
                .append(String.format("%s=%s", GoogleConstants.ParamNames.KEY, GoogleConstants.API_KEY));
        return new URL(urlStringBuilder.toString());
    }
}
