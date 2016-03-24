package com.example.yeelin.projects.betweenus.data.google.query;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.google.json.DirectionsJsonDeserializerHelper;
import com.example.yeelin.projects.betweenus.data.google.model.DirectionsResult;
import com.example.yeelin.projects.betweenus.utils.CacheUtils;
import com.example.yeelin.projects.betweenus.utils.FetchDataUtils;
import com.google.android.gms.maps.model.LatLng;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ninjakiki on 2/22/16.
 */
public class GoogleDirectionsLoaderHelper {
    //logcat
    private static final String TAG = GoogleDirectionsLoaderHelper.class.getCanonicalName();

    /**
     * Creates the url for appspot, deserializes the response, and returns directionResult.
     * Test query:
     * http://betweenus-3636.appspot.com/google/directions/json?origin=47.7963002,-122.2889804&destination=47.7411496,-122.4036502
     * @param context
     * @param origin
     * @param destination
     * @return
     */
    public static DirectionsResult getDirections(Context context, LatLng origin, LatLng destination) {
        Log.d(TAG, String.format("getDirections: Origin:%s, Destination:%s", origin, destination));

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists then the existing one is used
        CacheUtils.initializeCache(context);

        //build request for appspot
        HttpURLConnection urlConnection = null;
        DirectionsResult result = null;
        try {
            //contact appspot
            final URL url = buildDirectionsUrl(origin, destination);
            Log.d(TAG, "getDirections: Url:" + url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(LocalConstants.REQUEST_METHOD_GET);
            urlConnection.setConnectTimeout(LocalConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(LocalConstants.READ_TIMEOUT_MILLIS);
            urlConnection.connect();

            int httpStatus = urlConnection.getResponseCode();
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                //deserialize json response
                result = DirectionsJsonDeserializerHelper.deserializeDirectionsResponse(urlConnection.getInputStream());
                //CacheUtils.logCache();
            }
            else {
                Log.w(TAG, String.format("getDirections: Http Status:%d, Error:%s", httpStatus, urlConnection.getErrorStream()));
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "getDirections: Unexpected MalformedURLException", e);
        }
        catch (Exception e) {
            Log.e(TAG, "getDirections: Unexpected Exception", e);
        }
        finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return result;
    }

    /**
     * Builds the URL for getting directions results (via appengine)
     * @param origin
     * @param destination
     * @return
     * @throws MalformedURLException
     */
    private static URL buildDirectionsUrl(LatLng origin, LatLng destination) throws MalformedURLException {
        Uri uri = new Uri.Builder()
                .scheme(LocalConstants.SCHEME)
                .authority(LocalConstants.AUTHORITY)
                .appendPath(LocalConstants.GOOGLE_PATH)
                .appendPath(LocalConstants.DIRECTIONS_PATH)
                .appendQueryParameter(GoogleConstants.DirectionsParamNames.ORIGIN, String.format("%s,%s", origin.latitude, origin.longitude))
                .appendQueryParameter(GoogleConstants.DirectionsParamNames.DESTINATION, String.format("%s,%s", destination.latitude, destination.longitude))
                .build();
        return new URL(uri.toString());
    }
}
