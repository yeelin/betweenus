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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/20/15.
 */
public class SuggestionsLoaderHelper {
    private static final String TAG = SuggestionsLoaderHelper.class.getCanonicalName();

    /**
     * Called from a bg thread. This method does the following:
     * 1. Makes sure we passed pre-network checks
     * 2. initialize the cache
     * 3. makes the call to the Yelp API
     * 4. returns an arraylist of YelpBusiness
     *
     * @param context
     * @param searchTerm
     * @param userLocation
     * @param friendLocation
     */
    @Nullable
    public static ArrayList<YelpBusiness> fetchFromNetwork(Context context, String searchTerm, Location userLocation, Location friendLocation) {
        Log.d(TAG, "fetchFromNetwork");

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        try {
            //fetch data from Yelp
            ArrayList<YelpBusiness> suggestedItems = fetchFromYelp(searchTerm, userLocation, friendLocation);
            CacheUtils.logCache();
            return suggestedItems;
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
     * @param userLocation
     * @param friendLocation
     */
    private static ArrayList<YelpBusiness> fetchFromYelp(String searchTerm, Location userLocation, Location friendLocation)
            throws IOException {
        Log.d(TAG, "fetchFromYelp");

        //build the url
        //open a http url connection to get data
        //build the result and return
        YelpApiHelper yelpApiHelper = new YelpApiHelper();
        Location midPoint = LocationUtils.computeMidPoint(userLocation, friendLocation);
        InputStream yelpResponseJSON = yelpApiHelper.searchForBusinessesByGeoCoords(searchTerm, midPoint.getLatitude(), midPoint.getLongitude());

        //deserialize the json response
        YelpResult yelpResult = deserializeYelpResponseJson(yelpResponseJSON);
        Log.d(TAG, "fetchFromYelp: YelpResult: " + yelpResult);

        //return arraylist businesses
        return yelpResult.getBusinesses();
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
