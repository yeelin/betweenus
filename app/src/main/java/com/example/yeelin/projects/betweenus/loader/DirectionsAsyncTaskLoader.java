package com.example.yeelin.projects.betweenus.loader;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.google.model.DirectionsResult;
import com.example.yeelin.projects.betweenus.data.google.query.GoogleDirectionsLoaderHelper;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ninjakiki on 2/22/16.
 */
public class DirectionsAsyncTaskLoader extends AsyncTaskLoader<DirectionsResult> {
    //logcat
    private static final String TAG = DirectionsAsyncTaskLoader.class.getCanonicalName();

    private final LatLng origin;
    private final LatLng destination;

    //cached result
    private DirectionsResult directionsResult;

    /**
     * Private Constructor
     * @param context
     * @param origin
     * @param destination
     */
    public DirectionsAsyncTaskLoader(Context context, LatLng origin, LatLng destination) {
        super(context);
        this.origin = origin;
        this.destination = destination;
    }

    /**
     * This is where the bulk of the work is done.  This method is called on a bg thread
     * and should generate a new set of data to be published by the loader.
     * @return
     */
    @Override
    public DirectionsResult loadInBackground() {
        Log.d(TAG, String.format("loadInBackground: Origin:%s, Dest:%s", origin, destination));
        return GoogleDirectionsLoaderHelper.getDirections(getContext(), origin, destination);
    }

    /**
     * Called when there is new data to deliver to the client.  The super class will take care of
     * the delivery, the implementation here just adds a little logic. After this, onLoadFinished in
     * LoaderCallbacks is called.  This method runs on the UI thread.
     * @param directionsResult
     */
    @Override
    public void deliverResult(DirectionsResult directionsResult) {
        Log.d(TAG, "deliverResult");
        //an async query came in while the loader is stopped. we don't need the result so toss it
        if (isReset()) {
            if (directionsResult != null) {
                directionsResult = null;
            }
            return;
        }

        //reassign the old data reference
        DirectionsResult oldDirections = this.directionsResult;
        this.directionsResult = directionsResult;

        //if the loader is currently started, we can immediately deliver its results
        if (isStarted()) {
            Log.d(TAG, "deliverResult: isStarted. Delivering results");
            super.deliverResult(directionsResult);
        }

        //release old data
        //very important to check that old data != new data, otherwise we will get no results when
        //the loader reloads
        if (oldDirections != null && oldDirections != directionsResult) {
            Log.d(TAG, "deliverResult: Releasing old directions");
            if (directionsResult == null) {
                Log.d(TAG, "deliverResult: New directions are null");
            }
            oldDirections = null;
        }
    }

    /**
     * Started state
     * The loader has been created. This method handles a request to start the loader.
     * It will either:
     * 1. return cached data (via deliverResult) OR
     * 2: start loading the data in which case loadInBackground will be called
     */
    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading:");
        if (directionsResult != null) {
            //we currently have a result available so deliver it immediately
            deliverResult(directionsResult);
        }

        if (takeContentChanged() || directionsResult == null) {
            //data is not currently available, or the data has changed since the last time it was loaded
            //so start a load
            forceLoad();
        }
    }

    /**
     * Stopped state
     * Handles a request to stop the loader
     */
    @Override
    protected void onStopLoading() {
        //attempt to cancel the current load task if possible
        Log.d(TAG, "onStopLoading:");
        cancelLoad();
    }

    /**
     * Callback from AsyncTaskLoader
     * Handles a request to cancel a load.  This is called after data loading when it turns out that the
     * data is no longer needed.  For example, when the async task executing the loadInBackground is
     * cancelled. Clean up and release resources
     * @param directionsResult
     */
    @Override
    public void onCanceled(DirectionsResult directionsResult) {
        //release resources
        if (directionsResult != null) {
            Log.d(TAG, "onCanceled: Releasing direction result");
            directionsResult = null;
        }
    }

    /**
     * Reset state
     * The data previously loaded by the loader is no longer used. This method handles a request
     * to completely reset the loader. Clean up and free up resources
     */
    @Override
    protected void onReset() {
        Log.d(TAG, "onReset:");
        super.onReset();

        //ensure the loader is stopped
        onStopLoading();

        //release resources
        if (directionsResult != null) directionsResult = null;
    }
}
