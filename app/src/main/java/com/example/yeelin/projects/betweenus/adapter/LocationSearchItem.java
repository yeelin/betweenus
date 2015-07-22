package com.example.yeelin.projects.betweenus.adapter;

/**
 * Created by ninjakiki on 7/16/15.
 * This class encapsulates an item returned by the places autocomplete API
 */
public class LocationSearchItem {
    //logcat
    private static final String TAG = LocationSearchItem.class.getCanonicalName();

    private final String description;
    private final String placeId;

    public LocationSearchItem(String description, String placeId) {
        this.description = description;
        this.placeId = placeId;
    }

    public String getDescription() {
        return description;
    }

    public String getPlaceId() {
        return placeId;
    }
}
