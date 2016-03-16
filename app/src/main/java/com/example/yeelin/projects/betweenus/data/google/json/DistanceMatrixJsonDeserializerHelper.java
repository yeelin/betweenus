package com.example.yeelin.projects.betweenus.data.google.json;

import android.util.Log;

import com.example.yeelin.projects.betweenus.data.google.model.DistanceMatrixResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ninjakiki on 2/26/16.
 */
public class DistanceMatrixJsonDeserializerHelper {
    private static final String TAG = DistanceMatrixJsonDeserializerHelper.class.getCanonicalName();

    /**
     * Deserialize the distance matrix JSON response into a DistanceMatrixResult object using GSON
     * @param responseJSON
     * @return
     * @throws IOException
     */
    public static DistanceMatrixResult deserializeDistanceMatrixResponse(InputStream responseJSON)
            throws IOException {
        //create a gson object
        final Gson gson = new GsonBuilder().create();

        InputStreamReader inputStreamReader = null;
        try {
            //create an input stream reader from the json input stream
            inputStreamReader = new InputStreamReader(responseJSON, "UTF-8");

            //deserialize json into java
            DistanceMatrixResult result = gson.fromJson(inputStreamReader, DistanceMatrixResult.class); //TODO: change class type

            //log for debugging purposes
            Log.d(TAG, "deserializeDistanceMatrixResponse: DistanceMatrix: " + result);
            return result;
        }
        finally {
            if (inputStreamReader != null) inputStreamReader.close();
        }
    }
}
