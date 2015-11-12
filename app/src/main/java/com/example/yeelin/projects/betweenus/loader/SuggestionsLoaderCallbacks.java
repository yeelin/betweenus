package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.example.yeelin.projects.betweenus.loader.callback.SuggestionsLoaderListener;
import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 7/20/15.
 */
public class SuggestionsLoaderCallbacks implements LoaderManager.LoaderCallbacks<LocalResult> {
    //logcat
    private static final String TAG = SuggestionsLoaderCallbacks.class.getCanonicalName();

    //bundle args
    private static final String ARG_SEARCH_TERM = SuggestionsLoaderCallbacks.class.getSimpleName() + ".searchTerm";
    private static final String ARG_USER_LATLNG = SuggestionsLoaderCallbacks.class.getSimpleName() + ".userLatLng";
    private static final String ARG_FRIEND_LATLNG = SuggestionsLoaderCallbacks.class.getSimpleName() + ".friendLatLng";
    private static final String ARG_MID_LATLNG = SuggestionsLoaderCallbacks.class.getSimpleName() + ".midLatLng";
    private static final String ARG_IMAGE_HEIGHT = SuggestionsLoaderCallbacks.class.getSimpleName() + ".imageHeight";
    private static final String ARG_IMAGE_WIDTH = SuggestionsLoaderCallbacks.class.getSimpleName() + ".imageWidth";
    private static final String ARG_DATASOURCE = SuggestionsLoaderCallbacks.class.getSimpleName() + ".dataSource";

    //member variables
    private Context applicationContext;
    private WeakReference<SuggestionsLoaderListener> loaderListenerWeakRef;

    /**
     * Helper method to initialize the loader and callbacks
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param searchTerm
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng midpoint between userLatLng and friendLatLng
     * @param imageHeightPx
     * @param imageWidthPx
     * @param dataSource
     */
    public static void initLoader(Context context, LoaderManager loaderManager, SuggestionsLoaderListener loaderListener,
                                  String searchTerm, LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng,
                                  int imageHeightPx, int imageWidthPx,
                                  int dataSource) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_TERM, searchTerm);
        args.putParcelable(ARG_USER_LATLNG, userLatLng);
        args.putParcelable(ARG_FRIEND_LATLNG, friendLatLng);
        args.putParcelable(ARG_MID_LATLNG, midLatLng);
        args.putInt(ARG_IMAGE_HEIGHT, imageHeightPx);
        args.putInt(ARG_IMAGE_WIDTH, imageWidthPx);
        args.putInt(ARG_DATASOURCE, dataSource);

        //call loaderManager's initLoader
        loaderManager.initLoader(LoaderId.MULTI_PLACES.getValue(),
                args,
                new SuggestionsLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to restart the loader
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param searchTerm
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng midpoint between userLatLng and friendLatLng
     * @param imageHeightPx
     * @param imageWidthPx
     * @param dataSource
     */
    public static void restartLoader(Context context, LoaderManager loaderManager, SuggestionsLoaderListener loaderListener,
                                     String searchTerm, LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng,
                                     int imageHeightPx, int imageWidthPx,
                                     int dataSource) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_TERM, searchTerm);
        args.putParcelable(ARG_USER_LATLNG, userLatLng);
        args.putParcelable(ARG_FRIEND_LATLNG, friendLatLng);
        args.putParcelable(ARG_MID_LATLNG, midLatLng);
        args.putInt(ARG_IMAGE_HEIGHT, imageHeightPx);
        args.putInt(ARG_IMAGE_WIDTH, imageWidthPx);
        args.putInt(ARG_DATASOURCE, dataSource);


        //call loaderManager's restart loader
        loaderManager.restartLoader(LoaderId.MULTI_PLACES.getValue(),
                args,
                new SuggestionsLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to destroy the loader
     * @param loaderManager
     */
    public static void destroyLoader(LoaderManager loaderManager) {
        //call loaderManager's destroy loader
        loaderManager.destroyLoader(LoaderId.MULTI_PLACES.getValue());
    }

    /**
     * Private constructor
     * @param context
     * @param loaderListener
     */
    private SuggestionsLoaderCallbacks(Context context, SuggestionsLoaderListener loaderListener) {
        applicationContext = context.getApplicationContext();
        loaderListenerWeakRef = new WeakReference<>(loaderListener);
    }

    /**
     * Called by LoaderManager's initLoader method. Instantiate and return a new Loader for the given ID.
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        //read bundle args
        String searchTerm = args.getString(ARG_SEARCH_TERM, "");
        LatLng userLatLng = args.getParcelable(ARG_USER_LATLNG);
        LatLng friendLatLng = args.getParcelable(ARG_FRIEND_LATLNG);
        LatLng midLatLng = args.getParcelable(ARG_MID_LATLNG);
        int imageHeightPx = args.getInt(ARG_IMAGE_HEIGHT);
        int imageWidthPx = args.getInt(ARG_IMAGE_WIDTH);
        int dataSource = args.getInt(ARG_DATASOURCE, LocalConstants.YELP);

        //create a new loader
        return new SuggestionsAsyncTaskLoader(applicationContext, searchTerm, userLatLng, friendLatLng, midLatLng,
                imageHeightPx, imageWidthPx, dataSource);
    }

    /**
     * Loader has finished. Called when a previously created loader has finished its load. Notify listeners.
     * @param loader
     * @param localResult
     */
    @Override
    public void onLoadFinished(Loader<LocalResult> loader, LocalResult localResult) {
        Log.d(TAG, "onLoadFinished");
        SuggestionsLoaderListener loaderListener = loaderListenerWeakRef.get();
        if (loaderListener != null) {
            loaderListener.onLoadComplete(LoaderId.getLoaderIdForInt(loader.getId()), localResult);
        }
    }

    /**
     * Loader has been reset. Called when a previously created loader is being reset, thus making its data unavailable.
     * Notify listeners with a null dataset.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<LocalResult> loader) {
        Log.d(TAG, "onLoaderReset");
        //let the listener know
        onLoadFinished(loader, null);
    }
}
