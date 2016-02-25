package com.example.yeelin.projects.betweenus.data.google.json;

import android.util.Log;

import com.example.yeelin.projects.betweenus.data.google.model.DirectionsResult;
import com.example.yeelin.projects.betweenus.data.google.model.Route;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 * Created by ninjakiki on 2/22/16.
 */
public class DirectionsJsonDeserializerHelper {
    private static final String TAG = DirectionsJsonDeserializerHelper.class.getCanonicalName();

    /**
     * Deserialize the directions JSON response into a DirectionsResult object using GSON
     * @param directionsResponseJSON
     * @return
     * @throws IOException
     */
    public static DirectionsResult deserializeDirectionsResponse(InputStream directionsResponseJSON)
        throws IOException {

        //create a gson object
        final Gson gson = new GsonBuilder().create();

        InputStreamReader inputStreamReader = null;
        try {
            //create an input stream reader from the json input stream
            inputStreamReader = new InputStreamReader(directionsResponseJSON, "UTF-8");

            //deserialize json into java
            DirectionsResult result = gson.fromJson(inputStreamReader, DirectionsResult.class);

            //log for debugging purposes
            Log.d(TAG, "deserializeDirectionsResponse: DirectionsResult: " + result);
            return result;
        }
        finally {
            if (inputStreamReader != null) inputStreamReader.close();
        }
    }

    /**
     * DirectionsResultDeserializer
     * Implements JsonDeserializer interface from GSON
     */
    private static class DirectionsResultDeserializer implements JsonDeserializer<DirectionsResult> {

        @Override
        public DirectionsResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject jsonObject = json.getAsJsonObject();

            final DirectionsResult.GeocodedWaypoint[] geocodedWaypoints = context.deserialize(jsonObject.get("geocoded_waypoints"), DirectionsResult.GeocodedWaypoint[].class);
            final Route[] routes = context.deserialize(jsonObject.get("routes"), Route[].class);

            final DirectionsResult result = new DirectionsResult(geocodedWaypoints, routes);
            return result;
        }
    }
}
