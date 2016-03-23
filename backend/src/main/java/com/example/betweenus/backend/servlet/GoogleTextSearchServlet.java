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
 * Created by ninjakiki on 3/18/16.
 * Responds to request for text searches.
 */
public class GoogleTextSearchServlet extends HttpServlet {
    public static final Logger log = LoggerFactory.getLogger(GoogleTextSearchServlet.class);

    /**
     * Test query:
     * http://localhost:8080/google/textsearch?query=Restaurant&location=47.645932320504436,-122.20470420793882&radius=4828&type=Restaurant
     * https://betweenus-3636.appspot.com/google/textsearch?query=Restaurant&location=47.645932320504436,-122.20470420793882&radius=4828&type=Restaurant
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
        final String query = req.getParameter(GoogleConstants.TextSearchParamNames.QUERY);
        final String location = req.getParameter(GoogleConstants.TextSearchParamNames.LOCATION);
        final String radius = req.getParameter(GoogleConstants.TextSearchParamNames.RADIUS);
        final String type = req.getParameter(GoogleConstants.TextSearchParamNames.TYPE);
        final String pageToken = req.getParameter(GoogleConstants.TextSearchParamNames.PAGE_TOKEN);

        //contact text search api
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            final URL url = buildTextSearchUrl(query, location, radius, type, pageToken);
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
                log.warn("GoogleTextSearchServlet:doGet: ResponseStatus:" + responseCode);
                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        }
        catch (MalformedURLException e) {
            log.error("GoogleTextSearchServlet:doGet: Unexpected MalformedURLException", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (Exception e) {
            log.error("GoogleTextSearchServlet:doGet: Unexpected Exception", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            //close connection
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    /**
     * Builds the url to call the place search with query (aka text search) api
     * https://maps.googleapis.com/maps/api/place/textsearch/json?query=Restaurant&location=47.645932320504436,-122.20470420793882&radius=4828&type=Restaurant&key=%s
     *
     * @param query
     * @param location
     * @param radius
     * @param type
     * @param pageToken
     *
     * @return
     * @throws MalformedURLException
     */
    private URL buildTextSearchUrl(String query, String location, String radius, String type, String pageToken)
            throws MalformedURLException {
        StringBuilder urlStringBuilder = new StringBuilder()
                .append(GoogleConstants.TEXT_SEARCH_URL)
                .append(String.format("%s=%s", GoogleConstants.ParamNames.KEY, GoogleConstants.API_KEY))
                .append("&");

        if (pageToken == null) {
            //pagetoken is null, so this is the first call
            urlStringBuilder
                    .append(String.format("%s=%s", GoogleConstants.TextSearchParamNames.QUERY, query))
                    .append("&")
                    .append(String.format("%s=%s", GoogleConstants.TextSearchParamNames.LOCATION, location))
                    .append("&")
                    .append(String.format("%s=%s", GoogleConstants.TextSearchParamNames.RADIUS, radius))
                    .append("&")
                    .append(String.format("%s=%s", GoogleConstants.TextSearchParamNames.TYPE, type));
        }
        else {
            //pagetoken isn't null, so this is a second call to get more data
            urlStringBuilder
                    .append(String.format("%s=%s", GoogleConstants.TextSearchParamNames.PAGE_TOKEN, pageToken));
        }
        return new URL(urlStringBuilder.toString());
    }
}
