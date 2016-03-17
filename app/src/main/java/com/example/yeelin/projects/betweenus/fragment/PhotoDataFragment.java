package com.example.yeelin.projects.betweenus.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalPhotosResult;
import com.example.yeelin.projects.betweenus.loader.PhotosLoaderCallbacks;
import com.example.yeelin.projects.betweenus.loader.callback.PhotosLoaderListener;
import com.facebook.AccessToken;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 12/7/15.
 * This is a data fragment. It has no views.  The purpose of this fragment is to retrieve data for
 * the photo pager activity.
 */
public class PhotoDataFragment
        extends Fragment
        implements PhotosLoaderListener {
    //logcat
    public static final String TAG = PhotoDataFragment.class.getCanonicalName();

    //bundle args
    private static final String ARG_ID = PhotoDataFragment.class.getSimpleName() + ".id";
    private static final String ARG_DATA_SOURCE = PhotoDataFragment.class.getSimpleName() + ".dataSource";

    //member variables
    private String id;
    private int preferredDataSource;
    private ArrayList<LocalPhotosResult> localResultArrayList = new ArrayList<>(); //this is the array that accumulates the fetched pages

    private PhotoDataListener photoDataListener;

    /**
     * PhotoDataListener interface
     * Activities or fragments that are interested in data from the photo data fragment should
     * implement this interface
     */
    public interface PhotoDataListener {
        /**
         * Called when a single page is loaded.
         * @param localPhotosResult
         * @param pageNumber page number of the page loaded
         */
        void onSinglePageLoad(@Nullable LocalPhotosResult localPhotosResult, int pageNumber);

        /**
         * Called when multiple pages are loaded. This is typically called when the fragment is returning all
         * the data that it cached.  Typical callback to an activity/fragment after a configuration change.
         * @param localPhotosResultArrayList
         */
        void onMultiPageLoad(@Nullable ArrayList<LocalPhotosResult> localPhotosResultArrayList);
    }

    /**
     * Creates a new instance of this fragment with the given arguments in a bundle.
     * @param searchId
     * @param dataSource
     * @return
     */
    public static PhotoDataFragment newInstance(String searchId, int dataSource) {
        Bundle args = new Bundle(2);
        args.putString(ARG_ID, searchId);
        args.putInt(ARG_DATA_SOURCE, dataSource);

        PhotoDataFragment photoDataFragment = new PhotoDataFragment();
        photoDataFragment.setArguments(args);
        return photoDataFragment;
    }

    /**
     * Required public empty constructor
     */
    public PhotoDataFragment() {}

    /**
     * Make sure the parent fragment or activity implements PhotoDataListener
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Object objectToCast = getParentFragment() != null ? getParentFragment() : context;
        try {
            photoDataListener = (PhotoDataListener) objectToCast;
        } catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement PhotoDataListener");
        }
    }

    /**
     * Creates the fragment and sets its retainInstance to true so that it doesn't get destroyed during
     * a configuration change.  It will, however, be destroyed when the activity it is attached to, is finished.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //read args
        Bundle args = getArguments();
        if (args != null) {
            id = args.getString(ARG_ID);
            preferredDataSource = args.getInt(ARG_DATA_SOURCE, LocalConstants.YELP); //better use Yelp as default since we do not load additional photos if it's Yelp
        }

        //set retain instance to true so that this doesn't get destroyed during a configuration change
        setRetainInstance(true);
    }

    /**
     * If there are currently no cached results, initiate a fetch of the first page of data right away.
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //if the local result array is empty, initiate a fetch right away
        if (localResultArrayList.size() == 0) {
            fetchPlacePhotos(0, null);
        }
    }

    /**
     * Logging to make sure this fragment is destroyed when the attached activity is finished.
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Destroying retained fragment");
        super.onDestroy();
    }

    /**
     * Fetch photo urls for this place/detail page using a loader
     * @param pageNumber this is used to index into the array list of cached results
     * @param nextUrl url for the next page, if any
     */
    public void fetchPlacePhotos(int pageNumber, @Nullable String nextUrl) {
        //if preferred data source is fb but user is not logged in, return immediately
        if (preferredDataSource == LocalConstants.FACEBOOK && AccessToken.getCurrentAccessToken() == null) {
            Log.d(TAG, "fetchPlacePhotos: Preferred data source is FB but user is not logged in, so nothing to do");
            return;
        }

        if (pageNumber == 0 && localResultArrayList.size() == 0) {
            //fetch data using initLoader
            Log.d(TAG, "fetchPlacePhotos: Calling initLoader.");
            PhotosLoaderCallbacks.initLoader(PhotosLoaderCallbacks.PHOTOS_INITIAL,
                    getContext(), getLoaderManager(), this, id, preferredDataSource);
        }
        else if (pageNumber >= localResultArrayList.size()) {
            //fetch data using restart loader
            Log.d(TAG, String.format("fetchPlacePhotos: PageNumber:%d, Size:%d, Calling restartLoader.", pageNumber, localResultArrayList.size()));
            PhotosLoaderCallbacks.restartLoader(PhotosLoaderCallbacks.PHOTOS_SUBSEQUENT,
                    getContext(), getLoaderManager(), this, nextUrl, LocalConstants.NEXT_PAGE, preferredDataSource);
        }
        else {
            //pageNumber < localResultArrayList.size()
            //since the requested page number is less than the size of our current cache, we can return cached data right way
            Log.d(TAG, String.format("fetchPlacePhotos: PageNumber:%d, Size:%d, Returning all results.", pageNumber, localResultArrayList.size()));
            photoDataListener.onMultiPageLoad(localResultArrayList);
        }
    }

    /**
     * PhotosLoaderListener callback
     * This is the callback from the loader with the result.
     * If the result is null, we return null to the listener.
     * If the result is not null, we cached the result by adding it to the end, and then return the result to the listener.
     *
     * @param loaderId
     * @param localPhotosResult
     */
    @Override
    public void onLoadComplete(@PhotosLoaderCallbacks.PhotosLoaderId int loaderId, @Nullable LocalPhotosResult localPhotosResult) {
        if (localPhotosResult == null) {
            photoDataListener.onSinglePageLoad(null, -1);
        }
        else {
            //append it to the end of the cache
            localResultArrayList.add(localPhotosResult);
            photoDataListener.onSinglePageLoad(localPhotosResult, localResultArrayList.size()-1);
        }
    }
}
