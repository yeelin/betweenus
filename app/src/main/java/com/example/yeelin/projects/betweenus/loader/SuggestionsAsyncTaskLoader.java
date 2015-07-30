package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.location.Location;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.model.YelpBusiness;

import java.util.ArrayList;

/**
 * Loads data in a bg thread. Caches the loaded data, so that it isn't reloaded on orientation change, or when
 * navigating up to parent.
 * Created by ninjakiki on 7/20/15.
 * http://chalup.github.io/blog/2014/06/12/android-loaders/
 */
public class SuggestionsAsyncTaskLoader extends AsyncTaskLoader<ArrayList<YelpBusiness>> {
    //logcat
    private static final String TAG = SuggestionsAsyncTaskLoader.class.getCanonicalName();

    //member variables
    private final String searchTerm;
    private final Location userLocation;
    private final Location friendLocation;
    private ArrayList<YelpBusiness> suggestedItems;

    /**
     * Constructor. Creates a fully specified async task loader
     * @param context
     */
    public SuggestionsAsyncTaskLoader(Context context, String searchTerm, Location userLocation, Location friendLocation) {
        super(context);

        this.searchTerm = searchTerm;
        this.userLocation = userLocation;
        this.friendLocation = friendLocation;
    }

    /**
     * This is where the bulk of the work is done. This method is called on a bg thread and
     * should generate a new set of data to be published by the loader.
     * @return
     */
    @Override
    public ArrayList<YelpBusiness> loadInBackground() {
        Log.d(TAG, "loadInBackground");
        ArrayList<YelpBusiness> suggestedItems = YelpLoaderHelper.fetchFromNetwork(getContext(), searchTerm, userLocation, friendLocation);
        return suggestedItems;
    }

    /**
     * Called when there is new data to deliver to the client. The super class will take care of
     * delivering it, the implementation here just adds a little logic.  After this, onLoadFinished
     * in LoaderCallbacks is called.
     * Runs on UI thread.
     * @param suggestedItems
     */
    @Override
    public void deliverResult(ArrayList<YelpBusiness> suggestedItems) {
        Log.d(TAG, "deliverResult");
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (suggestedItems != null) {
                releaseResources(suggestedItems);
            }
            Log.d(TAG, "deliverResult: isReset");
            return;
        }

        //reassign old data reference
        ArrayList<YelpBusiness> oldItems = this.suggestedItems;
        this.suggestedItems = suggestedItems;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            Log.d(TAG, "deliverResult: isStarted");
            super.deliverResult(suggestedItems);
        }

        //release old data
        //very important to check oldItems != suggestedItems, otherwise we will get no results when the loader reloads
        if (oldItems != null && oldItems != suggestedItems) {
            releaseResources(oldItems);
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
        if (suggestedItems != null) {
            //we currently have a result available so deliver it immediately
            Log.d(TAG, "onStartLoading: Suggested items not null, so delivering results immediately");
            deliverResult(suggestedItems);
        }

        if (takeContentChanged() || suggestedItems == null) {
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
     * @param suggestedItems
     */
    @Override
    public void onCanceled(ArrayList<YelpBusiness> suggestedItems) {
        Log.d(TAG, "onCanceled");

        if (suggestedItems != null) {
            //release resources
            releaseResources(suggestedItems);
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
        if (suggestedItems != null) {
            releaseResources(suggestedItems);
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    private void releaseResources(ArrayList<YelpBusiness> suggestedItems) {
        Log.d(TAG, "releaseResources");
        if (suggestedItems != null) {
            suggestedItems.clear();
            suggestedItems = null;
        }
    }
}
