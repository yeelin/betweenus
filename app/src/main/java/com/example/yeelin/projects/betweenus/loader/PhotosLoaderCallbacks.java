package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalPhotosResult;
import com.example.yeelin.projects.betweenus.loader.callback.PhotosLoaderListener;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 11/13/15.
 */
public class PhotosLoaderCallbacks implements LoaderManager.LoaderCallbacks<LocalPhotosResult> {
    //logcat
    private static final String TAG = PhotosLoaderCallbacks.class.getCanonicalName();

    //bundle args
    private static final String ARG_SEARCH_ID = PhotosLoaderCallbacks.class.getSimpleName() + ".searchId";
    private static final String ARG_DATASOURCE = PhotosLoaderCallbacks.class.getSimpleName() + ".dataSource";
    private static final String ARG_PAGE_URL = PhotosLoaderCallbacks.class.getSimpleName() + ".pageUrl";
    private static final String ARG_PAGING_DIRECTION = PhotosLoaderCallbacks.class.getSimpleName() + ".pagingDirection";

    //member variables
    private final Context applicationContext;
    private final WeakReference<PhotosLoaderListener> loaderListenerWeakRef;

    /**
     * Helper method to initialize the loader and callbacks using searchId
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param searchId
     * @param dataSource
     */
    public static void initLoader(Context context, LoaderManager loaderManager, PhotosLoaderListener loaderListener,
                                  @NonNull String searchId, int dataSource) {
        Bundle args = new Bundle(2);
        args.putInt(ARG_DATASOURCE, dataSource);
        args.putString(ARG_SEARCH_ID, searchId);

        //call loader manager's init loader
        loaderManager.initLoader(LoaderId.PHOTOS.getValue(),
                args,
                new PhotosLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to restart the loader for the next page of data
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param url
     * @param pagingDirection
     * @param dataSource
     */
    public static void restartLoader(Context context, LoaderManager loaderManager, PhotosLoaderListener loaderListener,
                                     @NonNull String url, int pagingDirection, int dataSource) {
        Bundle args = new Bundle(3);
        args.putInt(ARG_DATASOURCE, dataSource);
        args.putString(ARG_PAGE_URL, url);
        args.putInt(ARG_PAGING_DIRECTION, pagingDirection);

        //call loader manager's restart loader
        loaderManager.restartLoader(LoaderId.PHOTOS.getValue(),
                args,
                new PhotosLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to destroy the loader
     * @param context
     * @param loaderManager
     */
    public static void destroyLoader(Context context, LoaderManager loaderManager) {
        loaderManager.destroyLoader(LoaderId.PHOTOS.getValue());
    }

    /**
     * Private constructor
     * @param context
     * @param loaderListener
     */
    private PhotosLoaderCallbacks(Context context, PhotosLoaderListener loaderListener) {
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
    public Loader<LocalPhotosResult> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader:");

        //read bundle args
        int dataSource = args.getInt(ARG_DATASOURCE, LocalConstants.FACEBOOK); //default is fb
        String searchId = args.getString(ARG_SEARCH_ID, null);

        if (searchId != null) {
            //create a new loader for the initial request
            return new PhotosAsyncTaskLoader(applicationContext, searchId, dataSource);
        }
        else {
            //create a loader for the subsequent request
            String url = args.getString(ARG_PAGE_URL, null);
            int pagingDirection = args.getInt(ARG_PAGING_DIRECTION, LocalConstants.NEXT_PAGE); //default it the next page of results
            return new PhotosAsyncTaskLoader(applicationContext, url, pagingDirection, dataSource);
        }
    }

    /**
     * Loader has finished. Called when a previously created loader has finished its load.
     * Notify listener
     * @param loader
     * @param localPhotosResult
     */
    @Override
    public void onLoadFinished(Loader<LocalPhotosResult> loader, LocalPhotosResult localPhotosResult) {
        Log.d(TAG, "onLoadFinished:");
        PhotosLoaderListener loaderListener = loaderListenerWeakRef.get();
        if (loaderListener != null) {
            loaderListener.onLoadComplete(LoaderId.getLoaderIdForInt(loader.getId()), localPhotosResult);
        }
    }

    /**
     * Loader has been reset. This is called when the previously created loader is being reset,
     * thus making its data unavailable. Notify listener will a null dataset.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<LocalPhotosResult> loader) {
        Log.d(TAG, "onLoaderReset:");
        onLoadFinished(loader, null);
    }
}
