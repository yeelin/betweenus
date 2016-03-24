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
 * Created by ninjakiki on 3/21/16.
 * Responds to request for a place's photos
 * https://developers.google.com/places/web-service/photos#place_photo_requests
 */
public class GooglePlacePhotosServlet extends HttpServlet {
    public static final Logger log = LoggerFactory.getLogger(GooglePlacePhotosServlet.class);
    /**
     * Test query:
     * http://localhost:8080/google/placephotos?maxwidth=400&maxheight=400&photoreference=CnRtAAAATLZNl354RwP_9UKbQ_5Psy40texXePv4oAlgP4qNEkdIrkyse7rPXYGd9D_Uj1rVsQdWT4oRz4QrYAJNpFX7rzqqMlZw2h2E2y5IKMUZ7ouD_SlcHxYq1yL4KbKUv3qtWgTK0A6QbGh87GB3sscrHRIQiG2RrmU_jF4tENr9wGS_YxoUSSDrYjWmrNfeEHSGSc3FyhNLlBU
     * https://betweenus-3636.appspot.com/google/placephotos?maxwidth=400&maxheight=400&photoreference=CnRtAAAATLZNl354RwP_9UKbQ_5Psy40texXePv4oAlgP4qNEkdIrkyse7rPXYGd9D_Uj1rVsQdWT4oRz4QrYAJNpFX7rzqqMlZw2h2E2y5IKMUZ7ouD_SlcHxYq1yL4KbKUv3qtWgTK0A6QbGh87GB3sscrHRIQiG2RrmU_jF4tENr9wGS_YxoUSSDrYjWmrNfeEHSGSc3FyhNLlBU
     *
     * Documentation on how to handle response:
     * https://developers.google.com/places/web-service/photos#place_photo_response
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //get parameters from request
        final String photoReference = req.getParameter(GoogleConstants.PlacePhotosParamNames.PHOTO_REFERENCE);
        final String maxHeight = req.getParameter(GoogleConstants.PlacePhotosParamNames.MAX_HEIGHT);
        final String maxWidth = req.getParameter(GoogleConstants.PlacePhotosParamNames.MAX_WIDTH);

        //contact place details api
        HttpURLConnection urlConnection = null;

        try {
            final URL url = buildPlacePhotosUrl(photoReference, maxHeight, maxWidth);
            log.warn("GooglePlacePhotosServlet:doGet : URL:" + url.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(GoogleConstants.REQUEST_METHOD);
            urlConnection.setConnectTimeout(GoogleConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(GoogleConstants.READ_TIMEOUT_MILLIS);
            //urlConnection.setInstanceFollowRedirects(false);
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //copy response onto output stream
                ServletUtils.copyBytes(urlConnection.getInputStream(), resp.getOutputStream());
                ServletUtils.copyHeaders(urlConnection.getHeaderFields(), resp);
                //resp.setContentType("image/jpeg");

                //String cacheControl = urlConnection.getHeaderField("cache-control");
                //String expires = urlConnection.getHeaderField("expires");
                //resp.addHeader("cache-control", cacheControl);
                //resp.addHeader("expires", expires);

                resp.setStatus(HttpServletResponse.SC_OK);
            }
            else {
                log.warn("GooglePlacePhotosServlet:doGet: ResponseStatus:" + responseCode);
                resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        }
        catch (MalformedURLException e) {
            log.error("GooglePlacePhotosServlet:doGet: Unexpected MalformedURLException", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (Exception e) {
            log.error("GooglePlacePhotosServlet:doGet: Unexpected Exception", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            //close connection
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    /**
     * Builds the url to call the place photos api
     * https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=CnRtAAAATLZNl354RwP_9UKbQ_5Psy40texXePv4oAlgP4qNEkdIrkyse7rPXYGd9D_Uj1rVsQdWT4oRz4QrYAJNpFX7rzqqMlZw2h2E2y5IKMUZ7ouD_SlcHxYq1yL4KbKUv3qtWgTK0A6QbGh87GB3sscrHRIQiG2RrmU_jF4tENr9wGS_YxoUSSDrYjWmrNfeEHSGSc3FyhNLlBU&key=YOUR_API_KEY
     *
     * @return
     * @throws MalformedURLException
     */
    private URL buildPlacePhotosUrl(String photoReference, String maxHeight, String maxWidth) throws MalformedURLException {
        StringBuilder urlStringBuilder = new StringBuilder()
                .append(GoogleConstants.PLACE_PHOTOS_URL)
                .append(String.format("%s=%s", GoogleConstants.PlacePhotosParamNames.PHOTO_REFERENCE, photoReference))
                .append("&")
                .append(String.format("%s=%s", GoogleConstants.PlacePhotosParamNames.MAX_HEIGHT, maxHeight))
                .append("&")
                .append(String.format("%s=%s", GoogleConstants.PlacePhotosParamNames.MAX_WIDTH, maxWidth))
                .append("&")
                .append(String.format("%s=%s", GoogleConstants.ParamNames.KEY, GoogleConstants.API_KEY));
        return new URL(urlStringBuilder.toString());
    }
}
