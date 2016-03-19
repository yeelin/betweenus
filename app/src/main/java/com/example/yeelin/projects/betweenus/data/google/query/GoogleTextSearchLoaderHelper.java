package com.example.yeelin.projects.betweenus.data.google.query;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.google.json.TextSearchJsonDeserializerHelper;
import com.example.yeelin.projects.betweenus.data.google.model.PlaceSearchResult;
import com.example.yeelin.projects.betweenus.utils.CacheUtils;
import com.example.yeelin.projects.betweenus.utils.FetchDataUtils;
import com.google.android.gms.maps.model.LatLng;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ninjakiki on 3/18/16.
 */
public class GoogleTextSearchLoaderHelper {
    private static final String TAG = GoogleTextSearchLoaderHelper.class.getCanonicalName();

    /**
     * Called from a bg thread. This method does the following:
     * 1. Makes sure we passed pre-network checks
     * 2. initialize the cache
     * 3. makes a search call to the Google Text Search API (via appengine)
     * 4. returns a PlaceSearchResult object
     */
    public static PlaceSearchResult searchForPlaces(Context context, String query, LatLng location, int radius, String type) {
        Log.d(TAG, "searchForPlaces: Search coords: " + location);

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        HttpURLConnection urlConnection = null;
        PlaceSearchResult result = null;
        try {
            //search google for places
            final URL url = buildTextSearchUrl(query, location, radius, type);
            Log.d(TAG, "searchForPlaces: Url:" + url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(LocalConstants.REQUEST_METHOD_GET);
            urlConnection.setConnectTimeout(LocalConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(LocalConstants.READ_TIMEOUT_MILLIS);
            urlConnection.connect();

            int httpStatus = urlConnection.getResponseCode();
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "searchForPlaces: TextSearch Response:" + urlConnection.getInputStream());

                //deserialize the json response
                result = TextSearchJsonDeserializerHelper.deserializeTextSearchResponse(urlConnection.getInputStream());
                CacheUtils.logCache();
            }
            else {
                Log.w(TAG, String.format("searchForPlaces: Http Status:%d, Error:%s", httpStatus, urlConnection.getErrorStream()));
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "searchForPlaces: Unexpected MalformedURLException", e);
        }
        catch (Exception e) {
            Log.e(TAG, "searchForPlaces: Unexpected Exception", e);
        }
        finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return result;
    }

    /**
     * Builds the URL for getting google text search results (via appengine)
     * @param query
     * @param location
     * @param radius
     * @param type
     * @return
     * @throws MalformedURLException
     */
    private static URL buildTextSearchUrl(String query, LatLng location, int radius, String type)
            throws MalformedURLException {
        Uri uri = new Uri.Builder()
                .scheme(LocalConstants.SCHEME)
                .authority(LocalConstants.AUTHORITY)
                .appendPath(LocalConstants.GOOGLE_PATH)
                .appendPath(LocalConstants.TEXT_SEARCH_PATH)
                .appendQueryParameter(GoogleConstants.TextSearchParamNames.QUERY, query)
                .appendQueryParameter(GoogleConstants.TextSearchParamNames.LOCATION, String.format("%s,%s", location.latitude, location.longitude))
                .appendQueryParameter(GoogleConstants.TextSearchParamNames.RADIUS, String.valueOf(radius))
                .appendQueryParameter(GoogleConstants.TextSearchParamNames.TYPE, type)
                .build();
        return new URL(uri.toString());
    }
}
