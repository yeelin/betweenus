package com.example.yeelin.projects.betweenus.data.google.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ninjakiki on 2/26/16.
 * Total distance of this route
 */
public class Distance implements Parcelable {
    //uses unit specified in original request or origin's region
    private final String text;
    //total distance expressed in meters (always)
    private final int value;

    public Distance(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * This will be used only by the creator to reconstruct from the Parcel
     * @param in
     */
    protected Distance(Parcel in) {
        text = in.readString();
        value = in.readInt();
    }

    /**
     * This is required for deserializing data stored in Parcel
     */
    public static final Creator<Distance> CREATOR = new Creator<Distance>() {
        @Override
        public Distance createFromParcel(Parcel in) {
            return new Distance(in);
        }

        @Override
        public Distance[] newArray(int size) {
            return new Distance[size];
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