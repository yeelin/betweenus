package com.example.yeelin.projects.betweenus.data.yelp.json;

import android.util.Log;

import com.example.yeelin.projects.betweenus.data.yelp.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.data.yelp.model.YelpResult;
import com.example.yeelin.projects.betweenus.data.yelp.model.YelpResultRegion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by ninjakiki on 10/29/15.
 */
public class YelpJsonDeserializerHelper {
    //logcat
    private static final String TAG = YelpJsonDeserializerHelper.class.getCanonicalName();

    /**
     * Deserialize the yelp JSON response into a YelpResult object using GSON
     * @param yelpResponseJSON
     * @throws IOException
     */
    public static YelpResult deserializeYelpResponse(InputStream yelpResponseJSON)
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
            Log.d(TAG, "deserializeYelpResponse: Size of arraylist: " + yelpResult.getBusinesses().size());

            //log for debugging purposes
            Log.d(TAG, "deserializeYelpResponse: YelpResult: " + yelpResult);
            return yelpResult;
        }
        finally {
            if (yelpResponseInputStreamReader != null) {
                yelpResponseInputStreamReader.close();
                Log.d(TAG, "deserializeYelpResponse: Closed yelpResponseInputStreamReader");
            }
        }
    }

    /**
     * Deserialize the yelp JSON response into a YelpBusiness object using GSON
     * @param yelpResponseJSON
     * @throws IOException
     */
    public static YelpBusiness deserializeYelpSingleResponse(InputStream yelpResponseJSON)
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
            Log.d(TAG, "deserializeYelpResponse: YelpBusiness: " + yelpBusiness);
            return yelpBusiness;
        }
        finally {
            if (yelpResponseInputStreamReader != null) {
                yelpResponseInputStreamReader.close();
                Log.d(TAG, "deserializeYelpResponse: Closed yelpResponseInputStreamReader");
            }
        }
    }

    /**
     * YelpResultDeserializer class
     * Implements JsonDeserializer interface from GSON
     */
    private static class YelpResultDeserializer implements JsonDeserializer<YelpResult> {
        private static final String TAG = YelpResultDeserializer.class.getCanonicalName();

        @Override
        public YelpResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            Log.d(TAG, "deserialize");
            final JsonObject jsonObject = json.getAsJsonObject();

            YelpResultRegion yelpResultRegion = context.deserialize(jsonObject.get("region"), YelpResultRegion.class);
            int total = jsonObject.get("total").getAsInt();
            //YelpBusiness[] yelpBusinesses = context.deserialize(jsonObject.get("businesses"), YelpBusiness[].class);
            Type arrayListType = new TypeToken<ArrayList<YelpBusiness>>(){}.getType();
            ArrayList<YelpBusiness> yelpBusinessArrayList = context.deserialize(jsonObject.get("businesses"), arrayListType);

            final YelpResult yelpResult = new YelpResult(yelpResultRegion, total, yelpBusinessArrayList);
            return yelpResult;
        }
    }
}
