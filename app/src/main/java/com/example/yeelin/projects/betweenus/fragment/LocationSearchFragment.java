package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.location.Location;
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
import com.example.yeelin.projects.betweenus.adapter.LocationSearchAdapter;
import com.example.yeelin.projects.betweenus.adapter.LocationSearchItem;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
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

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class LocationSearchFragment
        extends BasePlayServicesFragment
        implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener {
    //logcat
    private static final String TAG = LocationSearchFragment.class.getCanonicalName();
    //intent extras
    private static final String ARG_USER_ID = LocationSearchFragment.class.getSimpleName() + ".userId";
    //saved instance state
    private static final String STATE_LAST_QUERY = LocationSearchFragment.class.getSimpleName() + ".lastQuery";
    //other constants
    private static final int MINIMUM_QUERY_TEXT_LENGTH = 2;
    private static final int PENDING_RESULT_TIME_SECONDS = 30; //amount of time to wait for Places api = 30 seconds
    private static final LatLng US_SW = new LatLng(24.498899, -124.422985);
    private static final LatLng US_NE = new LatLng(48.902104, -67.008434);
    private static final LatLngBounds US_LAT_LNG_BOUNDS = new LatLngBounds(US_SW, US_NE); //rectangle encapsulating USA

    //member variables
    private String lastQuery = "";
    private int userId = LocationUtils.USER_LOCATION;

    //pending results from google play services Places API
    private PendingResult<AutocompletePredictionBuffer> autocompletePendingResult;

    //callbacks from google play services Places API
    private AutocompleteResultCallback autocompleteResultCallback;

    //listener
    private LocationSearchFragmentListener locationSearchListener;

    /**
     * Listener interface to be implemented by whoever is interested in events from this fragment.
     */
    public interface LocationSearchFragmentListener {
        public void onLocationSelected(String placeId, String description);
    }

    public static LocationSearchFragment newInstance(int userId) {
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);

        LocationSearchFragment fragment = new LocationSearchFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Required empty constructor
     */
    public LocationSearchFragment() {}

    /**
     * Creates a new google api client builder that does places search
     * @return
     */
    @NonNull
    @Override
    public GoogleApiClient.Builder buildGoogleApiClient() {
        return new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API);
    }

    /**
     * Make sure either the activity or the parent fragment implements the listener interface.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : activity;
        try {
            locationSearchListener = (LocationSearchFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement LocationSearchFragmentListener");
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

        //read bundle args
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getInt(ARG_USER_ID, LocationUtils.USER_LOCATION);
        }

        //restore last query if it exists
        if (savedInstanceState != null) {
            lastQuery = savedInstanceState.getString(STATE_LAST_QUERY, "");
        }

        //callbacks from AutocompleteAPI
        autocompleteResultCallback = new AutocompleteResultCallback();
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
        return inflater.inflate(R.layout.fragment_location_search, container, false);
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
        inflater.inflate(R.menu.menu_location_search, menu);

        //get the search view
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        //set the title of the menu item
        searchMenuItem.setTitle(userId == LocationUtils.USER_LOCATION ? getString(R.string.user_search_title) : getString(R.string.friend_search_title));
        //set the searchable configuration
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        //reference the dummy activity that will "handle" the search result
        ComponentName componentName = new ComponentName(getActivity(), DummySearchActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        //set the query hint
        searchView.setQueryHint(userId == LocationUtils.USER_LOCATION ? getString(R.string.user_query_hint) : getString(R.string.friend_query_hint));
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
        autocompletePendingResult = null;

        //nullify the callbacks
        autocompleteResultCallback = null;

        super.onDestroy();
    }

    /**
     * Nullify the listener
     */
    @Override
    public void onDetach() {
        locationSearchListener = null;
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
        LocationSearchItem locationSearchItem = (LocationSearchItem) parent.getItemAtPosition(position);
        Log.d(TAG, String.format("onItemClick: Position:%d, Description:%s, PlaceId:%s",
                position, locationSearchItem.getDescription(), locationSearchItem.getPlaceId()));

        locationSearchListener.onLocationSelected(locationSearchItem.getPlaceId(), locationSearchItem.getDescription());
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
            ArrayList<LocationSearchItem> locationSearchItems = buildSearchResultItems(autocompletePredictions);
            autocompletePredictions.release();

            //check if listview adapter exists
            LocationSearchAdapter locationSearchAdapter = (LocationSearchAdapter) viewHolder.searchListView.getAdapter();
            if (locationSearchAdapter == null) {
                //create and set the adapter with the search resul items
                Log.d(TAG, "AutocompleteResultCallback.onResult: Search adapter is null, so creating a new one");
                locationSearchAdapter = new LocationSearchAdapter(viewHolder.searchListView.getContext(), locationSearchItems);
                viewHolder.searchListView.setAdapter(locationSearchAdapter);
            }
            else {
                //update the adapter
                Log.d(TAG, "AutocompleteResultCallback.onResult: Search adapter is not null, so updating");
                locationSearchAdapter.updateAllItems(locationSearchItems);
            }
        }

        /**
         * Helper method that reads the search results from the predictions buffer into an arraylist of
         * search result items.
         * @param autocompletePredictions
         * @return
         */
        @NonNull
        private ArrayList<LocationSearchItem> buildSearchResultItems (@NonNull AutocompletePredictionBuffer autocompletePredictions) {
            ArrayList<LocationSearchItem> locationSearchItems = new ArrayList<>(autocompletePredictions.getCount());
            for (int i=0; i<autocompletePredictions.getCount(); i++) {
                locationSearchItems.add(new LocationSearchItem(autocompletePredictions.get(i).getDescription(), autocompletePredictions.get(i).getPlaceId()));
            }
            return locationSearchItems;
        }
    }
}
