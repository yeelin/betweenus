package com.example.yeelin.projects.betweenus.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.example.yeelin.projects.betweenus.data.LocalTravelElement;
import com.example.yeelin.projects.betweenus.data.google.model.DistanceMatrixResult;
import com.example.yeelin.projects.betweenus.data.google.model.Element;
import com.example.yeelin.projects.betweenus.loader.DistanceMatrixLoaderCallbacks;
import com.example.yeelin.projects.betweenus.loader.SuggestionsLoaderCallbacks;
import com.example.yeelin.projects.betweenus.loader.callback.DistanceMatrixLoaderListener;
import com.example.yeelin.projects.betweenus.loader.callback.SuggestionsLoaderListener;
import com.facebook.AccessToken;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ninjakiki on 12/15/15.
 * This is a data fragment. It has no views. The purpose of this fragment is to retrieve and cache data for
 * the suggestions activity which in turn passes them to the list fragment and cluster map fragment.
 * This fragment is retained so that the data cache persists across configuration changes.
 */
public class SuggestionsDataFragment
        extends Fragment
        implements SuggestionsLoaderListener,
        DistanceMatrixLoaderListener {
    //logcat
    public static final String TAG = SuggestionsDataFragment.class.getCanonicalName();

    //bundle args
    private static final String ARG_SEARCH_TERM = SuggestionsDataFragment.class.getSimpleName() + ".searchTerm";
    private static final String ARG_SEARCH_RADIUS = SuggestionsDataFragment.class.getSimpleName() + ".searchRadius";
    private static final String ARG_SEARCH_LIMIT = SuggestionsDataFragment.class.getSimpleName() + ".searchLimit";
    private static final String ARG_IMAGE_SIZE = SuggestionsDataFragment.class.getSimpleName() + ".imageSize";
    private static final String ARG_DATA_SOURCE = SuggestionsDataFragment.class.getSimpleName() + ".dataSource";

    //member variables
    private String searchTerm;
    private int searchRadius;
    private int searchLimit;
    private int imageSizePx;
    private int dataSource;

    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    //results cache
    private ArrayList<LocalResult> localResultArrayList = new ArrayList<>(); //this is the array that accumulates the fetched pages
    private ArrayList<LocalTravelElement> userTravelElementArrayList = new ArrayList<>(); //this is the array that accumulates the fetched travel duration and distances for each place from the perspective of the user
    private ArrayList<LocalTravelElement> friendTravelElementArrayList = new ArrayList<>(); //this is the array that accumulates the fetched travel duration and distances for each place from the perspective of the friend

    //listener
    private SuggestionsDataListener suggestionsDataListener;

    /**
     * SuggestionsDataListener
     * Activities or fragments that are interested in data from the suggestions data fragment
     * should implement this interface
     */
    public interface SuggestionsDataListener {
        /**
         * Called when a single page is loaded
         * @param localResult
         * @param pageNumber
         */
        void onSinglePageLoad(@Nullable LocalResult localResult, int pageNumber);

        /**
         * Called when multiple pages are loaded. This is typically called when the fragment is returning all
         * the data that it cached.  Typical callback to an activity/fragment after a configuration change.
         * @param localResultArrayList
         */
        void onMultiPageLoad(@Nullable ArrayList<LocalResult> localResultArrayList);

        /**
         * Called when travel elements are loaded.  This is called when the fragment is returning the travel
         * durations and distances from user and friend.
         * @param userTravelArrayList
         * @param friendTravelArrayList
         */
        void onTravelElementLoad(@Nullable ArrayList<LocalTravelElement> userTravelArrayList, @Nullable ArrayList<LocalTravelElement> friendTravelArrayList);
    }

    /**
     * Creates a new instance of this fragment with the given args in the bundle
     * @param searchTerm
     * @param searchRadius
     * @param searchLimit
     * @param imageSizePx
     * @param dataSource
     * @return
     */
    public static SuggestionsDataFragment newInstance(String searchTerm, int searchRadius, int searchLimit,
                                                      int imageSizePx, @LocalConstants.DataSourceId int dataSource) {
        Bundle args = new Bundle(5);
        args.putString(ARG_SEARCH_TERM, searchTerm);
        args.putInt(ARG_SEARCH_RADIUS, searchRadius);
        args.putInt(ARG_SEARCH_LIMIT, searchLimit);
        args.putInt(ARG_IMAGE_SIZE, imageSizePx);
        args.putInt(ARG_DATA_SOURCE, dataSource);

        SuggestionsDataFragment suggestionsDataFragment = new SuggestionsDataFragment();
        suggestionsDataFragment.setArguments(args);
        return suggestionsDataFragment;
    }

    /**
     * Required public empty constructor
     */
    public SuggestionsDataFragment() {}

    /**
     * Make sure the parent fragment or activity implements SuggestionsDataListener
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Object objectToCast = getParentFragment() != null ? getParentFragment() : context;
        try {
            suggestionsDataListener = (SuggestionsDataListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement SuggestionsDataListener");
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
            searchTerm = args.getString(ARG_SEARCH_TERM);
            searchRadius = args.getInt(ARG_SEARCH_RADIUS);
            searchLimit = args.getInt(ARG_SEARCH_LIMIT);
            imageSizePx = args.getInt(ARG_IMAGE_SIZE);
            dataSource = args.getInt(ARG_DATA_SOURCE);
        }

        //set retain instance to true so that this doesn't get destroyed during a configuration change
        setRetainInstance(true);
    }

    /**
     * If there are currently no cached results, initiate a fetch of the first page of data right away
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //if we don't have any of the latlngs, we are not ready to fetch data
        if (userLatLng == null || friendLatLng == null || midLatLng == null) {
            Log.d(TAG, "onActivityCreated: Latlngs are null, not ready yet");
            return;
        }

        //if the local result array is empty and we have all the necessary latlngs,
        //initiate a fetch right away
        if (localResultArrayList.size() == 0) {
            fetchSuggestions(0, null);
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
     * Typically when this fragment is created, the activity would not have the latlngs yet. This method
     * allows for the activity to pass the latlngs over to the fragment and to initiate a fetch right away.
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    public void onLatLngLoad(LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        this.midLatLng = midLatLng;
    }

    /**
     * Clears out the cache and reloads the first page.
     */
    public void forceReload() {
        //clear out all caches
        localResultArrayList.clear();
        userTravelElementArrayList.clear();
        friendTravelElementArrayList.clear();

        //load the first page
        fetchSuggestions(0, null);
    }

    /**
     * Helper method that initializes the loader to fetch suggestions from either Yelp or Facebook
     * @param pageNumber this is used to index into the array list of cached results
     * @param nextUrl url for the next page, if any
     *
     */
    public void fetchSuggestions(int pageNumber, @Nullable String nextUrl) {
        //if we don't have any of the latlngs, we are not ready to fetch data
        if (userLatLng == null || friendLatLng == null || midLatLng == null) {
            Log.d(TAG, "fetchSuggestions: Latlngs are null, not ready yet");
            return;
        }

        //perform the appropriate action depending on dataSource
        switch (dataSource) {
            case LocalConstants.FACEBOOK:
                //check if user is currently logged into fb
                if (AccessToken.getCurrentAccessToken() == null) {
                    Log.d(TAG, "fetchSuggestions: User is not logged in");
                    return;
                }

                if (pageNumber == 0 && localResultArrayList.size() == 0) {
                    //initial request so fetch data using initLoader
                    Log.d(TAG, "fetchSuggestions: Calling initLoader");
                    SuggestionsLoaderCallbacks.initLoader(SuggestionsLoaderCallbacks.MULTI_PLACES_INITIAL,
                            getContext(), getLoaderManager(), this,
                            searchTerm, searchRadius, searchLimit,
                            userLatLng, friendLatLng, midLatLng,
                            imageSizePx, imageSizePx, dataSource);
                }
                else if (pageNumber >= localResultArrayList.size()) {
                    //request next page of results using restart loader
                    Log.d(TAG, "fetchSuggestions: Calling restartLoader with nextUrl");
                    SuggestionsLoaderCallbacks.restartLoader(SuggestionsLoaderCallbacks.MULTI_PLACES_SUBSEQUENT,
                            getContext(), getLoaderManager(), this,
                            nextUrl, LocalConstants.NEXT_PAGE, dataSource);
                }
                else {
                    //pageNumber < localResultArrayList.size()
                    //since the requested page number is less than the size of our current cache, we can return cached data right way
                    Log.d(TAG, String.format("fetchPlacePhotos: PageNumber:%d, Size:%d, Returning all results.", pageNumber, localResultArrayList.size()));
                    suggestionsDataListener.onMultiPageLoad(localResultArrayList);
                    //return cached travel elements
                    suggestionsDataListener.onTravelElementLoad(userTravelElementArrayList, friendTravelElementArrayList);
                }
                break;

            case LocalConstants.YELP:
                //initialize the loader to fetch suggestions from Yelp
                SuggestionsLoaderCallbacks.initLoader(SuggestionsLoaderCallbacks.MULTI_PLACES_INITIAL,
                        getContext(), getLoaderManager(), this,
                        searchTerm, searchRadius, searchLimit,
                        userLatLng, friendLatLng, midLatLng,
                        imageSizePx, imageSizePx, dataSource);
                break;

            case LocalConstants.GOOGLE:
                Log.d(TAG, "fetchSuggestions from Google has not been implemented");
                break;

            default:
                Log.d(TAG, "fetchSuggestions: unrecognized data source requested");
                break;
        }
    }

    /**
     * SuggestionsLoaderListener callback
     * This is the callback from the loader with the result
     * If the result is null, we return null to the listener
     * If the result is not null, we cache the result by adding it to the end and then return the result to the listener.
     * @param loaderId
     * @param localResult
     */
    @Override
    public void onLoadComplete(@SuggestionsLoaderCallbacks.MultiPlacesLoaderId int loaderId, @Nullable LocalResult localResult) {
        if (localResult == null) {
            suggestionsDataListener.onSinglePageLoad(null, -1);
        }
        else {
            //append it to the end of the cache
            localResultArrayList.add(localResult);
            suggestionsDataListener.onSinglePageLoad(localResult, localResultArrayList.size()-1);

            //start the loader for distance matrix (travel distance and duration)
            DistanceMatrixLoaderCallbacks.initLoader(DistanceMatrixLoaderCallbacks.DISTANCE_MATRIX,
                    getContext(),
                    getLoaderManager(),
                    this,
                    new LatLng[] {userLatLng, friendLatLng},
                    getLocalPlaceLatLngs(localResult));
        }
    }

    /**
     * Distance Matrix Loader callback
     * This is the callback from the loader with the distance matrix result
     * If the result is null, we don't do anything
     * If the result it not null, we cache the result by adding it to the end and return the result to the listener
     * @param loaderId
     * @param distanceMatrixResult
     */
    @Override
    public void onLoadComplete(@DistanceMatrixLoaderCallbacks.DistanceMatrixLoaderId int loaderId, @Nullable DistanceMatrixResult distanceMatrixResult) {
        if (distanceMatrixResult == null || distanceMatrixResult.getRows() == null) {
            Log.d(TAG, "onLoadComplete: DistanceMatrixResult is null or getRows is null");
            suggestionsDataListener.onTravelElementLoad(null, null);
        }
        else if (distanceMatrixResult.getRows().length != 2) {
            Log.d(TAG, "onLoadComplete: DistanceMatrixResult.getRows is not of length 2");
            suggestionsDataListener.onTravelElementLoad(null, null);
        }
        else {
            //append user and friend travel elements to the end of the respective cache
            DistanceMatrixResult.Row[] rows = distanceMatrixResult.getRows();
            Element[] userElements = rows[0].getElements();
            Element[] friendElements = rows[1].getElements();
            userTravelElementArrayList.addAll(Arrays.asList(userElements));
            friendTravelElementArrayList.addAll(Arrays.asList(friendElements));

            //notify the listeners that we have travel data
            suggestionsDataListener.onTravelElementLoad(userTravelElementArrayList, friendTravelElementArrayList);
        }
    }

    /**
     * Returns the whole LocalResult cache
     * @return
     */
    public ArrayList<LocalResult> getAllResults() { return localResultArrayList; }

    /**
     * Return the whole userTravel cache
     * @return
     */
    public ArrayList<LocalTravelElement> getUserTravelElements() { return userTravelElementArrayList; }

    /**
     * Return the whole friendTravel cache
     * @return
     */
    public ArrayList<LocalTravelElement> getFriendTravelElements() { return friendTravelElementArrayList; }

    /**
     *
     * @param localResult
     * @return
     */
    private LatLng[] getLocalPlaceLatLngs(@NonNull LocalResult localResult) {
        if (localResult.getLocalBusinesses() == null) return null;

        ArrayList<LocalBusiness> localBusinesses = localResult.getLocalBusinesses();
        LatLng[] latLngs = new LatLng[localBusinesses.size()];

        for (int i=0; i<localBusinesses.size(); i++) {
            latLngs[i] = localBusinesses.get(i).getLocalBusinessLocation().getLatLng();
        }
        return latLngs;
    }
}
