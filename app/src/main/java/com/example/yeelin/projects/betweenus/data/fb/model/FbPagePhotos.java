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

                //Log.d(TAG, String.format("DesiredHeight:%d, DesiredWidth:%d", desiredHeight, desiredWidth));
                data[i].setHighResSourceUrl(photoImages[index].getSource());
                data[i].setSourceUrl(photoImages[index].getSource());
            }
        }
    }

    @Nullable
    public FbPagination getPaging() {
        return paging;
    }

    /**
     * The Graph API endpoint that will return the previous page of data.
     * If not included, this is the first page of data.
     * @return
     */
    @Nullable
    public String getPreviousUrl() {
        return paging != null ? paging.getPrevious() : null;
    }

    /**
     * The Graph API endpoint that will return the next page of data.
     * If not included, this is the last page of data.
     * Due to how pagination works with visibility and privacy it is possible that a page
     * may be empty but contain a 'next' paging link - you should stop paging
     * when the 'next' link no longer appears.
     * @return
     */
    @Nullable
    @Override
    public String getNextUrl() {
        return paging != null ? paging.getNext() : null;
    }

    /**
     * This is the cursor that points to the end of the page of data that has been returned.
     * @return
     */
    @Nullable
    @Override
    public String getAfterId() {
        return paging != null ? paging.getCursors().getAfter() : null;
    }

    /**
     * This is the cursor that points to the start of the page of data that has been returned.
     * @return
     */
    @Nullable
    public String getBeforeId() {
        return paging != null ? paging.getCursors().getBefore() : null;
    }

    @Override
    public ArrayList<LocalPhoto> getLocalPhotos() {
        return new ArrayList<LocalPhoto>(Arrays.asList(data)); //TODO: Change base data structure to Arraylist
    }
}
