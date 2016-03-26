package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Cache;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.StatsSnapshot;
import com.squareup.picasso.Target;

/**
 * Created by ninjakiki on 8/3/15.
 */
public class ImageUtils {
    //logcat
    public static final String TAG = ImageUtils.class.getCanonicalName();
    public static boolean customPicassoBuilt = false;

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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(new BitmapDrawable(context.getResources(), bitmap), null, null, null);
                    else
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
     * @param left which side to load the image on
     * @return
     */
    public static Target newTarget(final Context context, final TextView textView, final Marker marker, final boolean left) {
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
                    if (left) {
                        textView.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(context.getResources(), bitmap), null, null, null);
                    }
                    else {
                        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, new BitmapDrawable(context.getResources(), bitmap), null);
                    }

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
        //if (!customPicassoBuilt) buildCustomPicassoInstance(context);
        Log.d(TAG, "loadImage with imageView: Url:" + imageUrl);
        Picasso.with(context).setIndicatorsEnabled(true);
        Picasso.with(context).setLoggingEnabled(true);

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
        //if (!customPicassoBuilt) buildCustomPicassoInstance(context);
        Log.d(TAG, "loadImage with imageView: Url:" + imageUrl);
        Picasso.with(context).setIndicatorsEnabled(true);
        Picasso.with(context).setLoggingEnabled(true);

        Picasso.with(context)
                .load(imageUrl)
                .placeholder(downloadPlaceHolder)
                .error(errorPlaceHolder)
                .into(imageView);
    }

    /**
     * Loads an image into the given image view. Placeholders are provided for download and error cases
     * @param context
     * @param imageUrl
     * @param imageView
     * @param width
     * @param height
     * @param downloadPlaceHolder
     * @param errorPlaceHolder
     */
    public static void loadImage(final Context context, final String imageUrl, final ImageView imageView,
                                 int width, int height,
                                 final int downloadPlaceHolder, final int errorPlaceHolder) {
        //if (!customPicassoBuilt) buildCustomPicassoInstance(context);
        Log.d(TAG, "loadImage with imageView: Url:" + imageUrl);
        Picasso.with(context).setIndicatorsEnabled(true);
        Picasso.with(context).setLoggingEnabled(true);

        Picasso.with(context)
                .load(imageUrl)
                .placeholder(downloadPlaceHolder)
                .error(errorPlaceHolder)
                .resize(width, height)
                .onlyScaleDown()
                .centerCrop()
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
        //if (!customPicassoBuilt) buildCustomPicassoInstance(context);
        Log.d(TAG, "loadImage with target: Url: " + imageUrl);
        Picasso.with(context).setIndicatorsEnabled(true);
        Picasso.with(context).setLoggingEnabled(true);

        Picasso.with(context)
                .load(imageUrl)
                .into(target);
    }

    /**
     *
     * @param context
     */
    public static void buildCustomPicassoInstance(Context context) {
        Log.d(TAG, "buildCustomPicassoInstance");
        customPicassoBuilt = true;

        //long PICASSO_DISK_CACHE_SIZE = 50 * 1024 * 1024;
        //Downloader downloader = new OkHttpDownloader(context.getApplicationContext(), PICASSO_DISK_CACHE_SIZE);

        int PICASSO_MEMORY_CACHE_SIZE = 50 * 1024 * 1024;
        Cache memoryCache = new LruCache(PICASSO_MEMORY_CACHE_SIZE);

        Picasso picasso = new Picasso.Builder(context.getApplicationContext())
                //.downloader(downloader)
                .memoryCache(memoryCache)
                .build();
        Picasso.setSingletonInstance(picasso);

        Picasso.with(context).setLoggingEnabled(true);
        Picasso.with(context).setIndicatorsEnabled(true);
    }

    /**
     *
     * @param context
     */
    public static void printStats(Context context) {
        StatsSnapshot statsSnapshot = Picasso.with(context).getSnapshot();
        Log.d(TAG, "Picasso Stats:" + statsSnapshot);
    }

}
