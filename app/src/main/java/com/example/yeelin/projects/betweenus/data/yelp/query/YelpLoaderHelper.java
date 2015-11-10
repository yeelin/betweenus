package com.example.yeelin.projects.betweenus.data.yelp.query;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.yelp.json.YelpJsonDeserializerHelper;
import com.example.yeelin.projects.betweenus.data.yelp.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.data.yelp.model.YelpResult;
import com.example.yeelin.projects.betweenus.utils.CacheUtils;
import com.example.yeelin.projects.betweenus.utils.FetchDataUtils;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ninjakiki on 7/20/15.
 */
public class YelpLoaderHelper {
    private static final String TAG = YelpLoaderHelper.class.getCanonicalName();

    /**
     * Called from a bg thread. This method does the following:
     * 1. Makes sure we passed pre-network checks
     * 2. initialize the cache
     * 3. makes the call to the Yelp API
     * 4. returns an arraylist of YelpBusiness
     *
     * @param context
     * @param searchTerm
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    @Nullable
    public static YelpResult fetchFromNetwork(Context context, String searchTerm, LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        Log.d(TAG, "fetchFromNetwork");

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        try {
            //fetch data from Yelp
            YelpResult yelpResult = fetchFromYelp(context, searchTerm, userLatLng, friendLatLng, midLatLng);
            CacheUtils.logCache();
            return yelpResult;
        }
        catch (Exception e) {
            Log.e(TAG, "fetchFromNetwork: Unexpected error", e);
        }

        return null;
    }

    /**
     * Called from a bg thread. Helper method that does the following:
     * 1. finds the midpoint between the user and friend
     * 2. creates the Yelp API helper to query the api
     * 3. parses the JSON response
     * 4. returns an arraylist of YelpBusiness
     *
     * @param searchTerm
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    private static YelpResult fetchFromYelp(Context context, String searchTerm, LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng)
            throws IOException {
        Log.d(TAG, "fetchFromYelp");

        //build the url
        //open a http url connection to get data
        //build the result and return
        YelpApiHelper yelpApiHelper = new YelpApiHelper();
        InputStream yelpResponseJSON = yelpApiHelper.searchForBusinessesByGeoCoords(searchTerm, midLatLng.latitude, midLatLng.longitude);

        //deserialize the json response
        YelpResult yelpResult = YelpJsonDeserializerHelper.deserializeYelpResponse(yelpResponseJSON);
        Log.d(TAG, "fetchFromYelp: YelpResult: " + yelpResult);

        //return yelp result whole
        return yelpResult;
    }

    /**
     *
     * @param context
     * @param id
     * @return
     */
    public static YelpBusiness fetchFromNetwork(Context context, String id) {
        Log.d(TAG, "fetchFromNetwork:BusinessId:" + id);

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        try {
            //fetch data from Yelp
            YelpBusiness yelpBusiness = fetchFromYelp(id);
            CacheUtils.logCache();
            return yelpBusiness;
        }
        catch (Exception e) {
            Log.e(TAG, "fetchFromNetwork: Unexpected error", e);
        }

        return null;
    }

    /**
     * Called from a bg thread. Helper method that does the following:
     * 1. creates the Yelp API helper to query the api (searchByBusinessId)
     * 2. parses the JSON response
     * 3. returns an arraylist of YelpBusiness
     *
     * @param id
     */
    private static YelpBusiness fetchFromYelp(String id)
            throws IOException {
        Log.d(TAG, "fetchFromYelp");

        //build the url
        //open a http url connection to get data
        //build the result and return
        YelpApiHelper yelpApiHelper = new YelpApiHelper();
        InputStream yelpResponseJSON = yelpApiHelper.searchByBusinessId(id);

        //deserialize the json response
        YelpBusiness yelpBusiness  = YelpJsonDeserializerHelper.deserializeYelpSingleResponse(yelpResponseJSON);
        Log.d(TAG, "fetchFromYelp: YelpResult: " + yelpBusiness);

        return yelpBusiness;
    }
}
