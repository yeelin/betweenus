package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.google.model.DirectionsResult;
import com.example.yeelin.projects.betweenus.loader.callback.DirectionsLoaderListener;
import com.google.android.gms.maps.model.LatLng;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 2/22/16.
 */
public class DirectionsLoaderCallbacks implements LoaderManager.LoaderCallbacks<DirectionsResult> {
    //log cat
    private static final String TAG = DirectionsLoaderCallbacks.class.getCanonicalName();

    //bundle args
    private static final String ARG_ORIGIN = DirectionsLoaderCallbacks.class.getSimpleName() + ".origin";
    private static final String ARG_DESTINATION = DirectionsLoaderCallbacks.class.getSimpleName() + ".destination";

    //member variables
    private final Context applicationContext;
    private final WeakReference<DirectionsLoaderListener> loaderListenerWeakRef;

    //accepted loader id constants
    @IntDef({DIRECTIONS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DirectionsLoaderId {}
    public static final int DIRECTIONS = 400; //declare the actual constants

    /**
     * Helper method to initialize the loader and callbacks
     * If the loader doesn't already exist, one is created and (if the activity/fragment is currently
     * started) starts the loader.  Otherwise the last created loader with that id is re-used.
     * @param loaderId
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param origin
     * @param destination
     */
    public static void initLoader(@DirectionsLoaderId int loaderId, Context context, LoaderManager loaderManager, DirectionsLoaderListener loaderListener,
                                  LatLng origin, LatLng destination) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_ORIGIN, origin);
        args.putParcelable(ARG_DESTINATION, destination);

        //call loader manager's init loader
        loaderManager.initLoader(loaderId, args, new DirectionsLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to restart the loader with another origin and destination
     * Starts a new or restarts an existing {@link android.content.Loader} in
     * this manager, registers the callbacks to it,
     * and (if the activity/fragment is currently started) starts loading it.
     * If a loader with the same id has previously been
     * started it will automatically be destroyed when the new loader completes
     * its work. The callback will be delivered before the old loader
     * is destroyed.
     *
     * @param loaderId
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param origin
     * @param destination
     */
    public static void restartLoader(@DirectionsLoaderId int loaderId, Context context, LoaderManager loaderManager, DirectionsLoaderListener loaderListener,
                                     LatLng origin, LatLng destination) {
        Bundle args = new Bundle(2);
        args.putParcelable(ARG_ORIGIN, origin);
        args.putParcelable(ARG_DESTINATION, destination);

        //call loader manager's init loader
        loaderManager.restartLoader(loaderId, args, new DirectionsLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to destroy the loader
     * @param loaderId
     * @param loaderManager
     */
    public static void destroyLoader(@DirectionsLoaderId int loaderId, LoaderManager loaderManager) {
        loaderManager.destroyLoader(loaderId);
    }

    /**
     * Private constructor.  Use initLoader and restartLoader instead.
     * @param context
     * @param loaderListener
     */
    private DirectionsLoaderCallbacks(Context context, DirectionsLoaderListener loaderListener) {
        applicationContext = context.getApplicationContext();
        loaderListenerWeakRef = new WeakReference<>(loaderListener);
    }

    /**
     * Called by LoaderManager's initLoader method. Instantiate and return a new loader for the given id.
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<DirectionsResult> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        //read bundle args
        LatLng origin = args.getParcelable(ARG_ORIGIN);
        LatLng destination = args.getParcelable(ARG_DESTINATION);

        //create a new loader for the request
        return new DirectionsAsyncTaskLoader(applicationContext, origin, destination);
    }

    /**
     * Loader has finished. Called when a previously created loader has finished its load.
     * Notify listener
     * @param loader
     * @param directionsResult
     */
    @Override
    public void onLoadFinished(Loader<DirectionsResult> loader, DirectionsResult directionsResult) {
        Log.d(TAG, "onLoadFinished");
        DirectionsLoaderListener loaderListener = loaderListenerWeakRef.get();
        if (loaderListener != null)
            loaderListener.onLoadComplete(loader.getId(), directionsResult);
    }

    /**
     * Loader has been reset. This is called when the previously created loader is being reset,
     * thus making its data unavailable. Notify listener will a null dataset.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<DirectionsResult> loader) {
        Log.d(TAG, "onLoaderReset");
        onLoadFinished(loader, null);
    }
}
