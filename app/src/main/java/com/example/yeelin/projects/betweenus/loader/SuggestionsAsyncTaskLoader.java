package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.example.yeelin.projects.betweenus.data.fb.query.FbApiHelper;
import com.example.yeelin.projects.betweenus.data.yelp.query.YelpLoaderHelper;
import com.facebook.AccessToken;
import com.google.android.gms.maps.model.LatLng;

/**
 * Loads data in a bg thread. Caches the loaded data, so that it isn't reloaded on orientation change, or when
 * navigating up to parent.
 * Created by ninjakiki on 7/20/15.
 * http://chalup.github.io/blog/2014/06/12/android-loaders/
 */
public class SuggestionsAsyncTaskLoader extends AsyncTaskLoader<LocalResult> {
    //logcat
    private static final String TAG = SuggestionsAsyncTaskLoader.class.getCanonicalName();

    //member variables
    private final String searchTerm;
    private final LatLng userLatLng;
    private final LatLng friendLatLng;
    private final LatLng midLatLng;
    private final int imageHeightPx;
    private final int imageWidthPx;
    private final int dataSource;
    private LocalResult localResult;

    /**
     * Constructor. Creates a fully specified async task loader
     * @param context
     * @param searchTerm
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     * @param imageHeightPx
     * @param imageWidthPx
     * @param dataSource
     */
    public SuggestionsAsyncTaskLoader(Context context,
                                      String searchTerm, LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng,
                                      int imageHeightPx, int imageWidthPx, int dataSource) {
        super(context);
        this.searchTerm = searchTerm;
        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        this.midLatLng = midLatLng;
        this.imageHeightPx = imageHeightPx;
        this.imageWidthPx = imageWidthPx;
        this.dataSource = dataSource;
    }

    /**
     * This is where the bulk of the work is done. This method is called on a bg thread and
     * should generate a new set of data to be published by the loader.
     * @return
     */
    @Override
    public LocalResult loadInBackground() {
        if (dataSource == LocalConstants.YELP) {
            Log.d(TAG, "loadInBackground: Searching Yelp");
            return YelpLoaderHelper.fetchFromNetwork(getContext(), searchTerm, userLatLng, friendLatLng, midLatLng);
        }
        else if (dataSource == LocalConstants.FACEBOOK) {
            Log.d(TAG, "loadInBackground: Searching Facebook");
            return FbApiHelper.searchForPlaces(getContext(), AccessToken.getCurrentAccessToken(), midLatLng, imageHeightPx, imageWidthPx);
        }
        else if (dataSource == LocalConstants.GOOGLE) {
            Log.d(TAG, "loadInBackground: Google data source has not been implemented yet");
            return null;
        }
        else {
            Log.d(TAG, "loadInBackground: Unknown data source: " + dataSource);
            return null;
        }
    }

    /**
     * Called when there is new data to deliver to the client. The super class will take care of
     * delivering it, the implementation here just adds a little logic.  After this, onLoadFinished
     * in LoaderCallbacks is called.
     * Runs on UI thread.
     * @param localResult
     */
    @Override
    public void deliverResult(LocalResult localResult) {
        Log.d(TAG, "deliverResult");
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (localResult != null) {
                releaseResources(localResult);
            }
            Log.d(TAG, "deliverResult: isReset");
            return;
        }

        //reassign old data reference
        LocalResult oldResult = this.localResult;
        this.localResult = localResult;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            Log.d(TAG, "deliverResult: isStarted");
            super.deliverResult(localResult);
        }

        //release old data
        //very important to check oldItems != suggestedItems, otherwise we will get no results when the loader reloads
        if (oldResult != null && oldResult != localResult) {
            releaseResources(oldResult);
        }
    }

    /**
     * Started state
     * The loader has been created.  This method handles a request to start the loader.
     * It will either:
     * 1. return cached data (via deliverResult) OR
     * 2. start loading the data. After this, loadInBackground will be called
     */
    @Override
    protected void onStartLoading() {
        Log.d(TAG, "onStartLoading");
        if (localResult != null) {
            //we currently have a result available so deliver it immediately
            Log.d(TAG, "onStartLoading: Suggested items not null, so delivering results immediately");
            deliverResult(localResult);
        }

        if (takeContentChanged() || localResult == null) {
            //data is not currently available, or the data has changed since the last time it was loaded
            //start a load
            Log.d(TAG, "onStartLoading: Force load");
            forceLoad();
        }
    }

    /**
     * Stopped state
     * Handles a request to stop the loader
     */
    @Override
    protected void onStopLoading() {
        Log.d(TAG, "onStopLoading");
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Callback from AsyncTaskLoader
     * Handles a request to cancel a load. Called after data loading when it turns out that the data is no
     * longer needed.  For example, when the async task executing the loadInBackground is cancelled. Clean up and
     * release resources
     * @param localResult
     */
    @Override
    public void onCanceled(LocalResult localResult) {
        Log.d(TAG, "onCanceled");

        if (localResult != null) {
            //release resources
            releaseResources(localResult);
        }
    }

    /**
     * Reset state
     * The data previously loaded by the loader is no longer used.  This method handles a request
     * to completely reset the loader. Clean up and free up resources.
     */
    @Override
    protected void onReset() {
        Log.d(TAG, "onReset");
        super.onReset();

        //ensure the loader is stopped
        onStopLoading();

        //release resources
        if (localResult != null) {
            releaseResources(localResult);
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    private void releaseResources(LocalResult localResult) {
        Log.d(TAG, "releaseResources");
        if (localResult != null) {
            localResult = null;
        }
    }
}
