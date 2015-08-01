package com.example.yeelin.projects.betweenus.loader;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.model.YelpBusiness;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 7/31/15.
 */
public class SingleSuggestionLoaderCallbacks
        implements LoaderManager.LoaderCallbacks<YelpBusiness> {
    //logcat
    private static final String TAG = SingleSuggestionLoaderCallbacks.class.getCanonicalName();

    //bundle args
    public static final String ARG_SEARCH_ID = SingleSuggestionLoaderCallbacks.class.getSimpleName() + ".searchId";

    //member variables
    private Context applicationContext;
    private WeakReference<SingleSuggestionLoaderListener> loaderListenerWeakRef;

    /**
     * Listener interface. The loader's listener is usually the ui.
     */
    public interface SingleSuggestionLoaderListener {
        void onLoadComplete(LoaderId loaderId, @Nullable YelpBusiness yelpBusiness);
    }

    /**
     * Helper method to initialize the loader and callbacks
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param searchId
     */
    public static void initLoader(Context context, LoaderManager loaderManager, SingleSuggestionLoaderListener loaderListener,
                                  String searchId) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_ID, searchId);

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
                                     String searchId) {
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_ID, searchId);

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
    public Loader<YelpBusiness> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader");
        //read bundle args
        String searchId = args.getString(ARG_SEARCH_ID, "");
        return new SingleSuggestionAsyncTaskLoader(applicationContext, searchId);
    }

    /**
     * Loader has finished. Called when a previously created loader has finished its load. Notify listeners.
     * @param loader
     * @param yelpBusiness
     */
    @Override
    public void onLoadFinished(Loader<YelpBusiness> loader, YelpBusiness yelpBusiness) {
        Log.d(TAG, "onLoadFinished");
        SingleSuggestionLoaderListener loaderListener = loaderListenerWeakRef.get();
        if (loaderListener != null) {
            loaderListener.onLoadComplete(LoaderId.getLoaderIdForInt(loader.getId()), yelpBusiness);
        }
    }

    /**
     * Loader has been reset. Called when a previously created loader is being reset, thus making its data unavailable.
     * Notify listeners with a null dataset.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<YelpBusiness> loader) {
        Log.d(TAG, "onLoaderReset");
        SingleSuggestionLoaderListener loaderListener = loaderListenerWeakRef.get();
        if (loaderListener != null) {
            loaderListener.onLoadComplete(LoaderId.getLoaderIdForInt(loader.getId()), null);
        }
    }
}
