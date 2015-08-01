package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.util.Log;

import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.utils.CacheUtils;
import com.example.yeelin.projects.betweenus.utils.FetchDataUtils;
import com.example.yeelin.projects.betweenus.yelp.YelpApiHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ninjakiki on 7/31/15.
 */
public class SingleSuggestionLoaderHelper {
    //logcat
    private static final String TAG = SingleSuggestionLoaderHelper.class.getCanonicalName();

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
        YelpBusiness yelpBusiness  = deserializeYelpResponseJson(yelpResponseJSON);
        Log.d(TAG, "fetchFromYelp: YelpResult: " + yelpBusiness);

        return yelpBusiness;
    }

    /**
     * Deserialize the yelp JSON response using GSON
     * @param yelpResponseJSON
     * @throws IOException
     */
    private static YelpBusiness deserializeYelpResponseJson(InputStream yelpResponseJSON)
            throws IOException {

        //create a gson object
        final Gson gson = new GsonBuilder().create();

        InputStreamReader yelpResponseInputStreamReader = null;
        try {
            //create an input stream reader from the json input stream
            yelpResponseInputStreamReader = new InputStreamReader(yelpResponseJSON, "UTF-8");

            //deserialize json into java
            YelpBusiness yelpBusiness = gson.fromJson(yelpResponseInputStreamReader, YelpBusiness.class);

            //log for debugging purposes
            Log.d(TAG, "deserializeYelpResponseJson: YelpBusiness: " + yelpBusiness);
            return yelpBusiness;
        }
        finally {
            if (yelpResponseInputStreamReader != null) {
                yelpResponseInputStreamReader.close();
                Log.d(TAG, "deserializeYelpResponseJson: Closed yelpResponseInputStreamReader");
            }
        }
    }
}
