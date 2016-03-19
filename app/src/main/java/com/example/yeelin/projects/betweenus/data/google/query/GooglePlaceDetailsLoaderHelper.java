package com.example.yeelin.projects.betweenus.data.google.query;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.google.json.PlaceDetailsJsonDeserializerHelper;
import com.example.yeelin.projects.betweenus.data.google.model.PlaceDetailsResult;
import com.example.yeelin.projects.betweenus.utils.CacheUtils;
import com.example.yeelin.projects.betweenus.utils.FetchDataUtils;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ninjakiki on 3/17/16.
 */
public class GooglePlaceDetailsLoaderHelper {
    private static final String TAG = GooglePlaceDetailsLoaderHelper.class.getCanonicalName();

    /**
     * Called from a bg thread. This method does the following:
     * 1. Makes sure we passed pre-network checks
     * 2. initialize the cache
     * 3. makes a search call to the Google Place Details API (via appengine)
     * 4. returns a PlaceDetailsResult object
     */
    public static PlaceDetailsResult fetchPlaceDetails(Context context, String placeId) {
        Log.d(TAG, "fetchPlaceDetails: PlaceId: " + placeId);

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        HttpURLConnection urlConnection = null;
        PlaceDetailsResult result = null;
        try {
            //search google for nearby places
            final URL url = buildPlaceDetailsUrl(placeId);
            Log.d(TAG, "fetchPlaceDetails: Url:" + url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(LocalConstants.REQUEST_METHOD_GET);
            urlConnection.setConnectTimeout(LocalConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(LocalConstants.READ_TIMEOUT_MILLIS);
            urlConnection.connect();

            int httpStatus = urlConnection.getResponseCode();
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                //deserialize the json response
                result = PlaceDetailsJsonDeserializerHelper.deserializePlaceDetailsResponse(urlConnection.getInputStream());
                CacheUtils.logCache();
            }
            else {
                Log.w(TAG, String.format("fetchPlaceDetails: Http Status:%d, Error:%s", httpStatus, urlConnection.getErrorStream()));
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "fetchPlaceDetails: Unexpected MalformedURLException", e);
        }
        catch (Exception e) {
            Log.e(TAG, "fetchPlaceDetails: Unexpected Exception", e);
        }
        finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return result;
    }

    /**
     * Builds the URL for getting google place details (via appengine)
     * @param placeId
     * @return
     * @throws MalformedURLException
     */
    private static URL buildPlaceDetailsUrl(String placeId)
            throws MalformedURLException {
        Uri uri = new Uri.Builder()
                .scheme(LocalConstants.SCHEME)
                .authority(LocalConstants.AUTHORITY)
                .appendPath(LocalConstants.GOOGLE_PATH)
                .appendPath(LocalConstants.PLACE_DETAILS_PATH)
                .appendQueryParameter(GoogleConstants.PlaceDetailsParamNames.PLACE_ID, placeId)
                .build();
        return new URL(uri.toString());
    }
}
