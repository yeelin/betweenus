package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalPhotosResult;
import com.example.yeelin.projects.betweenus.data.fb.query.FbApiHelper;
import com.facebook.AccessToken;

/**
 * Created by ninjakiki on 11/13/15.
 */
public class PhotosAsyncTaskLoader extends AsyncTaskLoader<LocalPhotosResult> {
    //logcat
    private static final String TAG = PhotosAsyncTaskLoader.class.getCanonicalName();

    //member variables
    private final String id;
    private final String url;
    private final int pagingDirection;
    private final int dataSource;

    //cached result
    private LocalPhotosResult localPhotosResult;

    /**
     * Constructor
     * @param context
     * @param id
     * @param dataSource
     */
    public PhotosAsyncTaskLoader(Context context, String id, int dataSource) {
        this(context, id, null, 0, dataSource);
    }

    /**
     * Constructor
     * @param context
     * @param url
     * @param pagingDirection
     * @param dataSource
     */
    public PhotosAsyncTaskLoader(Context context, String url, int pagingDirection, int dataSource) {
        this(context, null, url, pagingDirection, dataSource);
    }

    /**
     * Private Constructor
     * @param context
     * @param id
     * @param url
     * @param pagingDirection
     * @param dataSource
     */
    private PhotosAsyncTaskLoader(Context context, String id, String url, int pagingDirection, int dataSource) {
        super(context);
        this.id = id;
        this.url = url;
        this.pagingDirection = pagingDirection;
        this.dataSource = dataSource;
    }

    /**
     * This is where the bulk of the work is done.  This method is called on a bg thread
     * and should generate a new set of data to be published by the loader.
     * @return
     */
    @Override
    public LocalPhotosResult loadInBackground() {
        switch (dataSource) {
            case LocalConstants.YELP:
                Log.d(TAG, "loadInBackground: Yelp data source has not been implemented yet");
                return null;

            case LocalConstants.FACEBOOK:
                if (id != null) {
                    Log.d(TAG, "loadInBackground: getPlacePhotos with id:" + id);
                    return FbApiHelper.getPlacePhotos(getContext(), AccessToken.getCurrentAccessToken(), id);
                }
                else {
                    Log.d(TAG, "loadInBackground: getMorePlacePhotos with url:" + url);
                    return FbApiHelper.getMorePlacePhotos(getContext(), AccessToken.getCurrentAccessToken(), url, pagingDirection);
                }

            case LocalConstants.GOOGLE:
                Log.d(TAG, "loadInBackground: Google data source has not been implemented yet");
                return null;

            default:
                Log.d(TAG, "loadInBackground: Unknown data source: " + dataSource);
                return null;
        }
    }

    /**
     * Called when there is new data to deliver to the client.  The super class will take care of
     * the delivery, the implementation here just adds a little logic. After this, onLoadFinished in
     * LoaderCallbacks is called.  This method runs on the UI thread.
     * @param localPhotosResult
     */
    @Override
    public void deliverResult(LocalPhotosResult localPhotosResult) {
        Log.d(TAG, "deliverResult:");
        //an async query came in while the loader is stopped. we don't need the result so toss it
        if (isReset()) {
            if (localPhotosResult != null) {
                Log.d(TAG, "deliverResult: isReset. Releasing local photos");
                localPhotosResult = null;
            }
            return;
        }

        //reassign the old data reference
        LocalPhotosResult oldPhotos = this.localPhotosResult;
        this.localPhotosResult = localPhotosResult;

        //if the loader is currently started, we can immediately deliver its results
        if (isStarted()) {
            Log.d(TAG, "deliverResult: isStarted. Delivering results");
            super.deliverResult(localPhotosResult);
        }

        //release old data
        //very important to check that old data != new data, otherwise we will get no results when
        //the loader reloads
        if (oldPhotos != null && oldPhotos != localPhotosResult) {
            Log.d(TAG, "deliverResult: Releasing old photos");
            if (localPhotosResult == null) {
                Log.d(TAG, "deliverResult: New photos are null");
            }
            oldPhotos = null;
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
        if (localPhotosResult != null) {
            //we currently have a result available so deliver it immediately
            Log.d(TAG, "onStartLoading: Delivering results immediately");
            deliverResult(localPhotosResult);
        }

        if (takeContentChanged() || localPhotosResult == null) {
            //data is not currently available, or the data has changed since the last time it was loaded
            //so start a load
            Log.d(TAG, "onStartLoading: Forcing a new load");
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
     * @param localPhotosResult
     */
    @Override
    public void onCanceled(LocalPhotosResult localPhotosResult) {
        if (localPhotosResult != null) {
            //release resources
            Log.d(TAG, "onCanceled: Releasing local photos");
            localPhotosResult = null;
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
        if (localPhotosResult != null) {
            Log.d(TAG, "onReset: Releasing local photos member variable");
            localPhotosResult = null;
        }
    }
}
