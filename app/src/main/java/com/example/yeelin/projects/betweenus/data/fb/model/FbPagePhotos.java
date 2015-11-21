package com.example.yeelin.projects.betweenus.data.fb.model;

import android.util.Log;

/**
 * Created by ninjakiki on 11/13/15.
 */
public class FbPagePhotos {
    private static final String TAG = FbPagePhotos.class.getCanonicalName();
    private final FbPhoto[] data;

    public FbPagePhotos(FbPhoto[] data) {
        this.data = data;
    }

    /**
     * Getter for photos.
     * Uses the width and height from fb response to find the source url matching the dimensions in the
     * images array.
     * @return
     */
    public FbPhoto[] getPhotos() {
        for (int i=0; i<data.length; i++) {
            final FbPhoto photo = data[i];

            int desiredHeight = photo.getHeight();
            int desiredWidth = photo.getWidth();
            int maxHeight = 0;
            int maxWidth = 0;
            int index = -1;

            final FbPhoto.FbPhotoImages[] photoImages = photo.getImages();
            if (photoImages != null) {
                for (int j = 0; j < photoImages.length; j++) {
                    if (desiredHeight == photoImages[j].getHeight() && desiredWidth == photoImages[j].getWidth()) {
                        String sourceUrl = photoImages[j].getSource();
                        data[i].setSourceUrl(sourceUrl);
                    }

                    if (maxHeight < photoImages[j].getHeight()) {
                        maxHeight = photoImages[j].getHeight();
                        index = j;
                    }

                    if (maxWidth < photoImages[j].getWidth()) {
                        maxWidth = photoImages[j].getWidth();
                        index = j;
                    }
                }

                Log.d(TAG, String.format("DesiredHeight:%d, DesiredWidth:%d",
                        desiredHeight, desiredWidth));
                data[i].setHighResSourceUrl(photoImages[index].getSource());
                data[i].setSourceUrl(photoImages[index].getSource());
            }
        }
        return data;
    }
}
