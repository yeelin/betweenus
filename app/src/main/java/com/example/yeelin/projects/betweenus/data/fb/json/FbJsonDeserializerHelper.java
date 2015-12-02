package com.example.yeelin.projects.betweenus.data.fb.json;

import android.util.Log;

import com.example.yeelin.projects.betweenus.data.fb.model.FbPage;
import com.example.yeelin.projects.betweenus.data.fb.model.FbPagePhotos;
import com.example.yeelin.projects.betweenus.data.fb.model.FbPagination;
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
        Log.d(TAG, "deserializeFbResponse: FbResult:" + fbResult);
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
        return fbPage;
    }

    /**
     * Deserializes the raw json response for a single page's photos into a FbPagePhotos object using GSON
     * @param rawJson
     * @return
     */
    public static FbPagePhotos deserializeFbPhotosResponse(String rawJson) {
        //create a gson object
        final Gson gson = new GsonBuilder().create();

        //deserialize json into java
        final FbPagePhotos fbPagePhotos = gson.fromJson(rawJson, FbPagePhotos.class);
        return fbPagePhotos;
    }

    /**
     * FbResultDeserializer class
     * Implements JsonDeserializer interface from GSON
     */
    private static class FbResultDeserializer implements JsonDeserializer<FbResult> {
        @Override
        public FbResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            final JsonObject jsonObject = json.getAsJsonObject();

            //get data as ArrayList<FbPage> type
            Type arrayListType = new TypeToken<ArrayList<FbPage>>(){}.getType();
            final ArrayList<FbPage> fbPageArrayList = context.deserialize(jsonObject.get("data"), arrayListType);

            //get paging as FbPagination type
            final FbPagination paging = context.deserialize(jsonObject.get("paging"), FbPagination.class);

            //construct FbResult
            final FbResult fbResult = new FbResult(fbPageArrayList, paging);

            return fbResult;
        }
    }
}
