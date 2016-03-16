package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.google.model.DistanceMatrixResult;
import com.example.yeelin.projects.betweenus.loader.callback.DistanceMatrixLoaderListener;
import com.google.android.gms.maps.model.LatLng;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 2/26/16.
 */
public class DistanceMatrixLoaderCallbacks implements LoaderManager.LoaderCallbacks<DistanceMatrixResult> {
    //logcat
    private static final String TAG = DistanceMatrixLoaderCallbacks.class.getCanonicalName();

    //bundle args
    private static final String ARG_ORIGINS = DistanceMatrixLoaderCallbacks.class.getSimpleName() + ".origins";
    private static final String ARG_DESTINATIONS = DistanceMatrixLoaderCallbacks.class.getSimpleName() + ".destinations";

    //member variables
    private final Context applicationContext;
    private final WeakReference<DistanceMatrixLoaderListener> loaderListenerWeakRef;

    //accepted loader id constants
    @IntDef({DISTANCE_MATRIX})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DistanceMatrixLoaderId {}
    public static final int DISTANCE_MATRIX = 500;

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
     * @param origins
     * @param destinations
     */
    public static void initLoader(@DistanceMatrixLoaderId int loaderId, Context context,
                                  LoaderManager loaderManager, DistanceMatrixLoaderListener loaderListener,
                                  LatLng[] origins, LatLng[] destinations) {
        Bundle args = new Bundle(2);
        args.putParcelableArray(ARG_ORIGINS, origins);
        args.putParcelableArray(ARG_DESTINATIONS, destinations);

        //call loader manager's init loader
        loaderManager.initLoader(loaderId, args, new DistanceMatrixLoaderCallbacks(context, loaderListener));
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
     * @param origins
     * @param destinations
     */
    public static void restartLoader(@DistanceMatrixLoaderId int loaderId, Context context,
                                     LoaderManager loaderManager, DistanceMatrixLoaderListener loaderListener,
                                     LatLng[] origins, LatLng[] destinations) {
        Bundle args = new Bundle(2);
        args.putParcelableArray(ARG_ORIGINS, origins);
        args.putParcelableArray(ARG_DESTINATIONS, destinations);

        //call loader manager's restart loader
        loaderManager.restartLoader(loaderId, args, new DistanceMatrixLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to destroy the loader
     * @param loaderId
     * @param loaderManager
     */
    public static void destroyLoader(@DistanceMatrixLoaderId int loaderId, LoaderManager loaderManager) {
        loaderManager.destroyLoader(loaderId);
    }

    /**
     * Private constructor
     * @param context
     * @param listener
     */
    private DistanceMatrixLoaderCallbacks(Context context, DistanceMatrixLoaderListener listener) {
        applicationContext = context.getApplicationContext();
        loaderListenerWeakRef = new WeakReference<>(listener);
    }

    /**
     * Called by LoaderManager's initLoader method. Instantiate and return a new loader for the given id.
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<DistanceMatrixResult> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");

        //read bundle args
        LatLng[] origins = (LatLng[]) args.getParcelableArray(ARG_ORIGINS);
        LatLng[] destinations = (LatLng[]) args.getParcelableArray(ARG_DESTINATIONS);

        //return a new loader for the request
        return new DistanceMatrixAsyncTaskLoader(applicationContext, origins, destinations);
    }

    /**
     * Loader has finished. Called when a previously created loader has finished its load.
     * Notify listener
     * @param loader
     * @param distanceMatrixResult
     */
    @Override
    public void onLoadFinished(Loader<DistanceMatrixResult> loader, DistanceMatrixResult distanceMatrixResult) {
        Log.d(TAG, "onLoadFinished");
        DistanceMatrixLoaderListener loaderListener = loaderListenerWeakRef.get();
        if (loaderListener != null)
            loaderListener.onLoadComplete(loader.getId(), distanceMatrixResult);
    }

    /**
     * Loader has been reset. This is called when the previously created loader is being reset,
     * thus making its data unavailable. Notify listener will a null dataset.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<DistanceMatrixResult> loader) {
        Log.d(TAG, "onLoaderReset");
        onLoadFinished(loader, null);
    }
}
