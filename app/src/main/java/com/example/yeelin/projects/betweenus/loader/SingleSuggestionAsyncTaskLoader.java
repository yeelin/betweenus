package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.model.YelpBusiness;

/**
 * Created by ninjakiki on 7/31/15.
 */
public class SingleSuggestionAsyncTaskLoader extends AsyncTaskLoader<YelpBusiness> {
    //logcat
    private static final String TAG = SingleSuggestionAsyncTaskLoader.class.getCanonicalName();

    //member variables
    private final String id;
    private YelpBusiness yelpBusiness;

    /**
     * Constructor. Creates a fully specified async task loader
     * @param context
     * @param id
     */
    public SingleSuggestionAsyncTaskLoader(Context context, String id) {
        super(context);
        this.id = id;
    }

    /**
     * This is where the bulk of the work is done. This method is called on a bg thread and
     * should generate a new set of data to be published by the loader.
     * @return
     */
    @Override
    public YelpBusiness loadInBackground() {
        Log.d(TAG, "loadInBackground");
        YelpBusiness yelpBusiness = SingleSuggestionLoaderHelper.fetchFromNetwork(getContext(), id);
        return yelpBusiness;
    }

    /**
     * Called when there is new data to deliver to the client. The super class will take care of
     * delivering it, the implementation here just adds a little logic.  After this, onLoadFinished
     * in LoaderCallbacks is called.
     * Runs on UI thread.
     * @param yelpBusiness
     */
    @Override
    public void deliverResult(YelpBusiness yelpBusiness) {
        Log.d(TAG, "deliverResult");
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (yelpBusiness != null) {
                releaseResources(yelpBusiness);
            }
            Log.d(TAG, "deliverResult: isReset");
            return;
        }

        //reassign old data reference
        YelpBusiness oldBusiness = this.yelpBusiness;
        this.yelpBusiness = yelpBusiness;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            Log.d(TAG, "deliverResult: isStarted");
            super.deliverResult(yelpBusiness);
        }

        //release old data
        //very important to check oldItems != suggestedItems, otherwise we will get no results when the loader reloads
        if (oldBusiness != null && oldBusiness != yelpBusiness) {
            releaseResources(oldBusiness);
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
        if (yelpBusiness != null) {
            //we currently have a result available so deliver it immediately
            Log.d(TAG, "onStartLoading: Suggested items not null, so delivering results immediately");
            deliverResult(yelpBusiness);
        }

        if (takeContentChanged() || yelpBusiness == null) {
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
     * @param yelpBusiness
     */
    @Override
    public void onCanceled(YelpBusiness yelpBusiness) {
        Log.d(TAG, "onCanceled");

        if (yelpBusiness != null) {
            //release resources
            releaseResources(yelpBusiness);
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
        if (yelpBusiness != null) {
            releaseResources(yelpBusiness);
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    private void releaseResources(YelpBusiness yelpBusiness) {
        Log.d(TAG, "releaseResources");
        if (yelpBusiness != null) {
            yelpBusiness = null;
        }
    }
}
