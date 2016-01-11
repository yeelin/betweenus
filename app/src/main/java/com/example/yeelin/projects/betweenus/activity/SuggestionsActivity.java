package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.analytics.EventConstants;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.fb.query.FbConstants;
import com.example.yeelin.projects.betweenus.fragment.SuggestionsClusterMapFragment;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.example.yeelin.projects.betweenus.data.generic.model.SimplifiedBusiness;
import com.example.yeelin.projects.betweenus.fragment.SuggestionsDataFragment;
import com.example.yeelin.projects.betweenus.fragment.callback.OnSuggestionActionListener;
import com.example.yeelin.projects.betweenus.fragment.SuggestionsListFragment;
import com.example.yeelin.projects.betweenus.receiver.PlacesBroadcastReceiver;
import com.example.yeelin.projects.betweenus.service.PlacesService;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by ninjakiki on 7/13/15.
 */
public class SuggestionsActivity
        extends BasePlayServicesActivity
        implements
        SuggestionsDataFragment.SuggestionsDataListener,
        OnSuggestionActionListener,
        PlacesBroadcastReceiver.PlacesBroadcastListener {
    //logcat
    private static final String TAG = SuggestionsActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_SEARCH_TERM = SuggestionsActivity.class.getSimpleName() + ".searchTerm";
    private static final String EXTRA_PLACE_IDS = SuggestionsActivity.class.getSimpleName() + ".placeIds";

    //fragment constants
    private static final int LIST = 0;
    private static final int MAP = 1;
    private static final int DATA = 2;
    private static final int FRAGMENT_COUNT = DATA + 1;

    //fragment tag
    private static final String FRAGMENT_TAG_SUGGESTION_DATA = SuggestionsActivity.class.getSimpleName() + ".suggestionData";

    //saved instance state
    private static final String STATE_SHOWING_MAP = SuggestionsActivity.class.getSimpleName() + ".showingMap";
    private static final String STATE_USER_LATLNG = SuggestionsActivity.class.getSimpleName() + ".userLatLng";
    private static final String STATE_FRIEND_LATLNG = SuggestionsActivity.class.getSimpleName() + ".friendLatLng";
    private static final String STATE_MID_LATLNG = SuggestionsActivity.class.getSimpleName() + ".midLatLng";
    private static final String STATE_HAS_MORE_DATA = SuggestionsActivity.class.getSimpleName() + ".hasMoreData";
    private static final String STATE_NEXT_URL = SuggestionsActivity.class.getSimpleName() + ".nextUrl";
    private static final String STATE_PAGE_NUMBER = SuggestionsActivity.class.getSimpleName() + ".pageNumber";
    private static final String STATE_SELECTED_IDS = SuggestionsActivity.class.getSimpleName() + ".selectedIds";
    private static final String STATE_SELECTED_POSITIONS = SuggestionsActivity.class.getSimpleName() + ".selectedPositions";

    //activity request code
    private static final int REQUEST_CODE_PAGER_VIEW = 101;

    //member variables
    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private boolean showingMap = false;
    private boolean hasMoreData;
    private String nextUrl;
    private int pageNumber;

    private ArrayMap<String,Integer> selectedIdsMap = new ArrayMap<>();
    private PlacesBroadcastReceiver placesBroadcastReceiver;

    /**
     * Builds the appropriate intent to start this activity.
     * @param context
     * @param searchTerm
     * @param userPlaceId
     * @param friendPlaceId
     * @return
     */
    public static Intent buildIntent(Context context, String searchTerm, String userPlaceId, String friendPlaceId) {
        Intent intent = new Intent(context, SuggestionsActivity.class);

        //put extras
        intent.putExtra(EXTRA_SEARCH_TERM, searchTerm);

        ArrayList<String> placeIds = new ArrayList<>(2);
        placeIds.add(userPlaceId);
        placeIds.add(friendPlaceId);
        intent.putStringArrayListExtra(EXTRA_PLACE_IDS, placeIds);

        return intent;
    }

    /**
     * Creates the activity and does the following:
     * 1. Creates the list and map fragments
     * 2. Show/hide the list and map fragments
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setup view and toolbar
        setContentView(R.layout.activity_suggestions);
        setupToolbar(R.id.suggestions_toolbar, true);

        //read extras from intent
        Intent intent = getIntent();
        String searchTerm = intent.getStringExtra(EXTRA_SEARCH_TERM);
        ArrayList<String> placeIds = intent.getStringArrayListExtra(EXTRA_PLACE_IDS);

        //read from save instance state
        if (savedInstanceState != null) {
            //restore last shown state
            showingMap = savedInstanceState.getBoolean(STATE_SHOWING_MAP, false);

            //restore latlngs
            userLatLng = savedInstanceState.getParcelable(STATE_USER_LATLNG);
            friendLatLng = savedInstanceState.getParcelable(STATE_FRIEND_LATLNG);
            midLatLng = savedInstanceState.getParcelable(STATE_MID_LATLNG);

            //restore info related to data paging
            pageNumber = savedInstanceState.getInt(STATE_PAGE_NUMBER, pageNumber);
            hasMoreData = savedInstanceState.getBoolean(STATE_HAS_MORE_DATA, hasMoreData);
            nextUrl = savedInstanceState.getString(STATE_NEXT_URL, nextUrl);
            Log.d(TAG, String.format("onCreate: savedInstanceState != null. PageNumber:%d, HasMoreData:%s, NextUrl:%s",
                    pageNumber, hasMoreData, nextUrl));

            //restore map of selected ids
            ArrayList<String> selectedIdsList = savedInstanceState.getStringArrayList(STATE_SELECTED_IDS);
            ArrayList<Integer> selectedPositionsList = savedInstanceState.getIntegerArrayList(STATE_SELECTED_POSITIONS);
            if (selectedIdsList != null && selectedPositionsList != null) {
                selectedIdsMap.ensureCapacity(selectedIdsList.size());
                for (int i=0; i < selectedIdsList.size(); i++) {
                    selectedIdsMap.put(selectedIdsList.get(i), selectedPositionsList.get(i));
                }
            }
        }

        //initialize and store references to fragments (must be done before calling showFragment)
        FragmentManager fm = getSupportFragmentManager();
        fragments[LIST] = fm.findFragmentById(R.id.list_fragment);
        fragments[MAP] = fm.findFragmentById(R.id.map_fragment);
        fragments[DATA] = fm.findFragmentByTag(FRAGMENT_TAG_SUGGESTION_DATA);
        if (fragments[DATA] == null) {
            Log.d(TAG, "onCreate: Data fragment is null so creating a new one now");
            fragments[DATA] = SuggestionsDataFragment.newInstance(searchTerm,
                    getResources().getDimensionPixelSize(R.dimen.profile_image_size),
                    FbConstants.USE_FB ? LocalConstants.FACEBOOK : LocalConstants.YELP);
            fm.beginTransaction()
                    .add(fragments[DATA], FRAGMENT_TAG_SUGGESTION_DATA)
                    .disallowAddToBackStack()
                    .commit();
        }

        //show the list fragment if this is the first time (i.e. no savedInstanceState) or
        //restore the view to the last fragment when we last left the activity
        if (showingMap) showMapFragment(false); //false == don't add to backstack
        else showListFragment(false);

        //check to make sure latlngs are not null
        //note: even in configuration change, it's possible that the activity was destroyed before the latlngs were set with values
        if (userLatLng == null || friendLatLng == null || midLatLng == null) {
            //latlngs are null so start the place service to get the latlngs
            Log.d(TAG, "onCreate: LatLngs are null.  Starting PlacesService");
            startService(PlacesService.buildGetPlaceByIdIntent(this, placeIds));
        }
        else {
            //latlngs are not null so initialize the loader to fetch suggestions from the network
            Log.d(TAG, "onCreate: LatLngs are not null. Fetching data from cache");
            ((SuggestionsDataFragment) fragments[DATA]).fetchSuggestions(pageNumber, nextUrl); //since this is the initial call, nextUrl is null
        }
    }

    /**
     * Inflate the menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_suggestions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Create a broadcast receiver and register for place broadcasts (success and failures).
     * Log activation
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Registering for broadcasts");
        placesBroadcastReceiver = new PlacesBroadcastReceiver(this, this); //we are a place broadcast listener
        AppEventsLogger.activateApp(this);
    }

    /**
     * Unregister for place broadcasts. Log deactivation.
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Unregistering for broadcasts");
        placesBroadcastReceiver.unregister();
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    /**
     * Saves out
     * 1. the boolean showingMap so that we know which fragment is being displayed
     * 2. the latlngs (user, friend, mid) so that we don't have to requery the service
     * 3. page number, hasMoreData, and nextUrl so that we can restore the listview easily
     * 3. the selected ids map so that we know which results were selected by the user
     * 4. the selected positions in the list/pager corresponding to the selected ids
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SHOWING_MAP, showingMap);

        if (userLatLng != null) outState.putParcelable(STATE_USER_LATLNG, userLatLng);
        if (friendLatLng != null) outState.putParcelable(STATE_FRIEND_LATLNG, friendLatLng);
        if (midLatLng != null) outState.putParcelable(STATE_MID_LATLNG, midLatLng);

        outState.putInt(STATE_PAGE_NUMBER, pageNumber);
        outState.putBoolean(STATE_HAS_MORE_DATA, hasMoreData);
        if (nextUrl != null) outState.putString(STATE_NEXT_URL, nextUrl);

        outState.putStringArrayList(STATE_SELECTED_IDS, new ArrayList<>(selectedIdsMap.keySet()));
        outState.putIntegerArrayList(STATE_SELECTED_POSITIONS, new ArrayList<>(selectedIdsMap.values()));
    }

    /**
     * Handles user selection of menu options:
     * 1. Home - navigates up to parent activity
     * 2. Select - indicates that the user has finished selection and wants to proceed with the invitation
     * 3. Toggle - toggles the view between the list and map
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Up button was clicked
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: Up button clicked");
                navigateUpToParentActivity(this);
                return true;

            //select and invite button was clicked
            case R.id.action_select:
                Log.d(TAG, "onOptionsItemSelected: Invite button clicked");
                if (selectedIdsMap.size() == 0) {
                    //create a snackbar to inform the user that a selection must be made before inviting friend
                    final View rootView = findViewById(R.id.root_layout);
                    if (rootView != null) {
                        final Snackbar snackbar = Snackbar.make(rootView, getString(R.string.snackbar_no_selections), Snackbar.LENGTH_LONG);
                        //provide an action link on the snackbar to go back to the location entry screen
                        snackbar.setAction(getString(R.string.snackbar_ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        });
                    }
                }
                else {
                    //start invite activity
                    startActivity(InvitationActivity.buildIntent(this,
                            SimplifiedBusiness.buildSelectedItemsList(((SuggestionsDataFragment) fragments[DATA]).getAllResults(), selectedIdsMap),
                            showingMap ? EventConstants.EVENT_PARAM_VIEW_MAP : EventConstants.EVENT_PARAM_VIEW_LIST));
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * SuggestionsDataListener callback
     * This method is called when the single requested page is returned by the SuggestionsDataFragment.
     * @param localResult
     * @param pageNumber
     */
    @Override
    public void onSinglePageLoad(@Nullable LocalResult localResult, int pageNumber) {
        if (localResult == null) return;

        //update page number
        this.pageNumber = pageNumber;
        //update nextUrl
        nextUrl = localResult.getNextUrl();
        //check if there's more data to fetch
        hasMoreData = isThereMoreData();

        //update the respective fragments with one new page of data
        if (fragments[LIST] != null) {
            ((SuggestionsListFragment) fragments[LIST]).onSinglePageLoad(localResult, selectedIdsMap, hasMoreData, pageNumber);
        }

        if (fragments[MAP] != null) {
            ((SuggestionsClusterMapFragment) fragments[MAP]).onSinglePageLoad(localResult, selectedIdsMap, hasMoreData, pageNumber);
        }
    }

    /**
     * SuggestionsDataListener callback
     * This method is called when multi pages are loaded by the SuggestionsDataFragment.
     * @param localResultArrayList
     */
    @Override
    public void onMultiPageLoad(@Nullable ArrayList<LocalResult> localResultArrayList) {
        if (localResultArrayList == null) return;

        //update page number
        pageNumber = localResultArrayList.size()-1;
        //update nextUrl
        nextUrl = localResultArrayList.get(pageNumber).getNextUrl();
        //check if there's more data to fetch
        hasMoreData = isThereMoreData();

        //update the respective fragments with multiple pages of data
        if (fragments[LIST] != null) {
            ((SuggestionsListFragment) fragments[LIST]).onMultiPageLoad(localResultArrayList, selectedIdsMap, hasMoreData);
        }

        if (fragments[MAP] != null) {
            ((SuggestionsClusterMapFragment) fragments[MAP]).onMultiPageLoad(localResultArrayList, selectedIdsMap, hasMoreData);
        }
    }

    /**
     * According to fb documentation, we should stop paging when 'next' is no longer available.
     * Note: AfterId should not be used because it is the cursor that points to the end of the
     * page of data that has been returned.
     * @return
     */
    private boolean isThereMoreData() {
        return nextUrl != null;
    }

    /**
     * OnSuggestionActionListener implementation
     * Start the pager activity when a place in the list or map is clicked
     * @param id business id
     * @param name business name
     * @param latLng business latlng
     * @param position position of item in list and pager
     */
    @Override
    public void onSuggestionClick(String id, String name, LatLng latLng, int position) {
        Log.d(TAG, String.format("onSuggestionClick: BusinessId:%s, Name:%s, Position:%d", id, name, position));

        Intent pagerIntent = SuggestionsPagerActivity.buildIntent(this,
                position,
                SimplifiedBusiness.buildSimplifiedBusinessList(((SuggestionsDataFragment) fragments[DATA]).getAllResults()),
                new ArrayList<>(selectedIdsMap.keySet()),
                new ArrayList<>(selectedIdsMap.values()),
                userLatLng, friendLatLng, midLatLng);
        startActivityForResult(pagerIntent, REQUEST_CODE_PAGER_VIEW);
    }

    /**
     * OnSuggestionActionListener implementation
     * Flips the toggle state of the item in the selectedIdsMap.
     * If the item is in the map, it is removed.
     * If the item is not in the map, it is added.
     * @param id business id
     * @param position position of item in list or pager
     * @param toggleState resulting toggle state (true means selected, false means not selected)
     */
    @Override
    public void onSuggestionToggle(String id, int position, boolean toggleState) {
        if (selectedIdsMap.containsKey(id) && !toggleState) {
            //if the item is in the map AND resulting toggle state is false (not selected), we remove it
            Log.d(TAG, "onSuggestionToggle: Item is in the map, so removing:" + id);
            selectedIdsMap.remove(id);
        }
        else if (!selectedIdsMap.containsKey(id) && toggleState) {
            //if the item is not in the map AND resulting toggle state is true, we add it
            Log.d(TAG, "onSuggestionToggle: Item is not in the map, so adding:" + id);
            selectedIdsMap.put(id, position);
        }

        //notify all the fragments that a selection has changed
        if (fragments[MAP] != null) {
            Log.d(TAG, "onSuggestionToggle: Notifying map fragment that a selection has changed");
            ((SuggestionsClusterMapFragment) fragments[MAP]).onSelectionChanged(id, toggleState);
        }

        if (fragments[LIST] != null) {
            Log.d(TAG, "onSuggestionToggle: Notifying list fragment that a selection has changed");
            ((SuggestionsListFragment) fragments[LIST]).onSelectionChanged(id, toggleState);
        }
    }

    /**
     * OnSuggestionActionListener implementation
     * This helper method is called by fragments to load more data.
     * Fragments would typically have checked if there is more data to load before calling this, but
     * just in case they didn't, this method checks again.
     * It is the responsibility of the fragment to determine when to call this method.  That is, this
     * method will immediately go fetch more data with the nextUrl without any further checks on visible
     * indexes, etc.
     */
    @Override
    public void onMoreDataFetch() {
        Log.d(TAG, "onMoreDataFetch: hasMoreData:" + hasMoreData);
        //figure out if we have more data to fetch, i.e. hasMoreData == true
        if (!hasMoreData) return;

        //fetch more data
        ((SuggestionsDataFragment) fragments[DATA]).fetchSuggestions(pageNumber + 1, nextUrl);
    }

    /**
     * OnSuggestionActionListener implementation
     */
    @Override
    public void showList() {
        showingMap = false;
        showListFragment(true);
    }

    /**
     * OnSuggestionActionListener implementation
     */
    @Override
    public void showMap() {
        showingMap = true;
        showMapFragment(true);
    }

    /**
     * Helper method for showing the list fragment
     * @param addToBackStack
     */
    private void showListFragment(boolean addToBackStack) {
        showFragment(fragments, LIST, addToBackStack);
    }

    /**
     * Helper method for showing the map fragment
     * @param addToBackStack
     */
    private void showMapFragment(boolean addToBackStack) {
        showFragment(fragments, MAP, addToBackStack);
    }

    /**
     * Handle the activity result from the detail activity or pager activity.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: RequestCode:" + requestCode);

        if (requestCode == REQUEST_CODE_PAGER_VIEW) {
            if (data == null) {
                Log.d(TAG, "onActivityResult: REQUEST_CODE_PAGER_VIEW. Data is null, so nothing to do");
                return;
            }

            //read the intent extras
            final ArrayList<String> selectedIdsList = data.getStringArrayListExtra(SuggestionsPagerActivity.EXTRA_SELECTED_IDS);
            final ArrayList<Integer> selectedPositionsList = data.getIntegerArrayListExtra(SuggestionsPagerActivity.EXTRA_SELECTED_POSITIONS);
            final ArrayMap<String, Integer> newSelectedIdsMap = new ArrayMap<>(selectedIdsList.size());
            for (int i=0; i<selectedIdsList.size(); i++) {
                newSelectedIdsMap.put(selectedIdsList.get(i), selectedPositionsList.get(i));
            }

            //check the old map against the contents of the new map for stuff to remove, i.e. got deselected
            Set<String> keys = selectedIdsMap.keySet();
            for (String id : keys) {
                if (!newSelectedIdsMap.containsKey(id)) {
                    //new map doesn't have it, so remove
                    Log.d(TAG, "onActivityResult: Removing Id:" + id);
                    onSuggestionToggle(id, selectedIdsMap.get(id), false);
                }
            }

            //check the new map against the contents of the old map for stuff to add, i.e. got selected
            Set<String> newKeys = newSelectedIdsMap.keySet();
            for (String id: newKeys) {
                if (!selectedIdsMap.containsKey(id)) {
                    //old map doesn't have it, so add
                    Log.d(TAG, "onActivityResult: Adding Id:" + id);
                    onSuggestionToggle(id, newSelectedIdsMap.get(id), true);
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * PlacesBroadcastReceiver.PlacesBroadcastListener callback
     * We have successfully retrieved the latlng for the user and friend, so
     * call the loader to fetch data from Yelp.
     *
     * @param userLatLng
     * @param friendLatLng
     */
    @Override
    public void onPlacesSuccess(LatLng userLatLng, LatLng friendLatLng) {
        Log.d(TAG, String.format("onPlacesSuccess: User:%s Friend:%s", userLatLng, friendLatLng));

        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        midLatLng = LocationUtils.computeMidPoint(userLatLng, friendLatLng);

        //share latlngs with all fragments
        ((SuggestionsListFragment) fragments[LIST]).onLatLngLoad(userLatLng, friendLatLng, midLatLng);
        ((SuggestionsClusterMapFragment) fragments[MAP]).onLatLngLoad(userLatLng, friendLatLng, midLatLng);
        ((SuggestionsDataFragment) fragments[DATA]).onLatLngLoad(userLatLng, friendLatLng, midLatLng);

        //search for places with the latlngs we just got
        ((SuggestionsDataFragment) fragments[DATA]).forceReload();
    }

    /**
     * PlacesBroadcastReceiver.PlacesBroadcastListener callback
     * We failed to retrieve the latlng for the user and friend, so display a snackbar
     * to inform the user as there is not much else we can do.  The snackbar allows the user to go back to the Location Entry screen.
     * TODO: Implement retry instead of just go back.
     *
     * @param statusCode
     * @param statusMessage
     */
    @Override
    public void onPlacesFailure(int statusCode, String statusMessage) {
        Log.d(TAG, String.format("onPlacesFailure: StatusCode:%d, Message:%s", statusCode, statusMessage));

        //create a snackbar to inform the user
        final View rootView = findViewById(R.id.root_layout);
        if (rootView != null) {
            final Snackbar snackbar = Snackbar.make(rootView, getString(R.string.get_place_by_id_error), Snackbar.LENGTH_LONG);
            //provide an action link on the snackbar to go back to the location entry screen
            snackbar.setAction(getString(R.string.snackbar_go_back), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onPlacesFailure.onClick: Going back to Location Entry screen");
                    navigateUpToParentActivity(SuggestionsActivity.this);
                }
            });
        }
    }
}
