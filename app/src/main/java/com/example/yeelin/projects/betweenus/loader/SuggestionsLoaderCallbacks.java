package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;


import com.example.yeelin.projects.betweenus.model.YelpBusiness;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/20/15.
 */
public class SuggestionsLoaderCallbacks implements LoaderManager.LoaderCallbacks<ArrayList<YelpBusiness>> {
    //logcat
    private static final String TAG = SuggestionsLoaderCallbacks.class.getCanonicalName();

    //bundle args
    private static final String ARG_SEARCH_TERM = SuggestionsLoaderCallbacks.class.getSimpleName() + ".searchTerm";
    private static final String ARG_USER_LOCATION = SuggestionsLoaderCallbacks.class.getSimpleName() + ".userLocation";
    private static final String ARG_FRIEND_LOCATION = SuggestionsLoaderCallbacks.class.getSimpleName() + ".friendLocation";

    //member variables
    private Context applicationContext;
    private WeakReference<SuggestionsLoaderListener> loaderListenerWeakRef;

    /**
     * Listener interface. The loader's listener is usually the ui.
     */
    public interface SuggestionsLoaderListener {
        void onLoadComplete(LoaderId loaderId, @Nullable ArrayList<YelpBusiness> suggestedItems);
    }

    /**
     * Helper method to initialize the loader and callbacks
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param searchTerm
     * @param userLocation
     * @param friendLocation
     */
    public static void initLoader(Context context, LoaderManager loaderManager, SuggestionsLoaderListener loaderListener,
                                  String searchTerm, Location userLocation, Location friendLocation) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_TERM, searchTerm);
        args.putParcelable(ARG_USER_LOCATION, userLocation);
        args.putParcelable(ARG_FRIEND_LOCATION, friendLocation);

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
     * @param userLocation
     * @param friendLocation
     */
    public static void restartLoader(Context context, LoaderManager loaderManager, SuggestionsLoaderListener loaderListener,
                                     String searchTerm, Location userLocation, Location friendLocation) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_TERM, searchTerm);
        args.putParcelable(ARG_USER_LOCATION, userLocation);
        args.putParcelable(ARG_FRIEND_LOCATION, friendLocation);

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
        Location userLocation = args.getParcelable(ARG_USER_LOCATION);
        Location friendLocation = args.getParcelable(ARG_FRIEND_LOCATION);

        //create a new loader
        return new SuggestionsAsyncTaskLoader(applicationContext, searchTerm, userLocation, friendLocation);
    }

    /**
     * Loader has finished. Called when a previously created loader has finished its load. Notify listeners.
     * @param loader
     * @param suggestedItems
     */
    @Override
    public void onLoadFinished(Loader<ArrayList<YelpBusiness>> loader, ArrayList<YelpBusiness> suggestedItems) {
        Log.d(TAG, "onLoadFinished");
        SuggestionsLoaderListener loaderListener = loaderListenerWeakRef.get();
        if (loaderListener != null) {
            loaderListener.onLoadComplete(LoaderId.getLoaderIdForInt(loader.getId()), suggestedItems);
        }
    }

    /**
     * Loader has been reset. Called when a previously created loader is being reset, thus making its data unavailable.
     * Notify listeners with a null dataset.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<ArrayList<YelpBusiness>> loader) {
        Log.d(TAG, "onLoaderReset");
        //let the listener know
        onLoadFinished(loader, null);
    }
}
