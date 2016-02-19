package com.example.yeelin.projects.betweenus.data.yelp.query;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.yelp.json.YelpJsonDeserializerHelper;
import com.example.yeelin.projects.betweenus.data.yelp.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.data.yelp.model.YelpResult;
import com.example.yeelin.projects.betweenus.utils.CacheUtils;
import com.example.yeelin.projects.betweenus.utils.FetchDataUtils;
import com.google.android.gms.maps.model.LatLng;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ninjakiki on 2/17/16.
 */
public class YelpLoaderHelper {
    private static final String TAG = YelpLoaderHelper.class.getCanonicalName();

    /**
     * Called from a bg thread. This method does the following:
     * 1. Makes sure we passed pre-network checks
     * 2. initialize the cache
     * 3. makes a search call to the Yelp API (via appengine)
     * 4. returns a YelpResult object
     *
     * @param context
     * @param searchTerm
     * @param searchRadius
     * @param searchLimit
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    @Nullable
    public static YelpResult searchForPlaces(Context context, String searchTerm, int searchRadius, int searchLimit,
                                        LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        Log.d(TAG, "searchForPlaces: Search coords: " + midLatLng);

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        HttpURLConnection urlConnection = null;
        YelpResult result = null;
        try {
            //search yelp for businesses
            final URL url = buildSearchUrl(searchTerm, searchRadius, searchLimit, midLatLng);
            Log.d(TAG, "searchForPlaces: Url:" + url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(LocalConstants.REQUEST_METHOD);
            urlConnection.setConnectTimeout(LocalConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(LocalConstants.READ_TIMEOUT_MILLIS);
            urlConnection.connect();

            int httpStatus = urlConnection.getResponseCode();
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                //deserialize the json response
                result = YelpJsonDeserializerHelper.deserializeYelpResponse(urlConnection.getInputStream());
                CacheUtils.logCache();
                //Log.d(TAG, "searchYelp: YelpResult: " + result);
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
     * Called from a bg thread. This method does the following:
     * 1. Makes sure we passed pre-network checks
     * 2. initialize the cache
     * 3. makes a fetch call to the Yelp API (via appengine) for a single business
     * 4. returns a YelpBusiness object
     *
     * @param context
     * @param id
     * @return
     */
    public static YelpBusiness fetchPlaceDetails(Context context, String id) {
        Log.d(TAG, "fetchPlaceDetails: BusinessId:" + id);

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        HttpURLConnection urlConnection = null;
        YelpBusiness business = null;
        try {
            //fetch single business from Yelp
            URL url = buildFetchUrl(id);
            Log.d(TAG, "fetchPlaceDetails: Url:" + url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(LocalConstants.REQUEST_METHOD);
            urlConnection.setConnectTimeout(LocalConstants.CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(LocalConstants.READ_TIMEOUT_MILLIS);
            urlConnection.connect();

            int httpStatus = urlConnection.getResponseCode();
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                //deserialize the json response
                business = YelpJsonDeserializerHelper.deserializeYelpSingleResponse(urlConnection.getInputStream());
                CacheUtils.logCache();
                //Log.d(TAG, "fetchFromYelp: YelpBusiness: " + business);
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

        return business;
    }

    /**
     * Builds the URL for getting yelp search results (via appengine)
     * @param searchTerm
     * @param searchRadius
     * @param searchLimit
     * @param midLatLng
     * @return
     * @throws MalformedURLException
     */
    private static URL buildSearchUrl(String searchTerm, int searchRadius, int searchLimit, LatLng midLatLng)
            throws MalformedURLException {

        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(LocalConstants.SCHEME)
                .authority(LocalConstants.AUTHORITY)
                .appendPath(LocalConstants.YELP_PATH)
                .appendPath(LocalConstants.SEARCH_PATH)
                .appendQueryParameter(YelpConstants.ParamNames.TERM, searchTerm)
                .appendQueryParameter(YelpConstants.ParamNames.LATITUDE, String.valueOf(midLatLng.latitude))
                .appendQueryParameter(YelpConstants.ParamNames.LONGITUDE, String.valueOf(midLatLng.longitude))
                .appendQueryParameter(YelpConstants.ParamNames.RADIUS_FILTER, String.valueOf(searchRadius))
                .appendQueryParameter(YelpConstants.ParamNames.LIMIT, String.valueOf(searchLimit));
        Uri uri = uriBuilder.build();
        return new URL(uri.toString());
    }

    /**
     * Builds the URL for fetching yelp data for a single id (via appengine)
     * @param id
     * @return
     * @throws MalformedURLException
     */
    private static URL buildFetchUrl(String id)
            throws MalformedURLException {

        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(LocalConstants.SCHEME)
                .authority(LocalConstants.AUTHORITY)
                .appendPath(LocalConstants.YELP_PATH)
                .appendPath(LocalConstants.FETCH_PATH)
                .appendQueryParameter(YelpConstants.ParamNames.ID, id);
        Uri uri = uriBuilder.build();
        return new URL(uri.toString());
    }
}
