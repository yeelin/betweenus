package com.example.yeelin.projects.betweenus.data.google.query;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.google.json.DistanceMatrixJsonDeserializerHelper;
import com.example.yeelin.projects.betweenus.data.google.model.DistanceMatrixResult;
import com.example.yeelin.projects.betweenus.utils.CacheUtils;
import com.example.yeelin.projects.betweenus.utils.FetchDataUtils;
import com.google.android.gms.maps.model.LatLng;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by ninjakiki on 2/26/16.
 */
public class GoogleDistanceMatrixLoaderHelper {
    private static final String TAG = GoogleDistanceMatrixLoaderHelper.class.getCanonicalName();

    /**
     * Creates the url for appspot, deserializes the response, and returns DistanceMatrixResult.
     * Test query:
     * https://betweenus-3636.appspot.com/google/distancematrix/json?origins=Vancouver+BC|Seattle&destinations=San+Francisco|Victoria+BC&key=YOUR_API_KEY
     *
     * @param context
     * @param origins
     * @param destinations
     */
    public static DistanceMatrixResult getDistanceMatrix(Context context, @NonNull LatLng[] origins, @NonNull LatLng[] destinations) {
        Log.d(TAG, String.format("getTravelTimes: Origins:%s, Destinations:%s", Arrays.toString(origins), Arrays.toString(destinations)));

        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        CacheUtils.initializeCache(context);

        HttpURLConnection urlConnection = null;
        DistanceMatrixResult result = null;
        try {
            //contact appspot
            final URL url = buildDistanceMatrixUrl(origins, destinations);
            Log.d(TAG, "getDistanceMatrix: Url:" + url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(LocalConstants.REQUEST_METHOD_GET);
            urlConnection.setConnectTimeout(LocalConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(LocalConstants.READ_TIMEOUT_MILLIS);
            urlConnection.connect();

            int httpStatus = urlConnection.getResponseCode();
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                result = DistanceMatrixJsonDeserializerHelper.deserializeDistanceMatrixResponse(urlConnection.getInputStream());
                CacheUtils.logCache();
            }
            else {
                Log.w(TAG, String.format("getDistanceMatrix: Http Status:%d, Error:%s", httpStatus, urlConnection.getErrorStream()));
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "getDistanceMatrix: Unexpected MalformedURLException", e);
        }
        catch (Exception e) {
            Log.e(TAG, "getDistanceMatrix: Unexpected Exception", e);
        }
        finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return result;
    }

    /**
     * Builds the URL for getting distance matrix results via appengine
     * @param origins
     * @param destinations
     * @return
     * @throws MalformedURLException
     */
    private static URL buildDistanceMatrixUrl(@NonNull LatLng[] origins, @NonNull LatLng[] destinations) throws MalformedURLException {
        Uri uri = new Uri.Builder()
                .scheme(LocalConstants.SCHEME)
                .authority(LocalConstants.AUTHORITY)
                .appendPath(LocalConstants.GOOGLE_PATH)
                .appendPath(LocalConstants.DISTANCE_MATRIX_PATH)
                .appendQueryParameter(GoogleConstants.DistanceMatrixParamNames.ORIGINS, buildPipeDelimitedString(origins))
                .appendQueryParameter(GoogleConstants.DistanceMatrixParamNames.DESTINATIONS, buildPipeDelimitedString(destinations))
                .build();
        return new URL(uri.toString());
    }

    /**
     * Builds a string delimited by pipes (i.e. |) given an array of Latlng
     * @param latLngs
     * @return
     */
    private static String buildPipeDelimitedString(@NonNull LatLng[] latLngs) {
        StringBuilder stringBuilder = new StringBuilder(latLngs.length);

        for (int i=0; i<latLngs.length; i++) {
            stringBuilder.append(String.format("%f,%f", latLngs[i].latitude, latLngs[i].longitude));
            if (i < latLngs.length - 1) {
                stringBuilder.append("|");
            }
        }
        return stringBuilder.toString();
    }
}
