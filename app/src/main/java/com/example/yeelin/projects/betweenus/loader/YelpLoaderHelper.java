package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.model.YelpResult;
import com.example.yeelin.projects.betweenus.gson.YelpResultDeserializer;
import com.example.yeelin.projects.betweenus.utils.CacheUtils;
import com.example.yeelin.projects.betweenus.utils.FetchDataUtils;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.example.yeelin.projects.betweenus.yelp.YelpApiHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
     */
    @Nullable
    public static YelpResult fetchFromNetwork(Context context, String searchTerm, LatLng userLatLng, LatLng friendLatLng) {
        Log.d(TAG, "fetchFromNetwork");

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        try {
            //fetch data from Yelp
            YelpResult yelpResult = fetchFromYelp(context, searchTerm, userLatLng, friendLatLng);
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
     */
    private static YelpResult fetchFromYelp(Context context, String searchTerm, LatLng userLatLng, LatLng friendLatLng)
            throws IOException {
        Log.d(TAG, "fetchFromYelp");

        //build the url
        //open a http url connection to get data
        //build the result and return
        YelpApiHelper yelpApiHelper = new YelpApiHelper();
        LatLng midPoint = LocationUtils.computeMidPoint(userLatLng, friendLatLng);
        InputStream yelpResponseJSON = yelpApiHelper.searchForBusinessesByGeoCoords(searchTerm, midPoint.latitude, midPoint.longitude);

        //deserialize the json response
        YelpResult yelpResult = deserializeYelpResponseJson(yelpResponseJSON);
        Log.d(TAG, "fetchFromYelp: YelpResult: " + yelpResult);

        //return yelp result whole
        return yelpResult;
    }

    /**
     * Deserialize the yelp JSON response using GSON
     * @param yelpResponseJSON
     * @throws IOException
     */
    private static YelpResult deserializeYelpResponseJson(InputStream yelpResponseJSON)
            throws IOException {

        //create a gson object
//        final Gson gson = new GsonBuilder().create();
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(YelpResult.class, new YelpResultDeserializer());
        final Gson gson = gsonBuilder.create();

        InputStreamReader yelpResponseInputStreamReader = null;
        try {
            //create an input stream reader from the json input stream
            yelpResponseInputStreamReader = new InputStreamReader(yelpResponseJSON, "UTF-8");

            //deserialize json into java
            YelpResult yelpResult = gson.fromJson(yelpResponseInputStreamReader, YelpResult.class);
            Log.d(TAG, "deserializeYelpResponseJson: Size of arraylist: " + yelpResult.getBusinesses().size());

            //log for debugging purposes
            Log.d(TAG, "deserializeYelpResponseJson: YelpResult: " + yelpResult);
            return yelpResult;
        }
        finally {
            if (yelpResponseInputStreamReader != null) {
                yelpResponseInputStreamReader.close();
                Log.d(TAG, "deserializeYelpResponseJson: Closed yelpResponseInputStreamReader");
            }
        }
    }
}
