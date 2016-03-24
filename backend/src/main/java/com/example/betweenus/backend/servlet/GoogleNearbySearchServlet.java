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
 * Created by ninjakiki on 3/17/16.
 * Responds to request for nearby place searches.
 */
public class GoogleNearbySearchServlet extends HttpServlet {
    public static final Logger log = LoggerFactory.getLogger(GoogleNearbySearchServlet.class);

    /**
     * Test query:
     * http://localhost:8080/google/nearbysearch?location=-33.8670522,151.1957362&radius=500&type=restaurant
     * https://betweenus-3636.appspot.com/google/nearbysearch?location=-33.8670522,151.1957362&radius=500&type=restaurant
     *
     * Documentation on how to handle response:
     * https://developers.google.com/places/web-service/search#PlaceSearchResponses
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //get parameters from request
        final String location = req.getParameter(GoogleConstants.NearbySearchParamNames.LOCATION);
        final String radius = req.getParameter(GoogleConstants.NearbySearchParamNames.RADIUS);
        final String type = req.getParameter(GoogleConstants.NearbySearchParamNames.TYPE);
        final String pageToken = req.getParameter(GoogleConstants.NearbySearchParamNames.PAGE_TOKEN);

        //contact nearby search api
        HttpURLConnection urlConnection = null;

        try {
            final URL url = buildNearbySearchUrl(location, radius, type, pageToken);
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
                log.warn("GoogleNearbySearchServlet:doGet: ResponseStatus:" + responseCode);
                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        }
        catch (MalformedURLException e) {
            log.error("GoogleNearbySearchServlet:doGet: Unexpected MalformedURLException", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (Exception e) {
            log.error("GoogleNearbySearchServlet:doGet: Unexpected Exception", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            //close connection
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    /**
     * Builds the url to call the place search (aka nearby search) api
     * https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&type=restaurant&key=%s
     *
     * @param location
     * @param radius
     * @param type
     * @param pageToken
     *
     * @return
     * @throws MalformedURLException
     */
    private URL buildNearbySearchUrl(String location, String radius, String type, String pageToken)
            throws MalformedURLException {
        StringBuilder urlStringBuilder = new StringBuilder()
                .append(GoogleConstants.NEARBY_SEARCH_URL)
                .append(String.format("%s=%s", GoogleConstants.ParamNames.KEY, GoogleConstants.API_KEY))
                .append("&");

        if (pageToken == null) {
            //pagetoken is null, so this is the first call
            urlStringBuilder
                    .append(String.format("%s=%s", GoogleConstants.NearbySearchParamNames.LOCATION, location))
                    .append("&")
                    .append(String.format("%s=%s", GoogleConstants.NearbySearchParamNames.RADIUS, radius))
                    .append("&")
                    .append(String.format("%s=%s", GoogleConstants.NearbySearchParamNames.TYPE, type));
        }
        else {
            //pagetoken isn't null i.e. this is a second call to get more data
            urlStringBuilder
                    .append(String.format("%s=%s", GoogleConstants.NearbySearchParamNames.PAGE_TOKEN, pageToken));
        }
        return new URL(urlStringBuilder.toString());
    }
}
