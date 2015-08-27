package com.example.yeelin.projects.betweenus.adapter;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ninjakiki on 7/16/15.
 * This class encapsulates an item returned by the places autocomplete API
 */
public class LocationSearchItem implements Parcelable {
    //logcat
    private static final String TAG = LocationSearchItem.class.getCanonicalName();

    private final String description;
    private final String placeId;

    public LocationSearchItem(String description, String placeId) {
        this.description = description;
        this.placeId = placeId;
    }

    protected LocationSearchItem(Parcel in) {
        description = in.readString();
        placeId = in.readString();
    }

    public static final Creator<LocationSearchItem> CREATOR = new Creator<LocationSearchItem>() {
        @Override
        public LocationSearchItem createFromParcel(Parcel in) {
            return new LocationSearchItem(in);
        }

        @Override
        public LocationSearchItem[] newArray(int size) {
            return new LocationSearchItem[size];
        }
    };

    public String getDescription() {
        return description;
    }

    public String getPlaceId() {
        return placeId;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeString(placeId);
    }
}
