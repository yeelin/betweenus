package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.projects.betweenus.adapter.SuggestionsItem;
import com.example.yeelin.projects.betweenus.utils.CacheUtils;
import com.example.yeelin.projects.betweenus.utils.FetchDataUtils;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.example.yeelin.projects.betweenus.yelp.YelpApiHelper;

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
     * 4. returns an arraylist of SuggestionsItem
     *
     * @param context
     * @param searchTerm
     * @param userLocation
     * @param friendLocation
     */
    @Nullable
    public static ArrayList<SuggestionsItem> fetchFromNetwork(Context context, String searchTerm, Location userLocation, Location friendLocation) {
        Log.d(TAG, "fetchFromNetwork");

        //make sure we have network connection and latest SSL
        if (!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return null;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        try {
            //fetch data from Yelp
            ArrayList<SuggestionsItem> suggestedItems = fetchFromYelp(searchTerm, userLocation, friendLocation);
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
     * 4. returns an arraylist of SuggestionsItem
     *
     * @param searchTerm
     * @param userLocation
     * @param friendLocation
     */
    private static ArrayList<SuggestionsItem> fetchFromYelp(String searchTerm, Location userLocation, Location friendLocation) {
        Log.d(TAG, "fetchFromYelp");

        //build the url
        //open a http url connection to get data
        //build the result and return
        YelpApiHelper yelpApiHelper = new YelpApiHelper();
        Location midPoint = LocationUtils.computeMidPoint(userLocation, friendLocation);
        String yelpResponseJSON = yelpApiHelper.searchForBusinessesByGeoCoords(searchTerm, midPoint.getLatitude(), midPoint.getLongitude());

        //TODO:parse the json
        Log.d(TAG, "fetchFromYelp: JSON response: " + yelpResponseJSON);

        //return arraylist of place items
        return new ArrayList<>();
    }
}
