package com.example.yeelin.projects.betweenus.data.fb.model;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalPhoto;
import com.example.yeelin.projects.betweenus.data.LocalPhotosResult;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ninjakiki on 11/13/15.
 */
public class FbPagePhotos implements LocalPhotosResult {
    private static final String TAG = FbPagePhotos.class.getCanonicalName();
    private final FbPhoto[] data;
    private final FbPagination paging;
    private boolean isProcessed;

    public FbPagePhotos(FbPhoto[] data, FbPagination paging) {
        this.data = data;
        this.paging = paging;
    }

    /**
     * Getter for photos.
     * Uses the width and height from fb response to find the source url matching the dimensions in the
     * images array.
     * @return
     */
    public FbPhoto[] getPhotos() {
        if (!isProcessed) {
            processPhotos();
        }
        return data;
    }

    public void processPhotos() {
        isProcessed = true;

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
    }

    @Nullable
    public FbPagination getPaging() {
        return paging;
    }

    @Nullable
    public String getPreviousPhotosUrl() {
        return paging != null ? paging.getPrevious() : null;
    }

    @Nullable
    public String getNextPhotosUrl() {
        return paging != null ? paging.getNext() : null;
    }

    @Nullable
    public String getAfterId() {
        return paging != null ? paging.getCursors().getAfter() : null;
    }

    @Nullable
    public String getBeforeId() {
        return paging != null ? paging.getCursors().getBefore() : null;
    }

    @Override
    public ArrayList<LocalPhoto> getLocalPhotos() {
        return new ArrayList<LocalPhoto>(Arrays.asList(data)); //TODO: Change base data structure to Arraylist
    }

    @Override
    public String getNextId() {
        return getAfterId();
    }

    @Override
    public String getNextUrl() {
        return getNextPhotosUrl();
    }

    /**
     * FbPagination class
     */
    public static class FbPagination {
        private final FbCursor cursors;
        private final String previous;
        private final String next;

        public FbPagination(FbCursor cursors, String previous, String next) {
            this.cursors = cursors;
            this.previous = previous;
            this.next = next;
        }

        public FbCursor getCursors() {
            return cursors;
        }

        public String getPrevious() {
            return previous;
        }

        public String getNext() {
            return next;
        }
    }

    /**
     * FbCursor class
     */
    public static class FbCursor {
        private final String after;
        private final String before;

        public FbCursor(String after, String before) {
            this.after = after;
            this.before = before;
        }

        public String getAfter() {
            return after;
        }

        public String getBefore() {
            return before;
        }
    }
}
