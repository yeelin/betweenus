package com.example.yeelin.projects.betweenus.data.google.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ninjakiki on 2/26/16.
 * Length of time it takes to travel this route
 */
public class Duration implements Parcelable {
    //localized using query's language parameter
    private final String text;
    //expressed in seconds
    private final int value;

    public Duration(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * This will be used only by the creator to reconstruct from the Parcel
     * @param in
     */
    protected Duration(Parcel in) {
        text = in.readString();
        value = in.readInt();
    }

    /**
     * This is required for deserializing data stored in Parcel
     */
    public static final Creator<Duration> CREATOR = new Creator<Duration>() {
        @Override
        public Duration createFromParcel(Parcel in) {
            return new Duration(in);
        }

        @Override
        public Duration[] newArray(int size) {
            return new Duration[size];
        }
    };

    public int getValue() {
        return value;
    }
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return String.format("Text:%s,Value:%d", text, value);
    }

    /**
     * Describes the contents of the object being parcelled
     * @return
     */
    @Override
    public int describeContents() {
        return hashCode();
    }

    /**
     * Actual object serialization happens here. Each element of the object is individually
     * parcelled.
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeInt(value);
    }
}