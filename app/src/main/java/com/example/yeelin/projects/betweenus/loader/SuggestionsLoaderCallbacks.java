package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.example.yeelin.projects.betweenus.loader.callback.SuggestionsLoaderListener;
import com.google.android.gms.maps.model.LatLng;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
    private static final String ARG_PAGE_URL = SuggestionsLoaderCallbacks.class.getSimpleName() + ".pageUrl";
    private static final String ARG_PAGING_DIRECTION = SuggestionsLoaderCallbacks.class.getSimpleName() + ".pagingDirection";

    //member variables
    private Context applicationContext;
    private WeakReference<SuggestionsLoaderListener> loaderListenerWeakRef;

    //list of accepted loader id constants
    @IntDef({MULTI_PLACES_INITIAL, MULTI_PLACES_SUBSEQUENT})
    @Retention(RetentionPolicy.SOURCE) //tell compiler not to store annotation in .class files
    public @interface MultiPlacesLoaderId {} //declare the MultiPlacesLoaderId annotation
    public static final int MULTI_PLACES_INITIAL = 100; //declare the actual constants
    public static final int MULTI_PLACES_SUBSEQUENT = 101;

    /**
     * Helper method to initialize the loader and callbacks
     * @param loaderId
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
    public static void initLoader(@MultiPlacesLoaderId int loaderId, Context context, LoaderManager loaderManager, SuggestionsLoaderListener loaderListener,
                                  String searchTerm, LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng,
                                  int imageHeightPx, int imageWidthPx, int dataSource) {
        Bundle args = new Bundle(7);
        args.putString(ARG_SEARCH_TERM, searchTerm);
        args.putParcelable(ARG_USER_LATLNG, userLatLng);
        args.putParcelable(ARG_FRIEND_LATLNG, friendLatLng);
        args.putParcelable(ARG_MID_LATLNG, midLatLng);
        args.putInt(ARG_IMAGE_HEIGHT, imageHeightPx);
        args.putInt(ARG_IMAGE_WIDTH, imageWidthPx);
        args.putInt(ARG_DATASOURCE, dataSource);

        //call loaderManager's initLoader
        loaderManager.initLoader(loaderId,
                args,
                new SuggestionsLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to restart the loader with the nextUrl for the next page of data
     * @param loaderId
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param url
     * @param pagingDirection
     * @param dataSource
     */
    public static void restartLoader(@MultiPlacesLoaderId int loaderId, Context context, LoaderManager loaderManager, SuggestionsLoaderListener loaderListener,
                                     @NonNull String url, int pagingDirection, int dataSource) {
        Bundle args = new Bundle(3);
        args.putString(ARG_PAGE_URL, url);
        args.putInt(ARG_PAGING_DIRECTION, pagingDirection);
        args.putInt(ARG_DATASOURCE, dataSource);

        //call loaderManager's restart loader
        loaderManager.restartLoader(loaderId,
                args,
                new SuggestionsLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to destroy the loader
     * @param loaderId
     * @param loaderManager
     */
    public static void destroyLoader(@MultiPlacesLoaderId int loaderId, LoaderManager loaderManager) {
        //call loaderManager's destroy loader
        loaderManager.destroyLoader(loaderId);
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
        int dataSource = args.getInt(ARG_DATASOURCE, LocalConstants.YELP);
        String searchTerm = args.getString(ARG_SEARCH_TERM, null);
        if (searchTerm != null) {
            LatLng userLatLng = args.getParcelable(ARG_USER_LATLNG);
            LatLng friendLatLng = args.getParcelable(ARG_FRIEND_LATLNG);
            LatLng midLatLng = args.getParcelable(ARG_MID_LATLNG);
            int imageHeightPx = args.getInt(ARG_IMAGE_HEIGHT);
            int imageWidthPx = args.getInt(ARG_IMAGE_WIDTH);

            //create a new loader for the initial search request
            return new SuggestionsAsyncTaskLoader(applicationContext, searchTerm, userLatLng, friendLatLng, midLatLng,
                    imageHeightPx, imageWidthPx, dataSource);
        }

        //create a new loader for the next page of results
        String url = args.getString(ARG_PAGE_URL, null);
        int pagingDirection = args.getInt(ARG_PAGING_DIRECTION, LocalConstants.NEXT_PAGE); //default is the next page of results
        return new SuggestionsAsyncTaskLoader(applicationContext, url, pagingDirection, dataSource);
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
            loaderListener.onLoadComplete(loader.getId(), localResult);
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
