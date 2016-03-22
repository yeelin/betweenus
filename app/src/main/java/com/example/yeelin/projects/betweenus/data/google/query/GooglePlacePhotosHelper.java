package com.example.yeelin.projects.betweenus.data.google.query;

import android.net.Uri;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ninjakiki on 3/21/16.
 */
public class GooglePlacePhotosHelper {
    private static final String TAG = GooglePlacePhotosHelper.class.getCanonicalName();

    /**
     * Builds the URL for getting google place photos (via appengine).  Note that this URL will be used
     * by Piccasso.
     * @param photoReference
     * @param maxHeight
     * @param maxWidth
     * @return
     * @throws MalformedURLException
     */
    public static URL buildPlacePhotosUrl(String photoReference, int maxHeight, int maxWidth)
        throws MalformedURLException {
        Uri uri = new Uri.Builder()
                .scheme(LocalConstants.SCHEME)
                .authority(LocalConstants.AUTHORITY)
                .appendPath(LocalConstants.GOOGLE_PATH)
                .appendPath(LocalConstants.PLACE_PHOTOS_PATH)
                .appendQueryParameter(GoogleConstants.PlacePhotosParamNames.PHOTO_REFERENCE, photoReference)
                .appendQueryParameter(GoogleConstants.PlacePhotosParamNames.MAX_HEIGHT, String.valueOf(maxHeight))
                .appendQueryParameter(GoogleConstants.PlacePhotosParamNames.MAX_WIDTH, String.valueOf(maxWidth))
                .build();
        Log.d(TAG, "buildPlacePhotosUrl:" + uri.toString());
        return new URL(uri.toString());
    }
}
