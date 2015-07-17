package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.activity.DummySearchActivity;
import com.example.yeelin.projects.betweenus.activity.PlaceActivity;
import com.example.yeelin.projects.betweenus.adapter.SearchAdapter;
import com.example.yeelin.projects.betweenus.adapter.SearchResultItem;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class SearchFragment
        extends BasePlayServicesFragment
        implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener {
    //logcat
    private static final String TAG = SearchFragment.class.getCanonicalName();
    //saved instance state
    private static final String STATE_LAST_QUERY = SearchFragment.class.getSimpleName() + ".lastQuery";
    //other constants
    private static final int MINIMUM_QUERY_TEXT_LENGTH = 2;
    private static final int PENDING_RESULT_TIME_SECONDS = 30; //amount of time to wait for Places api = 30 seconds
    private static final LatLng US_SW = new LatLng(24.498899, -124.422985);
    private static final LatLng US_NE = new LatLng(48.902104, -67.008434);
    private static final LatLngBounds US_LAT_LNG_BOUNDS = new LatLngBounds(US_SW, US_NE); //rectangle encapsulating USA

    //member variables
    private String lastQuery = "";
    //pending results from google play services Places API
    private PendingResult<AutocompletePredictionBuffer> autocompletePendingResult;
    private PendingResult<PlaceBuffer> placePendingResult;

    //callbacks from google play services Places API
    private AutocompleteResultCallback autocompleteResultCallback;
    private PlaceResultCallback placeResultCallback;

    //listener
    private SearchFragmentListener searchListener;

    /**
     * Listener interface to be implemented by whoever is interested in events from this fragment.
     */
    public interface SearchFragmentListener {
        public void onPlaceSelected(String name, double latitude, double longitude, List<Integer> placeTypes);

    }

    /**
     * Required empty constructor
     */
    public SearchFragment() {}

    /**
     * Creates a new google api client builder that does places search
     * @return
     */
    @NonNull
    @Override
    public GoogleApiClient.Builder buildGoogleApiClient() {
        return new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API);
    }

    /**
     * Make sure either the activity or the parent fragment implements the listener interface.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            Object objectToCast = getParentFragment() != null ? getParentFragment() : activity;
            searchListener = (SearchFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.getClass().getSimpleName() + " must implement SearchFragmentListener");
        }
    }

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //notify that we have an options menu so that we get the callback to create one later
        setHasOptionsMenu(true);

        //restore last query if it exists
        if (savedInstanceState != null) {
            lastQuery = savedInstanceState.getString(STATE_LAST_QUERY, "");
        }

        //callbacks from Autocomplete and Places API
        autocompleteResultCallback = new AutocompleteResultCallback();
        placeResultCallback = new PlaceResultCallback();
    }

    /**
     * Inflate the view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    /**
     * Configure the view
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set the view holder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        viewHolder.searchListView.setOnItemClickListener(this);
    }

    /**
     * Configure the search view
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //inflate the menu from xml
        inflater.inflate(R.menu.menu_search, menu);

        //get the search view and set the searchable configuration
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        //reference the dummy activity that will "handle" the search result
        ComponentName componentName = new ComponentName(getActivity(), DummySearchActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        //set the query hint
        searchView.setQueryHint(getString(R.string.search_query_hint));
        //expand the search view to save user yet another tap
        searchMenuItem.expandActionView();
        //set the query - false means only update the contents of the text field, true submits an intent and results in DummyActivity being instantiated
        searchView.setQuery(lastQuery, false);
        //listen for user actions within the search view
        searchView.setOnQueryTextListener(this);
    }

    /**
     * Save the last query to saved instance state so that it can be restored later
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_LAST_QUERY, lastQuery);
    }

    /**
     * Clean up by nullify some expensive things
     */
    @Override
    public void onDestroy() {
        //cancel and nullify the pending results
        if (autocompletePendingResult != null) autocompletePendingResult.cancel();
        if (placePendingResult != null) placePendingResult.cancel();
        autocompletePendingResult = null;
        placePendingResult = null;

        //nullify the callbacks
        autocompleteResultCallback = null;
        placeResultCallback = null;

        super.onDestroy();
    }

    /**
     * Nullify the listener
     */
    @Override
    public void onDetach() {
        searchListener = null;
        super.onDetach();
    }

    /**
     * GoogleApiClient.ConnectionCallbacks implementation
     * This callback happens when we are connected to Google Play Services.
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        //if we came from a configuration change, then lastQuery is probably not ""
        if (lastQuery.length() > 0) {
            Log.d(TAG, "onConnected: Last query:" + lastQuery);
            //get the results for the last query to repopulate the listview
            onQueryTextChange(lastQuery);
        }
    }

    /**
     * SearchView.OnQueryTextListener implementation
     * Returns true since we are handling the action and don't want the SearchView to perform
     * the default action of launching the associated intent
     * @param query
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        //do nothing
        return true;
    }

    /**
     * SearchView.OnQueryTextListener implementation
     * Returns true since we are handling the action and don't want the SearchView to perform
     * the default action of showing suggestions.
     * @param newText
     * @return
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(TAG, "onQueryTextChange: Query:" + newText);
        lastQuery = newText;

        //do nothing if we are not connected, or if we are under the minimum query length
        if (!googleApiClient.isConnected() || newText.length() < MINIMUM_QUERY_TEXT_LENGTH) {
            Log.d(TAG, "onQueryTextChange: Not connected to Google Play Services or under minimum query length");
            return true;
        }

        //handle the query
        //clear out any old pending results first
        if (autocompletePendingResult != null) {
            autocompletePendingResult.cancel();
            autocompletePendingResult = null;
        }

        //send the query to the autocomplete places API
        autocompletePendingResult = Places.GeoDataApi.getAutocompletePredictions(
                googleApiClient,
                newText, //user query
                US_LAT_LNG_BOUNDS, //restrict results to a bounding rectangle that encapsulates the US
                null); //no autocomplete filter
        //set the callback
        autocompletePendingResult.setResultCallback(autocompleteResultCallback, PENDING_RESULT_TIME_SECONDS, TimeUnit.SECONDS);
        return true;
    }

    /**
     * AdapterView.OnItemClickListener implementation
     * Handles user click on a search result
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SearchResultItem searchResultItem = (SearchResultItem) parent.getItemAtPosition(position);
        Log.d(TAG, String.format("onItemClick: Position:%d, Description:%s, PlaceId:%s",
                position, searchResultItem.getDescription(), searchResultItem.getPlaceId()));

        //clear out any old pending results first
        if (placePendingResult != null) {
            placePendingResult.cancel();
            placePendingResult = null;
        }

        //send the placeId to the getPlaceById API to get the place's name and lat/lng so that we can query Yelp
        placePendingResult = Places.GeoDataApi.getPlaceById(
                googleApiClient,
                searchResultItem.getPlaceId());
        //set the callback
        placePendingResult.setResultCallback(placeResultCallback, PENDING_RESULT_TIME_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Returns the view holder for the fragment's view if one exists.
     * @return
     */
    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /**
     * View holder class
     */
    private class ViewHolder {
        final ListView searchListView;
        final ImageView searchAttribution;

        ViewHolder(View view) {
            searchListView = (ListView) view.findViewById(R.id.search_listview);
            searchListView.setEmptyView(view.findViewById(R.id.search_empty));
            searchAttribution = (ImageView) view.findViewById(R.id.search_attribution);
        }
    }

    /**
     * Class for handling the callback from the places autocomplete API
     */
    private class AutocompleteResultCallback implements ResultCallback<AutocompletePredictionBuffer> {
        /**
         * ResultCallback<AutocompletePredictionBuffer> required override
         * Read the predictions buffer and display it
         * @param autocompletePredictions
         */
        @Override
        public void onResult(AutocompletePredictionBuffer autocompletePredictions) {
            autocompletePendingResult = null;

            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                //failed to get autocomplete results
                autocompletePredictions.release();

                //notify the user and log error
                String errorMessage = "Error contacting Google Places Autocomplete API. Status: " + status.toString();
                Log.e(TAG, "AutocompleteResultCallback.onResult: " + errorMessage);
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                return;
            }

            //we have autocomplete results
            //check if we still have a view
            ViewHolder viewHolder = getViewHolder();
            if (viewHolder == null) {
                //oops, too late, view is gone
                autocompletePredictions.release();
                Log.d(TAG, "AutocompleteResultCallback.onResult: View is null so nothing to do");
                return;
            }

            //good, we still have a view
            //read the search results from the buffer and then release the buffer
            ArrayList<SearchResultItem> searchResultItems = buildSearchResultItems(autocompletePredictions);
            autocompletePredictions.release();

            //check if listview adapter exists
            SearchAdapter searchAdapter = (SearchAdapter) viewHolder.searchListView.getAdapter();
            if (searchAdapter == null) {
                //create and set the adapter with the search resul items
                Log.d(TAG, "AutocompleteResultCallback.onResult: Search adapter is null, so creating a new one");
                searchAdapter = new SearchAdapter(viewHolder.searchListView.getContext(), searchResultItems);
                viewHolder.searchListView.setAdapter(searchAdapter);
            }
            else {
                //update the adapter
                Log.d(TAG, "AutocompleteResultCallback.onResult: Search adapter is not null, so updating");
                searchAdapter.updateAllItems(searchResultItems);
            }
        }

        /**
         * Helper method that reads the search results from the predictions buffer into an arraylist of
         * search result items.
         * @param autocompletePredictions
         * @return
         */
        @NonNull
        private ArrayList<SearchResultItem> buildSearchResultItems (@NonNull AutocompletePredictionBuffer autocompletePredictions) {
            ArrayList<SearchResultItem> searchResultItems = new ArrayList<>(autocompletePredictions.getCount());
            for (int i=0; i<autocompletePredictions.getCount(); i++) {
                searchResultItems.add(new SearchResultItem(autocompletePredictions.get(i).getDescription(), autocompletePredictions.get(i).getPlaceId()));
            }
            return searchResultItems;
        }
    }

    /**
     * Class for handling the callback from the getPlaceById places API
     */
    private class PlaceResultCallback implements ResultCallback<PlaceBuffer> {
        /**
         * ResultCallback<AutocompletePredictionBuffer> required override
         *
         * @param places
         */
        @Override
        public void onResult(PlaceBuffer places) {
            placePendingResult = null;

            final Status status = places.getStatus();
            if (!status.isSuccess()) {
                //failed to get results
                places.release();
                //notify the user and log error
                String errorMessage = "Error contacting Google Places getPlaceById API. Status: " + status.toString();
                Log.e(TAG, "PlaceResultCallback.onResult: " + errorMessage);
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                return;
            }

            //found a place matching the placeId
            final Place foundPlace = places.get(0);
            Log.d(TAG, String.format("PlaceResultCallback.onResult: Found place. Name:%s LatLng:%f, %f PlaceTypes:%s",
                    foundPlace.getName().toString(), foundPlace.getLatLng().latitude, foundPlace.getLatLng().longitude, foundPlace.getPlaceTypes().toString()));

            //notify the listener
            searchListener.onPlaceSelected(
                    foundPlace.getName().toString(),
                    foundPlace.getLatLng().latitude,
                    foundPlace.getLatLng().longitude,
                    foundPlace.getPlaceTypes());

            //release the places buffer, foundPlace should not be used after this call
            places.release();
        }
    }

}
