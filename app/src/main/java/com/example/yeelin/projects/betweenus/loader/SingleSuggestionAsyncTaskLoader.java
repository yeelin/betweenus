package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.fb.query.FbApiHelper;
import com.example.yeelin.projects.betweenus.data.yelp.query.YelpLoaderHelper;
import com.facebook.AccessToken;

/**
 * Created by ninjakiki on 7/31/15.
 */
public class SingleSuggestionAsyncTaskLoader extends AsyncTaskLoader<LocalBusiness> {
    //logcat
    private static final String TAG = SingleSuggestionAsyncTaskLoader.class.getCanonicalName();

    //member variables
    private final String id;
    private final int imageHeightPx;
    private final int imageWidthPx;
    private final int dataSource;
    private LocalBusiness localBusiness;

    /**
     * Constructor. Creates a fully specified async task loader
     * @param context
     * @param id
     */
    public SingleSuggestionAsyncTaskLoader(Context context, String id, int imageHeightPx, int imageWidthPx, int dataSource) {
        super(context);
        this.id = id;
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
    public LocalBusiness loadInBackground() {
        switch (dataSource) {
            case LocalConstants.YELP:
                Log.d(TAG, "loadInBackground: Loading from Yelp. Id:" + id);
                return YelpLoaderHelper.fetchPlaceDetails(getContext(), id);

            case LocalConstants.FACEBOOK:
                Log.d(TAG, "loadInBackground: Loading from Facebook. Id:" + id);
                return FbApiHelper.getPlaceDetails(getContext(), AccessToken.getCurrentAccessToken(), id, imageHeightPx, imageWidthPx);

            case LocalConstants.GOOGLE:
                Log.d(TAG, "loadInBackground: Google data source has not been implemented yet");
                return null;

            default:
                Log.d(TAG, "loadInBackground: Unknown data source: " + dataSource);
                return null;
        }
    }

    /**
     * Called when there is new data to deliver to the client. The super class will take care of
     * delivering it, the implementation here just adds a little logic.  After this, onLoadFinished
     * in LoaderCallbacks is called.
     * Runs on UI thread.
     * @param localBusiness
     */
    @Override
    public void deliverResult(LocalBusiness localBusiness) {
        Log.d(TAG, "deliverResult");
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (localBusiness != null) {
                localBusiness = null;
            }
            Log.d(TAG, "deliverResult: isReset");
            return;
        }

        //reassign old data reference
        LocalBusiness oldBusiness = this.localBusiness;
        this.localBusiness = localBusiness;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            Log.d(TAG, "deliverResult: isStarted");
            super.deliverResult(localBusiness);
        }

        //release old data
        //very important to check oldItems != suggestedItems, otherwise we will get no results when the loader reloads
        if (oldBusiness != null && oldBusiness != localBusiness) {
            oldBusiness = null;
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
        if (localBusiness != null) {
            //we currently have a result available so deliver it immediately
            Log.d(TAG, "onStartLoading: Suggested items not null, so delivering results immediately");
            deliverResult(localBusiness);
        }

        if (takeContentChanged() || localBusiness == null) {
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
     * @param localBusiness
     */
    @Override
    public void onCanceled(LocalBusiness localBusiness) {
        Log.d(TAG, "onCanceled");

        if (localBusiness != null) {
            //release resources
            localBusiness = null;
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
        if (localBusiness != null) {
            localBusiness = null;
        }
    }
}
