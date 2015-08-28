package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by ninjakiki on 8/3/15.
 */
public class ImageUtils {
    //logcat
    public static final String TAG = ImageUtils.class.getCanonicalName();

    /**
     * Creates a new target so that the downloaded bitmap can be loaded into the custom view.
     * Picasso api doesn't load into textviews.
     * @param context
     * @param textView
     * @return
     */
    public static Target newTarget(final Context context, final TextView textView) {
        return new Target() {
            /**
             * This method will not be called if Target is garbage-collected early. Note
             * picasso only holds a weak reference to Target.
             * @param bitmap
             * @param from
             */
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (textView != null) {
                    //Log.d(TAG, "onBitmapLoaded: Textview is not null");
                    textView.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(context.getResources(), bitmap), null, null, null);
                }
                else {
                    //Log.d(TAG, "onBitmapLoaded: Textview is already null so nothing to do");
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d(TAG, "onBitmapFailed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                //Log.d(TAG, "onPrepareLoad");
            }
        };
    }

    /**
     * Creates a new target so that the downloaded bitmap can be loaded into the custom view.
     * Picasso api doesn't load into textviews.  This version is for map markers.
     * @param context
     * @param textView
     * @param marker
     * @return
     */
    public static Target newTarget(final Context context, final TextView textView, final Marker marker) {
        //Log.d(TAG, "newTarget");
        return new Target() {
            /**
             * This method will not be called if Target is garbage-collected early. Note
             * picasso only holds a weak reference to Target.
             * @param bitmap
             * @param from
             */
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                if (textView != null) {
                    //Log.d(TAG, "onBitmapLoaded: Textview is not null");
                    textView.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(context.getResources(), bitmap), null, null, null);

                    if (marker != null && marker.isInfoWindowShown()) {
                        //Log.d(TAG, "onBitmapLoaded: Showing info window");
                        marker.showInfoWindow();
                    }
                }
                else {
                    //Log.d(TAG, "onBitmapLoaded: Textview is already null so nothing to do");
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d(TAG, "onBitmapFailed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                //Log.d(TAG, "onPrepareLoad");
            }
        };
    }

    /**
     * Loads an image into the given image view
     * @param context
     * @param imageUrl
     * @param imageView
     */
    public static void loadImage(final Context context, final String imageUrl, final ImageView imageView) {
        //Log.d(TAG, "loadImage with imageView: Url:" + imageUrl);
        Picasso.with(context)
                .load(imageUrl)
                .into(imageView);
    }

    /**
     * Loads an image into the given image view. Placeholders are provided for download and error cases
     * @param context
     * @param imageUrl
     * @param imageView
     * @param downloadPlaceHolder
     * @param errorPlaceHolder
     */
    public static void loadImage(final Context context, final String imageUrl, final ImageView imageView,
                                 final int downloadPlaceHolder, final int errorPlaceHolder) {
        //Log.d(TAG, "loadImage with imageView: Url:" + imageUrl);
        Picasso.with(context)
                .load(imageUrl)
                .placeholder(downloadPlaceHolder)
                .error(errorPlaceHolder)
                .into(imageView);
    }

    /**
     * Loads an image into the given target.
     * Picasso only keeps a weak reference to the Target object. While you can store a strong reference Target
     * in one of your classes, this can still be problematic if the Target references a View in any manner,
     * since you'll effectively also be keeping a strong reference to that View as well (which is one of the
     * things that Picasso explicitly helps you avoid).
     * @param context
     * @param imageUrl
     * @param target
     */
    public static void loadImage(final Context context, final String imageUrl, final Target target) {
        //Log.d(TAG, "loadImage with target: Url: " + imageUrl);
        Picasso.with(context)
                .load(imageUrl)
                .into(target);
    }
}
