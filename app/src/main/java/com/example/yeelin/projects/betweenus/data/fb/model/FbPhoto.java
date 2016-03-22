package com.example.yeelin.projects.betweenus.data.fb.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.yeelin.projects.betweenus.data.LocalPhoto;

/**
 * Created by ninjakiki on 11/13/15.
 */
public class FbPhoto implements LocalPhoto {
    private final String id;
    private final String name;
    private final int height;
    private final int width;
    private final FbPhotoImages[] images;

    //derived member variable
    private String sourceUrl;
    private String highResSourceUrl;

    public FbPhoto(String id, String name, int height, int width, FbPhotoImages[] images) {
        this.id = id;
        this.name = name;
        this.height = height;
        this.width = width;
        this.images = images;
    }

    /**
     * This will be used only by the creator to reconstruct from the Parcel
     * @param in
     */
    protected FbPhoto(Parcel in) {
        id = in.readString();
        name = in.readString();
        height = in.readInt();
        width = in.readInt();
        images = in.createTypedArray(FbPhotoImages.CREATOR);
        //in.readTypedArray(images, FbPhotoImages.CREATOR);
        sourceUrl = in.readString();
        highResSourceUrl = in.readString();
    }

    /**
     * This is required for deserializing data stored in Parcel
     */
    public static final Creator<FbPhoto> CREATOR = new Creator<FbPhoto>() {
        @Override
        public FbPhoto createFromParcel(Parcel in) {
            return new FbPhoto(in);
        }

        @Override
        public FbPhoto[] newArray(int size) {
            return new FbPhoto[size];
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
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(height);
        dest.writeInt(width);
        dest.writeParcelableArray(images, flags);
        dest.writeString(sourceUrl);
        dest.writeString(highResSourceUrl);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public FbPhotoImages[] getImages() {
        return images;
    }

    @Override
    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getHighResSourceUrl() {
        return highResSourceUrl;
    }

    @Override
    public String getCaption() {
        return name;
    }

    @Override
    public String getAttribution() {
        return null;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setHighResSourceUrl(String highResSourceUrl) {
        this.highResSourceUrl = highResSourceUrl;
    }

    /**
     * FbPhotoImages
     */
    public static class FbPhotoImages implements Parcelable {
        private final int height;
        private final int width;
        private final String source;

        public FbPhotoImages(int height, int width, String source) {
            this.height = height;
            this.width = width;
            this.source = source;
        }

        /**
         * This will be used only by the creator to reconstruct from the Parcel
         * @param in
         */
        protected FbPhotoImages(Parcel in) {
            height = in.readInt();
            width = in.readInt();
            source = in.readString();
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
            dest.writeString(source);
        }

        /**
         * Describes the contents of the object being parcelled.
         * @return
         */
        @Override
        public int describeContents() {
            return hashCode();
        }

        /**
         * This is required for deserializing data stored in Parcel
         */
        public static final Creator<FbPhotoImages> CREATOR = new Creator<FbPhotoImages>() {
            @Override
            public FbPhotoImages createFromParcel(Parcel in) {
                return new FbPhotoImages(in);
            }

            @Override
            public FbPhotoImages[] newArray(int size) {
                return new FbPhotoImages[size];
            }
        };

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public String getSource() {
            return source;
        }
    }
}
