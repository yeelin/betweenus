package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.google.model.DistanceMatrixResult;
import com.example.yeelin.projects.betweenus.data.google.query.GoogleDistanceMatrixLoaderHelper;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;

/**
 * Created by ninjakiki on 2/26/16.
 */
public class DistanceMatrixAsyncTaskLoader extends AsyncTaskLoader<DistanceMatrixResult> {
    //logcat
    private static final String TAG = DistanceMatrixAsyncTaskLoader.class.getCanonicalName();

    private final LatLng[] origins;
    private final LatLng[] destinations;

    //cached result
    private DistanceMatrixResult distanceMatrixResult;

    /**
     *
     * @param context
     * @param origins
     * @param destinations
     */
    public DistanceMatrixAsyncTaskLoader(Context context, LatLng[] origins, LatLng[] destinations) {
        super(context);
        this.origins = origins;
        this.destinations = destinations;
    }

    /**
     * This is where the bulk of the work is done.  This method is called on a bg thread
     * and should generate a new set of data to be published by the loader.
     * @return
     */
    @Override
    public DistanceMatrixResult loadInBackground() {
        Log.d(TAG, String.format("loadInBackground: Origins:%s, Destinations:%s", Arrays.toString(origins), Arrays.toString(destinations)));
        return GoogleDistanceMatrixLoaderHelper.getDistanceMatrix(getContext(), origins, destinations);
    }


    /**
     * Called when there is new data to deliver to the client.  The super class will take care of
     * the delivery, the implementation here just adds a little logic. After this, onLoadFinished in
     * LoaderCallbacks is called.  This method runs on the UI thread.
     * @param distanceMatrixResult
     */
    @Override
    public void deliverResult(DistanceMatrixResult distanceMatrixResult) {
        Log.d(TAG, "deliverResult");
        //an async query came in while the loader is stopped. we don't need the result so toss it
        if (isReset()) {
            if (distanceMatrixResult != null) {
                distanceMatrixResult = null;
            }
            return;
        }

        //reassign the old data reference
        DistanceMatrixResult oldDistanceMatrix = this.distanceMatrixResult;
        this.distanceMatrixResult = distanceMatrixResult;

        //if the loader is currently started, we can immediately deliver its results
        if (isStarted()) {
            Log.d(TAG, "deliverResult: isStarted. Delivering results");
            super.deliverResult(distanceMatrixResult);
        }

        //release old data
        //very important to check that old data != new data, otherwise we will get no results when
        //the loader reloads
        if (oldDistanceMatrix != null && oldDistanceMatrix != distanceMatrixResult) {
            Log.d(TAG, "deliverResult: Releasing old directions");
            if (distanceMatrixResult == null) {
                Log.d(TAG, "deliverResult: New directions are null");
            }
            oldDistanceMatrix = null;
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
        if (distanceMatrixResult != null) {
            //we currently have a result available so deliver it immediately
            deliverResult(distanceMatrixResult);
        }

        if (takeContentChanged() || distanceMatrixResult == null) {
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
     * @param distanceMatrixResult
     */
    @Override
    public void onCanceled(DistanceMatrixResult distanceMatrixResult) {
        //release resources
        if (distanceMatrixResult != null) {
            Log.d(TAG, "onCanceled: Releasing distance matrix result");
            distanceMatrixResult = null;
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
        if (distanceMatrixResult != null) distanceMatrixResult = null;
    }
}
