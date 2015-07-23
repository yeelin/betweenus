package com.example.yeelin.projects.betweenus.gson;

import android.util.Log;

import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.model.YelpResult;
import com.example.yeelin.projects.betweenus.model.YelpResultRegion;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/23/15.
 */
public class YelpResultDeserializer implements JsonDeserializer<YelpResult> {
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
