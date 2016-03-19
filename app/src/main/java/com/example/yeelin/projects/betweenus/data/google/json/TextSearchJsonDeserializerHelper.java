package com.example.yeelin.projects.betweenus.data.google.json;

import android.util.Log;

import com.example.yeelin.projects.betweenus.data.google.model.PlaceSearchResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ninjakiki on 3/18/16.
 */
public class TextSearchJsonDeserializerHelper {
    private static final String TAG = TextSearchJsonDeserializerHelper.class.getCanonicalName();

    public static PlaceSearchResult deserializeTextSearchResponse(InputStream responseJSON)
            throws IOException {
        //create a gson object
        final Gson gson = new GsonBuilder().create();

        InputStreamReader inputStreamReader = null;
        try {
            //create an input stream reader from the json input stream
            inputStreamReader = new InputStreamReader(responseJSON, "UTF-8");

            //deserialize json into java
            PlaceSearchResult result = gson.fromJson(inputStreamReader, PlaceSearchResult.class);

            //log for debugging purposes
            Log.d(TAG, "deserializeTextSearchResponse: PlaceSearchResult: " + result);
            return result;
        }
        finally {
            if (inputStreamReader != null) inputStreamReader.close();
        }
    }
}
