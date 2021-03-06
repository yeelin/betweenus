package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.loader.callback.SingleSuggestionLoaderListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 7/31/15.
 */
public class SingleSuggestionLoaderCallbacks
        implements LoaderManager.LoaderCallbacks<LocalBusiness> {
    //logcat
    private static final String TAG = SingleSuggestionLoaderCallbacks.class.getCanonicalName();

    //bundle args
    private static final String ARG_SEARCH_ID = SingleSuggestionLoaderCallbacks.class.getSimpleName() + ".searchId";
    private static final String ARG_IMAGE_HEIGHT = SuggestionsLoaderCallbacks.class.getSimpleName() + ".imageHeight";
    private static final String ARG_IMAGE_WIDTH = SuggestionsLoaderCallbacks.class.getSimpleName() + ".imageWidth";
    private static final String ARG_DATASOURCE = SingleSuggestionLoaderCallbacks.class.getSimpleName() + ".dataSource";

    //member variables
    private Context applicationContext;
    private WeakReference<SingleSuggestionLoaderListener> loaderListenerWeakRef;

    //list of accepted loader id constants
    @IntDef({SINGLE_PLACE})
    @Retention(RetentionPolicy.SOURCE) //tell compiler not to store annotation in .class files
    public @interface SinglePlaceLoaderId {} //declare the SinglePlaceLoaderId annotation
    public static final int SINGLE_PLACE = 200; //declare the actual constants

    /**
     * Helper method to initialize the loader and callbacks
     * @param loaderId
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param searchId
     * @param imageHeightPx
     * @param imageWidthPx
     * @param dataSource
     */
    public static void initLoader(@SinglePlaceLoaderId int loaderId, Context context, LoaderManager loaderManager, SingleSuggestionLoaderListener loaderListener,
                                  String searchId, int imageHeightPx, int imageWidthPx,
                                  int dataSource) {
        Bundle args = new Bundle(4);
        args.putString(ARG_SEARCH_ID, searchId);
        args.putInt(ARG_IMAGE_HEIGHT, imageHeightPx);
        args.putInt(ARG_IMAGE_WIDTH, imageWidthPx);
        args.putInt(ARG_DATASOURCE, dataSource);

        //call LoaderManager's init loader
        loaderManager.initLoader(loaderId,
                args,
                new SingleSuggestionLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to restart the loader
     * @param loaderId
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param searchId
     * @param imageHeightPx
     * @param imageWidthPx
     * @param dataSource
     */
    public static void restartLoader(@SinglePlaceLoaderId int loaderId, Context context, LoaderManager loaderManager, SingleSuggestionLoaderListener loaderListener,
                                     String searchId, int imageHeightPx, int imageWidthPx,
                                     int dataSource) {
        Bundle args = new Bundle(4);
        args.putString(ARG_SEARCH_ID, searchId);
        args.putInt(ARG_IMAGE_HEIGHT, imageHeightPx);
        args.putInt(ARG_IMAGE_WIDTH, imageWidthPx);
        args.putInt(ARG_DATASOURCE, dataSource);

        //call loaderManager's restart loader
        loaderManager.restartLoader(loaderId,
                args,
                new SingleSuggestionLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to destroy the loader
     * @param loaderId
     * @param loaderManager
     */
    public static void destroyLoader(@SinglePlaceLoaderId int loaderId, LoaderManager loaderManager) {
        loaderManager.destroyLoader(loaderId);
    }

    /**
     * Private constructor
     * @param context
     * @param loaderListener
     */
    private SingleSuggestionLoaderCallbacks(Context context, SingleSuggestionLoaderListener loaderListener) {
        applicationContext = context.getApplicationContext();
        loaderListenerWeakRef = new WeakReference<>(loaderListener);
    }

    /**
     *  Called by LoaderManager's initLoader method. Instantiate and return a new Loader for the given ID.
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<LocalBusiness> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        //read bundle args
        String searchId = args.getString(ARG_SEARCH_ID, "");
        int imageHeightPx = args.getInt(ARG_IMAGE_HEIGHT);
        int imageWidthPx = args.getInt(ARG_IMAGE_WIDTH);
        int dataSource = args.getInt(ARG_DATASOURCE, LocalConstants.YELP); //default to Yelp since it doesn't require login

        return new SingleSuggestionAsyncTaskLoader(applicationContext, searchId, imageHeightPx, imageWidthPx, dataSource);
    }

    /**
     * Loader has finished. Called when a previously created loader has finished its load. Notify listeners.
     * @param loader
     * @param localBusiness
     */
    @Override
    public void onLoadFinished(Loader<LocalBusiness> loader, LocalBusiness localBusiness) {
        Log.d(TAG, "onLoadFinished");
        SingleSuggestionLoaderListener loaderListener = loaderListenerWeakRef.get();
        if (loaderListener != null) {
            loaderListener.onLoadComplete(loader.getId(), localBusiness);
        }
    }

    /**
     * Loader has been reset. Called when a previously created loader is being reset, thus making its data unavailable.
     * Notify listeners with a null dataset.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<LocalBusiness> loader) {
        Log.d(TAG, "onLoaderReset");
        onLoadFinished(loader, null);
    }
}
