package com.example.yeelin.projects.betweenus.data.google.query;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.google.json.NearbySearchJsonDeserializerHelper;
import com.example.yeelin.projects.betweenus.data.google.model.PlaceSearchResult;
import com.example.yeelin.projects.betweenus.utils.CacheUtils;
import com.example.yeelin.projects.betweenus.utils.FetchDataUtils;
import com.google.android.gms.maps.model.LatLng;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ninjakiki on 3/17/16.
 */
public class GoogleNearbySearchLoaderHelper {
    private static final String TAG = GoogleNearbySearchLoaderHelper.class.getCanonicalName();

    /**
     * Called from a bg thread. This method does the following:
     * 1. Makes sure we passed pre-network checks
     * 2. initialize the cache
     * 3. makes a search call to the Google Nearby Search API (via appengine)
     * 4. returns a PlaceSearchResult object
     */
    public static PlaceSearchResult searchForPlaces(Context context, String type, int radius, LatLng location) {
        Log.d(TAG, "searchForPlaces: Search coords: " + location);

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        HttpURLConnection urlConnection = null;
        PlaceSearchResult result = null;
        try {
            //search google for nearby places
            final URL url = buildNearbySearchUrl(location, radius, type);
            Log.d(TAG, "searchForPlaces: Url:" + url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(LocalConstants.REQUEST_METHOD_GET);
            urlConnection.setConnectTimeout(LocalConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(LocalConstants.READ_TIMEOUT_MILLIS);
            urlConnection.connect();

            int httpStatus = urlConnection.getResponseCode();
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "searchForPlaces: NearbyResponse:" + urlConnection.getInputStream());

                //deserialize the json response
                result = NearbySearchJsonDeserializerHelper.deserializeNearbySearchResponse(urlConnection.getInputStream());
                //CacheUtils.logCache();
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
     * Searches for more places using the pagetoken returned from a previous response.
     * @param context
     * @param pageToken
     * @return
     */
    public static PlaceSearchResult searchForMorePlaces(Context context, String pageToken) {
        Log.d(TAG, "searchForMorePlaces: PageToken: " + pageToken);

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        HttpURLConnection urlConnection = null;
        PlaceSearchResult result = null;
        try {
            //search google for nearby places
            final URL url = buildNearbySearchUrl(pageToken);
            Log.d(TAG, "searchForMorePlaces: Url:" + url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(LocalConstants.REQUEST_METHOD_GET);
            urlConnection.setConnectTimeout(LocalConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(LocalConstants.READ_TIMEOUT_MILLIS);
            urlConnection.connect();

            int httpStatus = urlConnection.getResponseCode();
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "searchForMorePlaces: NearbyResponse:" + urlConnection.getInputStream());

                //deserialize the json response
                result = NearbySearchJsonDeserializerHelper.deserializeNearbySearchResponse(urlConnection.getInputStream());
                //CacheUtils.logCache();
            }
            else {
                Log.w(TAG, String.format("searchForMorePlaces: Http Status:%d, Error:%s", httpStatus, urlConnection.getErrorStream()));
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "searchForMorePlaces: Unexpected MalformedURLException", e);
        }
        catch (Exception e) {
            Log.e(TAG, "searchForMorePlaces: Unexpected Exception", e);
        }
        finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }

        return result;
    }

    /**
     * Builds the URL for getting google nearby search results (via appengine)
     * @param location
     * @param radius
     * @param type
     * @return
     * @throws MalformedURLException
     */
    private static URL buildNearbySearchUrl(LatLng location, int radius, String type)
            throws MalformedURLException {
        Uri uri = new Uri.Builder()
                .scheme(LocalConstants.SCHEME)
                .authority(LocalConstants.AUTHORITY)
                .appendPath(LocalConstants.GOOGLE_PATH)
                .appendPath(LocalConstants.NEARBY_SEARCH_PATH)
                .appendQueryParameter(GoogleConstants.NearbySearchParamNames.LOCATION, String.format("%s,%s", location.latitude, location.longitude))
                .appendQueryParameter(GoogleConstants.NearbySearchParamNames.RADIUS, String.valueOf(radius))
                .appendQueryParameter(GoogleConstants.NearbySearchParamNames.TYPE, type)
                .build();
        return new URL(uri.toString());
    }

    /**
     * Builds the URL for getting google nearby search results (via appengine)
     * @param pageToken
     * @return
     * @throws MalformedURLException
     */
    private static URL buildNearbySearchUrl(String pageToken)
            throws MalformedURLException {
        Uri uri = new Uri.Builder()
                .scheme(LocalConstants.SCHEME)
                .authority(LocalConstants.AUTHORITY)
                .appendPath(LocalConstants.GOOGLE_PATH)
                .appendPath(LocalConstants.NEARBY_SEARCH_PATH)
                .appendQueryParameter(GoogleConstants.NearbySearchParamNames.PAGE_TOKEN, pageToken)
                .build();
        return new URL(uri.toString());
    }
}
