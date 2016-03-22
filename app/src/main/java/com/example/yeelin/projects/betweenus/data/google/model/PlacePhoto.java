package com.example.yeelin.projects.betweenus.data.google.model;

import android.os.Parcel;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalPhoto;
import com.example.yeelin.projects.betweenus.data.google.query.GooglePlacePhotosHelper;

import java.net.MalformedURLException;
import java.util.Arrays;

/**
 * Place Photo
 * A Place Search will return at most one photo object.
 * Performing a Place Details request on the place may return up to ten photos.
 */
public class PlacePhoto implements LocalPhoto {
    private static final String TAG = PlacePhoto.class.getCanonicalName();

    //the maximum height of the image.
    private final int height;
    //the maximum width of the image.
    private final int width;
    //contains any required attributions. This field will always be present, but may be empty.
    private final String[] html_attributions;
    //a string used to identify the photo when you perform a Photo request.
    private final String photo_reference;

    public PlacePhoto(int height, int width, String[] html_attributions, String photo_reference) {
        this.height = height;
        this.width = width;
        this.html_attributions = html_attributions;
        this.photo_reference = photo_reference;
    }

    /**
     * This will be used only by the creator to reconstruct from the Parcel
     * @param in
     */
    protected PlacePhoto(Parcel in) {
        height = in.readInt();
        width = in.readInt();
        html_attributions = in.createStringArray();
        //in.readStringArray(html_attributions);
        photo_reference = in.readString();
    }

    /**
     * This is required for deserializing data stored in Parcel
     */
    public static final Creator<PlacePhoto> CREATOR = new Creator<PlacePhoto>() {
        @Override
        public PlacePhoto createFromParcel(Parcel in) {
            return new PlacePhoto(in);
        }

        @Override
        public PlacePhoto[] newArray(int size) {
            return new PlacePhoto[size];
        }
    };

    /**
     * Describes the contents of the object being parcelled.
     * @return
     */
    @Override
    public int describeContents() {
        return hashCode();
    }

    /**
     * Actual object serialization happens here. Each element of the object is
     * individually parcelled.
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(height);
        dest.writeInt(width);
        dest.writeStringArray(html_attributions);
        dest.writeString(photo_reference);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String[] getHtml_attributions() {
        return html_attributions;
    }

    public String getPhoto_reference() {
        return photo_reference;
    }

    @Override
    public String toString() {
        return String.format("Height:%d, Width:%d, Attributions:%s, Reference:%s",
                height, width, Arrays.toString(html_attributions), photo_reference);
    }

    @Override
    public String getSourceUrl() {
        if (photo_reference == null) return null;
        String photoUrl = null;
        try {
            photoUrl = GooglePlacePhotosHelper.buildPlacePhotosUrl(
                    photo_reference, height, width).toString();
        }
        catch (MalformedURLException e) {
            Log.d(TAG, "getSourceUrl: Unexpected MalformedURLException: + " + e.getLocalizedMessage());
        }
        return photoUrl;
    }

    @Override
    public String getCaption() {
        return null; //google photos do not have captions
    }

    @Override
    public String getAttribution() {
        return html_attributions != null ? Arrays.toString(html_attributions) : null;
    }
}