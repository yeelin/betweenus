package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.loader.callback.SingleSuggestionLoaderListener;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 7/31/15.
 */
public class SingleSuggestionLoaderCallbacks
        implements LoaderManager.LoaderCallbacks<LocalBusiness> {
    //logcat
    private static final String TAG = SingleSuggestionLoaderCallbacks.class.getCanonicalName();

    //bundle args
    private static final String ARG_SEARCH_ID = SingleSuggestionLoaderCallbacks.class.getSimpleName() + ".searchId";
    private static final String ARG_DATASOURCE = SingleSuggestionLoaderCallbacks.class.getSimpleName() + ".dataSource";

    //member variables
    private Context applicationContext;
    private WeakReference<SingleSuggestionLoaderListener> loaderListenerWeakRef;

    /**
     * Helper method to initialize the loader and callbacks
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param searchId
     */
    public static void initLoader(Context context, LoaderManager loaderManager, SingleSuggestionLoaderListener loaderListener,
                                  String searchId, int dataSource) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_ID, searchId);
        args.putInt(ARG_DATASOURCE, dataSource);

        //call LoaderManager's init loader
        loaderManager.initLoader(
                LoaderId.SINGLE_PLACE.getValue(),
                args,
                new SingleSuggestionLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to restart the loader
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param searchId
     */
    public static void restartLoader(Context context, LoaderManager loaderManager, SingleSuggestionLoaderListener loaderListener,
                                     String searchId, int dataSource) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_ID, searchId);
        args.putInt(ARG_DATASOURCE, dataSource);

        loaderManager.restartLoader(
                LoaderId.SINGLE_PLACE.getValue(),
                args,
                new SingleSuggestionLoaderCallbacks(context, loaderListener));
    }

    /**
     * Helper method to destroy the loader
     * @param loaderManager
     */
    public static void destroyLoader(LoaderManager loaderManager) {
        loaderManager.destroyLoader(LoaderId.SINGLE_PLACE.getValue());
    }

    /**
     * Private constructor
     * @param context
     * @param loaderListener
     */
    private SingleSuggestionLoaderCallbacks(Context context, SingleSuggestionLoaderListener loaderListener) {
        applicationContext = context.getApplicationContext();
        loaderListenerWeakRef = new WeakReference<>(loaderListener);
    }

    /**
     *  Called by LoaderManager's initLoader method. Instantiate and return a new Loader for the given ID.
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<LocalBusiness> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        //read bundle args
        String searchId = args.getString(ARG_SEARCH_ID, "");
        int dataSource = args.getInt(ARG_DATASOURCE, 0);
        return new SingleSuggestionAsyncTaskLoader(applicationContext, searchId, dataSource);
    }

    /**
     * Loader has finished. Called when a previously created loader has finished its load. Notify listeners.
     * @param loader
     * @param localBusiness
     */
    @Override
    public void onLoadFinished(Loader<LocalBusiness> loader, LocalBusiness localBusiness) {
        Log.d(TAG, "onLoadFinished");
        SingleSuggestionLoaderListener loaderListener = loaderListenerWeakRef.get();
        if (loaderListener != null) {
            loaderListener.onLoadComplete(LoaderId.getLoaderIdForInt(loader.getId()), localBusiness);
        }
    }

    /**
     * Loader has been reset. Called when a previously created loader is being reset, thus making its data unavailable.
     * Notify listeners with a null dataset.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<LocalBusiness> loader) {
        Log.d(TAG, "onLoaderReset");
        SingleSuggestionLoaderListener loaderListener = loaderListenerWeakRef.get();
        if (loaderListener != null) {
            loaderListener.onLoadComplete(LoaderId.getLoaderIdForInt(loader.getId()), null);
        }
    }
}
