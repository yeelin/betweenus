package com.example.yeelin.projects.betweenus.data.fb.json;

import android.util.Log;

import com.example.yeelin.projects.betweenus.data.fb.model.FbPage;
import com.example.yeelin.projects.betweenus.data.fb.model.FbResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ninjakiki on 10/27/15.
 */
public class FbJsonDeserializerHelper {
    //logcat
    private static final String TAG = FbJsonDeserializerHelper.class.getCanonicalName();

    /**
     * Deserializes the raw json response for multiple pages into a FbResult object using GSON
     * @param rawJson
     * @return
     */
    public static FbResult deserializeFbResponse(String rawJson) {
        //create a gson object
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(FbResult.class, new FbResultDeserializer());
        final Gson gson = gsonBuilder.create();

        //deserialize json into java
        final FbResult fbResult = gson.fromJson(rawJson, FbResult.class);
        Log.d(TAG, String.format("deserializeFbResponse: Size:%d, FbResult:%s", fbResult.getPages().size(), fbResult));
        return fbResult;
    }

    /**
     * Deserializes the raw json response for a single page into a FbPage object using GSON
     * @param rawJson
     * @return
     */
    public static FbPage deserializeFbSingleResponse(String rawJson) {
        //create a gson object
        final Gson gson = new GsonBuilder().create();

        //deserialize json into java
        final FbPage fbPage = gson.fromJson(rawJson, FbPage.class);
        Log.d(TAG, "deserializeFbSingleResponse: FbPage:" + fbPage);
        return fbPage;
    }

    /**
     * FbResultDeserializer class
     * Implements JsonDeserializer interface from GSON
     */
    private static class FbResultDeserializer implements JsonDeserializer<FbResult> {
        private static final String TAG = FbResultDeserializer.class.getCanonicalName();

        @Override
        public FbResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            Log.d(TAG, "deserialize");

            final JsonObject jsonObject = json.getAsJsonObject();
            Type arrayListType = new TypeToken<ArrayList<FbPage>>(){}.getType();
            ArrayList<FbPage> fbPageArrayList = context.deserialize(jsonObject.get("data"), arrayListType);

            final FbResult fbResult = new FbResult(fbPageArrayList);
            return fbResult;
        }
    }
}
