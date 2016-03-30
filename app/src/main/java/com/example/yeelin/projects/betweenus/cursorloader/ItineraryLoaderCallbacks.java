package com.example.yeelin.projects.betweenus.cursorloader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.projects.betweenus.cursorloader.callback.ItineraryLoaderListener;
import com.example.yeelin.projects.betweenus.provider.ItineraryContentProvider;
import com.example.yeelin.projects.betweenus.provider.ItineraryContract;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 3/29/16.
 */
public class ItineraryLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ItineraryLoaderCallbacks.class.getCanonicalName();

    //bundle args
    private static final String ARG_URI = ItineraryLoaderCallbacks.class.getSimpleName() + ".uri";
    private static final String ARG_PROJECTION = ItineraryLoaderCallbacks.class.getSimpleName() + ".projection";
    private static final String ARG_SELECTION = ItineraryLoaderCallbacks.class.getSimpleName() + ".selection";
    private static final String ARG_SELECTION_ARGS = ItineraryLoaderCallbacks.class.getSimpleName() + ".selectionArgs";

    //member variables
    private Context applicationContext;
    private WeakReference<ItineraryLoaderListener> listenerWeakReference;

    //loader id constants
    @IntDef({ITINERARY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ItineraryLoaderId {}
    public static final int ITINERARY = 600;

    /**
     * Private constructor. Use initloader instead.
     * @param context
     * @param loaderListener
     */
    private ItineraryLoaderCallbacks(Context context, ItineraryLoaderListener loaderListener) {
        applicationContext = context.getApplicationContext();
        listenerWeakReference = new WeakReference<>(loaderListener);
    }

    /**
     * Init loader to load all itineraries
     * @param loaderId
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param projection
     * @param selection
     * @param selectionArgs
     */
    public static void initLoader(@ItineraryLoaderId int loaderId, Context context, LoaderManager loaderManager, ItineraryLoaderListener loaderListener,
                                  String[] projection, String selection, String[] selectionArgs) {
        Uri uri = ItineraryContentProvider.buildUri();
        Log.d(TAG, "initLoader: Uri:" + uri.toString());
        initLoader(loaderId, context, loaderManager, loaderListener, uri, projection, selection, selectionArgs);
    }

    /**
     * Init loader to load itinerary with a specific id
     * @param loaderId
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param itineraryId
     * @param projection
     * @param selection
     * @param selectionArgs
     */
    public static void initLoader(@ItineraryLoaderId int loaderId, Context context, LoaderManager loaderManager, ItineraryLoaderListener loaderListener,
                                  long itineraryId, String[] projection, String selection, String[] selectionArgs) {
        Uri uri = ItineraryContentProvider.buildUri(itineraryId);
        Log.d(TAG, "initLoader with ItineraryId: Uri:" + uri.toString());
        initLoader(loaderId, context, loaderManager, loaderListener, uri, projection, selection, selectionArgs);
    }

    /**
     * Init loader to load itineraries with a specific closest city
     * @param loaderId
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param closestCity
     * @param projection
     * @param selection
     * @param selectionArgs
     */
    public static void initLoader(@ItineraryLoaderId int loaderId, Context context, LoaderManager loaderManager, ItineraryLoaderListener loaderListener,
                                  String closestCity, String[] projection, String selection, String[] selectionArgs) {
        Uri uri = ItineraryContentProvider.buildUri(closestCity);
        Log.d(TAG, "initLoader with ClosestCity: Uri:" + uri.toString());
        initLoader(loaderId, context, loaderManager, loaderListener, uri, projection, selection, selectionArgs);
    }

    /**
     * Private initloader that puts together everything and calls the loader manager's init loader
     * @param loaderId
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     */
    private static void initLoader(@ItineraryLoaderId int loaderId, Context context, LoaderManager loaderManager, ItineraryLoaderListener loaderListener,
                                   Uri uri, String[] projection, String selection, String[] selectionArgs) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        args.putStringArray(ARG_PROJECTION, projection);
        args.putString(ARG_SELECTION, selection);
        args.putStringArray(ARG_SELECTION_ARGS, selectionArgs);

        //call loader manager's initloader
        loaderManager.initLoader(loaderId, args, new ItineraryLoaderCallbacks(context, loaderListener));
    }

    /**
     * Restarts the loader. TODO: need to specify how to restart, with what params
     * @param loaderId
     * @param context
     * @param loaderManager
     * @param loaderListener
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     */
    public static void restartLoader(@ItineraryLoaderId int loaderId, Context context, LoaderManager loaderManager, ItineraryLoaderListener loaderListener,
                                     Uri uri, String[] projection, String selection, String[] selectionArgs) {
        //TODO
    }

    /**
     * Destroys the loader
     * @param loaderId
     * @param loaderManager
     */
    public static void destroyLoader(@ItineraryLoaderId int loaderId, LoaderManager loaderManager) {
        loaderManager.destroyLoader(loaderId);
    }

    /**
     * Creates a new loader
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //get bundle args
        Uri uri = args.getParcelable(ARG_URI);
        String[] projection = args.getStringArray(ARG_PROJECTION);
        String selection = args.getString(ARG_SELECTION);
        String[] selectionArgs = args.getStringArray(ARG_SELECTION_ARGS);

        //return a new cursor loader
        return new CursorLoader(applicationContext, uri, projection, selection, selectionArgs,
                ItineraryContract.Columns.CREATED_DATETIME + " desc, " + ItineraryContract.Columns.CLOSEST_CITY + " asc");
    }

    /**
     * Loader has finished, notify the listeners if any.
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ItineraryLoaderListener loaderListener = listenerWeakReference.get();
        if (loaderListener != null) {
            loaderListener.onLoadComplete(loader.getId(), data);
        }
    }

    /**
     * Loader has been reset, notify the listeners with a null cursor.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        onLoadFinished(loader, null);
    }
}
