package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalPhoto;
import com.example.yeelin.projects.betweenus.loader.callback.PhotosLoaderListener;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 11/13/15.
 */
public class PhotosLoaderCallbacks implements LoaderManager.LoaderCallbacks<LocalPhoto[]> {
    //logcat
    private static final String TAG = PhotosLoaderCallbacks.class.getCanonicalName();
    //budle args
    private static final String ARG_SEARCH_ID = PhotosLoaderCallbacks.class.getSimpleName() + ".searchId";
    private static final String ARG_DATASOURCE = PhotosLoaderCallbacks.class.getSimpleName() + ".dataSource";


    //member variables
    private final Context applicationContext;
    private final WeakReference<PhotosLoaderListener> loaderListenerWeakRef;

    /**
     * Helper method to initialize the loader and callbacks
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param searchId
     * @param dataSource
     */
    public static void initLoader(Context context, LoaderManager loaderManager, PhotosLoaderListener loaderListener,
                                  String searchId, int dataSource) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_ID, searchId);
        args.putInt(ARG_DATASOURCE, dataSource);

        //call loader manager's init loader
        loaderManager.initLoader(LoaderId.PHOTOS.getValue(),
                args,
                new PhotosLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to restart the loader with new callback
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param searchId
     * @param dataSource
     */
    public static void restartLoader(Context context, LoaderManager loaderManager, PhotosLoaderListener loaderListener,
                                     String searchId, int dataSource) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_ID, searchId);
        args.putInt(ARG_DATASOURCE, dataSource);

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
    public Loader<LocalPhoto[]> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader:");
        //read bundle args
        String searchId = args.getString(ARG_SEARCH_ID);
        int dataSource = args.getInt(ARG_DATASOURCE, LocalConstants.FACEBOOK);

        return new PhotosAsyncTaskLoader(applicationContext, searchId, dataSource);
    }

    /**
     * Loader has finished. Caled when a previously created loader has finished its load.
     * Notify listener
     * @param loader
     * @param localPhotos
     */
    @Override
    public void onLoadFinished(Loader<LocalPhoto[]> loader, LocalPhoto[] localPhotos) {
        Log.d(TAG, "onLoadFinished:");
        PhotosLoaderListener loaderListener = loaderListenerWeakRef.get();
        if (loaderListener != null) {
            loaderListener.onLoadComplete(LoaderId.getLoaderIdForInt(loader.getId()), localPhotos);
        }
    }

    /**
     * Loader has been reset. This is called when the previously created loader is being reset,
     * thus making its data unavailable. Notify listener will a null dataset.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<LocalPhoto[]> loader) {
        Log.d(TAG, "onLoaderReset:");
        onLoadFinished(loader, null);
    }
}
