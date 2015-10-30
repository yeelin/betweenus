package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.activity.DummySearchActivity;
import com.example.yeelin.projects.betweenus.adapter.LocationSearchAdapter;
import com.example.yeelin.projects.betweenus.data.generic.model.LocationSearchItem;
import com.example.yeelin.projects.betweenus.service.PlacesService;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class LocationSearchFragment
        extends Fragment
        implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener,
        PlacesService.AutocompleteCallback {
    //logcat
    private static final String TAG = LocationSearchFragment.class.getCanonicalName();
    //intent extras
    private static final String ARG_USER_ID = LocationSearchFragment.class.getSimpleName() + ".userId";
    //saved instance state
    private static final String STATE_LAST_QUERY = LocationSearchFragment.class.getSimpleName() + ".lastQuery";
    //other constants
    private static final int MINIMUM_QUERY_TEXT_LENGTH = 2;

    //member variables
    private String lastQuery = "";
    private int userId = LocationUtils.USER_LOCATION;

    //listener
    private LocationSearchFragmentListener locationSearchListener;

    //service-related
    private PlacesService.PlacesServiceBinder binder;
    private boolean bound = false;
    private ServiceConnection serviceConnection;

    /**
     * Listener interface to be implemented by whoever is interested in events from this fragment.
     */
    public interface LocationSearchFragmentListener {
        public void onLocationSelected(String placeId, String description);
    }

    /**
     * Creates an instance of this fragment. Use this instead of calling the constructor directly.
     * @param userId
     * @return
     */
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
     * Make sure either the activity or the parent fragment implements the listener interface.
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : context;
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

        //initialize the service connection object to be used in service binding later
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: Bound");
                binder = (PlacesService.PlacesServiceBinder) iBinder;
                bound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected:");
                bound = false;
            }
        };
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
     * Bind to places service for autocomplete
     */
    @Override
    public void onStart() {
        super.onStart();

        //bind to the places service, we'll get a callback on the service connection later
        Log.d(TAG, "onStart: Binding to service");
        getActivity().bindService(PlacesService.buildBindIntent(getActivity()), serviceConnection, Context.BIND_AUTO_CREATE);
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
     * Unbind from the places service
     */
    @Override
    public void onStop() {
        super.onStop();

        //unbind from the places service
        if (bound) {
            Log.d(TAG, "onStop: Unbinding from service");
            getActivity().unbindService(serviceConnection);
            bound = false;
        }
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

        //if bound to service, and the query length is greater than 2 chars
        if (bound && newText.length() > MINIMUM_QUERY_TEXT_LENGTH) {
            //request for autocomplete results from the bound service via the binder
            Log.d(TAG, "onQueryTextChange: Requesting autocomplete results. Query:" + newText);
            binder.requestAutocompleteResults(new Pair<String, PlacesService.AutocompleteCallback>(newText, this));
        }
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
     * Callback from PlacesFetchService.AutocompleteCallback
     * This is the callback from the bound service
     * We successfully retrieved autocomplete results so give it to the listview's adapter.
     * @param items
     */
    @Override
    public void onAutocompleteResult(ArrayList<LocationSearchItem> items) {
        Log.d(TAG, "onAutocompleteResult. Items size:" + items.size());

        //check if we still have a view
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            //oops, too late, view is gone
            Log.d(TAG, "onAutocompleteResult: View is null so nothing to do");
            return;
        }

        //good, we still have a view
        //check if listview adapter exists
        LocationSearchAdapter locationSearchAdapter = (LocationSearchAdapter) viewHolder.searchListView.getAdapter();
        if (locationSearchAdapter == null) {
            //create and set the adapter with the search result items
            Log.d(TAG, "onAutocompleteResult: Search adapter is null, so creating a new one");
            locationSearchAdapter = new LocationSearchAdapter(viewHolder.searchListView.getContext(), items);
            viewHolder.searchListView.setAdapter(locationSearchAdapter);
        }
        else {
            //update the adapter
            Log.d(TAG, "onAutocompleteResult: Search adapter is not null, so updating");
            locationSearchAdapter.updateAllItems(items);
        }

        //set empty view text to "no locations found"
        if (items == null || items.size() == 0) {
            viewHolder.searchStatus.setText(R.string.search_no_results);
        }
    }

    /**
     * Callback from PlacesFetchService.AutocompleteCallback
     * We failed to get autocomplete results so let the user know.
     * @param statusCode
     * @param statusMessage
     */
    @Override
    public void onAutocompleteFailure(int statusCode, String statusMessage) {
        Log.d(TAG, String.format("onAutocompleteFailure: StatusCode:%s, Message:%s", statusCode, statusMessage));

        //the view is already gone, so don't bother
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) return;

        //set empty view text to "no locations found"
        viewHolder.searchStatus.setText(R.string.search_no_results);

        //create a snackbar to inform the user
        final Snackbar snackbar = Snackbar.make(viewHolder.rootLayout, statusMessage, Snackbar.LENGTH_LONG);
        snackbar.setAction(getString(R.string.snackbar_retry), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onAutocompleteFailure.onClick: Retrying autocomplete");
                onQueryTextChange(lastQuery);
            }
        });
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
        final View rootLayout;
        final ListView searchListView;
        final TextView searchStatus;

        final ImageView searchAttribution;

        ViewHolder(View view) {
            rootLayout = view.findViewById(R.id.root_layout);

            searchListView = (ListView) view.findViewById(R.id.search_listview);
            searchStatus = (TextView) view.findViewById(R.id.search_empty);
            searchListView.setEmptyView(searchStatus);

            searchAttribution = (ImageView) view.findViewById(R.id.search_attribution);
        }
    }
}
