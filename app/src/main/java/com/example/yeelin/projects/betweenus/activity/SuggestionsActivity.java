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
import com.example.yeelin.projects.betweenus.data.fb.query.FbApiHelper;
import com.example.yeelin.projects.betweenus.fragment.SuggestionsClusterMapFragment;
import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.example.yeelin.projects.betweenus.model.SimplifiedBusiness;
import com.example.yeelin.projects.betweenus.fragment.OnSuggestionActionListener;
import com.example.yeelin.projects.betweenus.fragment.SuggestionsListFragment;
import com.example.yeelin.projects.betweenus.loader.LoaderId;
import com.example.yeelin.projects.betweenus.loader.SuggestionsLoaderCallbacks;
import com.example.yeelin.projects.betweenus.receiver.PlacesBroadcastReceiver;
import com.example.yeelin.projects.betweenus.service.PlacesService;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.facebook.AccessToken;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by ninjakiki on 7/13/15.
 */
public class SuggestionsActivity
        extends BasePlayServicesActivity
        implements SuggestionsLoaderCallbacks.SuggestionsLoaderListener,
        OnSuggestionActionListener,
        PlacesBroadcastReceiver.PlacesBroadcastListener {
    //TODO: Remove this constant
    private static final boolean USE_FB = true;
    //logcat
    private static final String TAG = SuggestionsActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_SEARCH_TERM = SuggestionsActivity.class.getSimpleName() + ".searchTerm";
    private static final String EXTRA_PLACE_IDS = SuggestionsActivity.class.getSimpleName() + ".placeIds";

    //fragment constants
    private static final int LIST = 0;
    private static final int MAP = 1;
    private static final int FRAGMENT_COUNT = MAP + 1;

    //saved instance state
    private static final String STATE_SHOWING_MAP = SuggestionsActivity.class.getSimpleName() + ".showingMap";
    private static final String STATE_SHOWING_PEOPLE_LOCATION = SuggestionsActivity.class.getSimpleName() + ".showingPeopleLocation";
    private static final String STATE_USER_LATLNG = SuggestionsActivity.class.getSimpleName() + ".userLatLng";
    private static final String STATE_FRIEND_LATLNG = SuggestionsActivity.class.getSimpleName() + ".friendLatLng";
    private static final String STATE_MID_LATLNG = SuggestionsActivity.class.getSimpleName() + ".midLatLng";
    private static final String STATE_SELECTED_IDS = SuggestionsActivity.class.getSimpleName() + ".selectedIds";
    private static final String STATE_SELECTED_POSITIONS = SuggestionsActivity.class.getSimpleName() + ".selectedPositions";

    //activity request code
    private static final int REQUEST_CODE_DETAIL_VIEW = 100;
    private static final int REQUEST_CODE_PAGER_VIEW = 101;

    //member variables
    private String searchTerm;
    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private boolean showingMap = false;
    private LocalResult result;
    private ArrayMap<String,Integer> selectedIdsMap = new ArrayMap<>();
    private PlacesBroadcastReceiver placesBroadcastReceiver;

    private MenuItem peopleLocationMenuItem;
    private boolean showingPeopleLocation = false;

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
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //setup view and toolbar
        setContentView(R.layout.activity_suggestions);
        setupToolbar(R.id.suggestions_toolbar, true);

        //read extras from intent
        Intent intent = getIntent();
        searchTerm = intent.getStringExtra(EXTRA_SEARCH_TERM);
        ArrayList<String> placeIds = intent.getStringArrayListExtra(EXTRA_PLACE_IDS);

        //store references to both fragments
        FragmentManager fm = getSupportFragmentManager();
        fragments[LIST] = fm.findFragmentById(R.id.list_fragment);
        fragments[MAP] = fm.findFragmentById(R.id.map_fragment);

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate: Saved instance state is not null");

            //restore last shown state
            showingMap = savedInstanceState.getBoolean(STATE_SHOWING_MAP, false);
            showingPeopleLocation = savedInstanceState.getBoolean(STATE_SHOWING_PEOPLE_LOCATION, false);

            //restore latlngs
            userLatLng = savedInstanceState.getParcelable(STATE_USER_LATLNG);
            friendLatLng = savedInstanceState.getParcelable(STATE_FRIEND_LATLNG);
            midLatLng = savedInstanceState.getParcelable(STATE_MID_LATLNG);

            //restore map of selected ids
            ArrayList<String> selectedIdsList = savedInstanceState.getStringArrayList(STATE_SELECTED_IDS);
            ArrayList<Integer> selectedPositionsList = savedInstanceState.getIntegerArrayList(STATE_SELECTED_POSITIONS);
            if (selectedIdsList != null && selectedPositionsList != null) {
                selectedIdsMap.ensureCapacity(selectedIdsList.size());
                for (int i=0; i < selectedIdsList.size(); i++) {
                    Log.d(TAG, "onCreate: SelectedId:" + selectedIdsList.get(i));
                    selectedIdsMap.put(selectedIdsList.get(i), selectedPositionsList.get(i));
                }
                Log.d(TAG, "onCreate: Restored selected ids: " + selectedIdsList);
            }
        }

        //show the list fragment if this is the first time (i.e. no savedInstanceState) or
        //restore the view to the last fragment when we last left the activity (either showing map or list)
        toggleListAndMapFragments(false, false); //false == don't add to backstack, false == don't load data since it's not ready

        //check to make sure latlngs are not null
        //note: even in configuration change, it's possible that the activity was destroyed before the latlngs were set with values
        if (userLatLng == null || friendLatLng == null || midLatLng == null) {
            //latlngs are null so start the place service to get the latlngs
            Log.d(TAG, "onCreate: LatLngs are null.  Starting PlacesService");
            startService(PlacesService.buildGetPlaceByIdIntent(this, placeIds));
        }
        else {
            //latlngs are not null so initialize the loader to fetch suggestions from the network
            SuggestionsLoaderCallbacks.initLoader(this, getSupportLoaderManager(), this, searchTerm, userLatLng, friendLatLng, midLatLng);
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

        //configure the list/map toggle to show the correct icon and title depending on boolean showingMap
        final MenuItem toggleItem = menu.findItem(R.id.action_toggle);
        toggleMapMenuIcon(toggleItem);
        toggleMapMenuTitle(toggleItem);

        //configure the people toggle to show the correct icon and title depending on boolean showingPeopleLocation
        peopleLocationMenuItem = menu.findItem(R.id.action_show_people_location);
        togglePeopleMenuIcon();
        togglePeopleMenuTitle();

        return super.onCreateOptionsMenu(menu);
    }

    /** Create a broadcast receiver and register for place broadcasts (success and failures)
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Registering for broadcasts");
        placesBroadcastReceiver = new PlacesBroadcastReceiver(this, this); //we are a place broadcast listener
    }

    /**
     * Unregister for place broadcasts
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Unregistering for broadcasts");
        placesBroadcastReceiver.unregister();
        super.onPause();
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
                if (selectedIdsMap.size() > 0) {
                    startActivity(InvitationActivity.buildIntent(this, buildSelectedItemsList()));
                }
                return true;

            //toggle list/map view was clicked
            case R.id.action_toggle:
                Log.d(TAG, String.format("onOptionsItemSelected: Toggle clicked. Current:%s, Next:%s", showingMap ? "map" : "list", !showingMap ? "map" : "list"));
                showingMap = !showingMap;

                //toggle list/map icon and title
                toggleMapMenuIcon(item);
                toggleMapMenuTitle(item);
                //toggle list/map fragments
                toggleListAndMapFragments(true, true); //true, true == add to backstack and load data

                //toggle people location item on/off
                togglePeopleMenuIcon();
                togglePeopleMenuTitle();
                return true;

            //toggle people location on/off
            case R.id.action_show_people_location:
                Log.d(TAG, String.format("onOptionsItemSelected: People toggle clicked. Current:%s, Next:%s", showingPeopleLocation ? "people" : "no people", !showingPeopleLocation ? "people" : "no people"));
                showingPeopleLocation = !showingPeopleLocation;

                //toggle people location item on/off
                togglePeopleMenuIcon();
                togglePeopleMenuTitle();

                //notify map fragment to toggle the people markers
                if (fragments[MAP] != null)
                    ((SuggestionsClusterMapFragment) fragments[MAP]).togglePeopleLocation(showingPeopleLocation, true); //true = update UI immediately
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Helper method to build the selected items array list for marshalling across to the
     * invitation activity.
     * @return
     */
    private ArrayList<SimplifiedBusiness> buildSelectedItemsList() {
        Log.d(TAG, "buildSelectedItemsList");
        ArrayList<SimplifiedBusiness> selectedItems = new ArrayList<>(selectedIdsMap.size());

        for (int i=0; i<result.getLocalBusinesses().size(); i++) {
            LocalBusiness business = result.getLocalBusinesses().get(i);
            if (selectedIdsMap.containsKey(business.getId())) {
                selectedItems.add(SimplifiedBusiness.newInstance(business));
            }
        }

        return selectedItems;
    }

    /**
     * Saves out
     * 1. the boolean showingMap so that we know which fragment is being displayed
     * 2. the boolean showingPeopleLocation so that we know if people location were being displayed
     * 3. the latlngs (user, friend, mid) so that we don't have to requery the service
     * 4. the selected ids map so that we know which results were selected by the user
     * 5. the selected positions in the list/pager corresponding to the selected ids
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SHOWING_MAP, showingMap);
        outState.putBoolean(STATE_SHOWING_PEOPLE_LOCATION, showingPeopleLocation);

        if (userLatLng != null) outState.putParcelable(STATE_USER_LATLNG, userLatLng);
        if (friendLatLng != null) outState.putParcelable(STATE_FRIEND_LATLNG, friendLatLng);
        if (midLatLng != null) outState.putParcelable(STATE_MID_LATLNG, midLatLng);

        outState.putStringArrayList(STATE_SELECTED_IDS, new ArrayList<>(selectedIdsMap.keySet()));
        outState.putIntegerArrayList(STATE_SELECTED_POSITIONS, new ArrayList<>(selectedIdsMap.values()));
    }

    /**
     * Depending on the boolean showingMap, this method toggles the icon of the given menu item
     * @param item
     */
    private void toggleMapMenuIcon(MenuItem item) {
        item.setIcon(showingMap ? R.drawable.ic_action_view_list : R.drawable.ic_action_maps_map);
    }

    /**
     * Depending on the boolean showingMap, this method toggles the title of the given menu item
     * @param item
     */
    private void toggleMapMenuTitle(MenuItem item) {
        item.setTitle(showingMap ? R.string.action_view_as_list : R.string.action_view_as_map);
    }

    /**
     * Depending on the boolean showingPeopleLocation, this method toggles the icon of the
     * people menu item
     */
    private void togglePeopleMenuIcon() {
        if (showingMap) {
            peopleLocationMenuItem.setIcon(showingPeopleLocation ?
                    R.drawable.ic_action_social_people : R.drawable.ic_action_social_people_outline);
            peopleLocationMenuItem.setVisible(true);
        }
        else {
            peopleLocationMenuItem.setVisible(false);
        }
    }

    /**
     * Depending on the boolean showingPeopleLocation, this method toggles the title of the
     * people menu item
     */
    private void togglePeopleMenuTitle() {
        if (showingMap) {
            peopleLocationMenuItem.setTitle(showingPeopleLocation ?
                    R.string.action_hide_people_location : R.string.action_show_people_location);
        }
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
     * Depending on the boolean showingMap, this method toggles the visibility of the list and map fragments.
     * If shouldLoadData is true, then the current result along with the selectedIdsMap is passed
     * to the fragment.
     * If selectionsChanged is true, then the id of the changed item is passed to the fragment.
     *
     * @param addToBackStack
     * @param shouldLoadData
     */
    private void toggleListAndMapFragments(boolean addToBackStack, boolean shouldLoadData) {
        if (showingMap) {
            Log.d(TAG, "toggleListAndMapFragments: Showing map fragment");
            showMapFragment(addToBackStack);

            //tell the map fragment if we should be showing people location, but only update when the rest of the data is ready
            SuggestionsClusterMapFragment mapFragment = (SuggestionsClusterMapFragment) fragments[MAP];
            mapFragment.togglePeopleLocation(showingPeopleLocation, false); //false = don't update immediately

            if (shouldLoadData) {
                mapFragment.onSuggestionsLoaded(result, selectedIdsMap, userLatLng, friendLatLng, midLatLng);
            }
        }
        else {
            Log.d(TAG, "toggleListAndMapFragments: Showing list fragment");
            showListFragment(addToBackStack);

            if (shouldLoadData) {
                ((SuggestionsListFragment) fragments[LIST]).onSuggestionsLoaded(result, selectedIdsMap, userLatLng, friendLatLng, midLatLng);
            }
        }
    }

    /**
     * SuggestionsLoaderCallbacks.SuggestionsLoaderListener callback
     * When the loader delivers the results, this method would be called.  Depending on which fragment is in view,
     * the data would be passed to the appropriate fragment.
     * @param loaderId
     * @param result
     */
    @Override
    public void onLoadComplete(LoaderId loaderId, @Nullable LocalResult result) {
        if (loaderId != LoaderId.MULTI_PLACES) {
            Log.d(TAG, "onLoadComplete: Unknown loaderId:" + loaderId);
            return;
        }

        //debugging purposes
        if (result == null) {
            Log.d(TAG, "onLoadComplete: Result is null. Loader must be resetting");
        }
        else {
            Log.d(TAG, "onLoadComplete: Item count:" + result.getLocalBusinesses().size());
        }

        //reset the member variables
        this.result = result;

        if (showingMap) {
            Log.d(TAG, "onLoadComplete: Notifying map fragment");

            if (fragments[MAP] != null) {
                SuggestionsClusterMapFragment mapFragment = (SuggestionsClusterMapFragment) fragments[MAP];
                mapFragment.togglePeopleLocation(showingPeopleLocation, false); //false = don't update immediately
                mapFragment.onSuggestionsLoaded(result, selectedIdsMap, userLatLng, friendLatLng, midLatLng);
            }
        }
        else {
            Log.d(TAG, "onLoadComplete: Notifying list fragment");

            if (fragments[LIST] != null) {
                Log.d(TAG, "onLoadComplete: List fragment is not null");
                ((SuggestionsListFragment) fragments[LIST]).onSuggestionsLoaded(result, selectedIdsMap, userLatLng, friendLatLng, midLatLng);
            }
            else {
                Log.d(TAG, "onLoadComplete: List fragment is null");
            }
        }
    }

    /**
     * OnSuggestionActionListener implementation
     * Start the pager activity
     * @param id business id
     * @param name business name
     * @param latLng business latlng
     * @param position position of item in pager
     */
    @Override
    public void onSuggestionClick(String id, String name, LatLng latLng, int position) {
        Log.d(TAG, String.format("onSuggestionClick: BusinessId:%s, Name:%s, Position:%d", id, name, position));

//        Intent detailIntent = SuggestionDetailActivity.buildIntent(this,
//                id, name, latLng,
//                position, selectedIdsMap.containsKey(id),
//                userLatLng, friendLatLng, midLatLng);
//        startActivityForResult(detailIntent, REQUEST_CODE_DETAIL_VIEW);

        Intent pagerIntent = SuggestionsPagerActivity.buildIntent(this,
                position,
                buildSimplifiedBusinessList(),
                new ArrayList<>(selectedIdsMap.keySet()),
                new ArrayList<>(selectedIdsMap.values()),
                userLatLng, friendLatLng, midLatLng);
        startActivityForResult(pagerIntent, REQUEST_CODE_PAGER_VIEW);
    }

    /**
     * Helper method to build the simplified business items array list for marshalling across to the
     * pager activity.
     * @return
     */
    private ArrayList<SimplifiedBusiness> buildSimplifiedBusinessList() {
        Log.d(TAG, "buildSimplifiedBusinessList");
        ArrayList<SimplifiedBusiness> simplifiedBusinesses = new ArrayList<>(result.getLocalBusinesses().size());

        for (int i=0; i<result.getLocalBusinesses().size(); i++) {
            LocalBusiness business = result.getLocalBusinesses().get(i);
            simplifiedBusinesses.add(SimplifiedBusiness.newInstance(business));
        }
        return simplifiedBusinesses;
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
     * Handle the activity result from the detail activity or pager activity.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: RequestCode:" + requestCode);

        if (requestCode == REQUEST_CODE_DETAIL_VIEW) {
            if (data == null) {
                Log.d(TAG, "onActivityResult: REQUEST_CODE_DETAIL_VIEW. Data is null, so nothing to do");
                return;
            }

            //read the intent extras
            String id = data.getStringExtra(SuggestionDetailActivity.EXTRA_ID);
            boolean toggleState = data.getBooleanExtra(SuggestionDetailActivity.EXTRA_TOGGLE_STATE, selectedIdsMap.containsKey(id)); //the default value is the previous value
            int position = data.getIntExtra(SuggestionDetailActivity.EXTRA_POSITION, 0);

            //compare the current selection state from the detail view to the original state in selectedIdsMap
            //if different, then update selectedIdsMap
            Log.d(TAG, String.format("onActivityResult: Data is not null. Id:%s, New selection state:%s, Old selection state:%s", id, toggleState, selectedIdsMap.containsKey(id)));
            if (toggleState != selectedIdsMap.containsKey(id)) {
                Log.d(TAG, "onActivityResult: Selection has changed, so updating the selectedIdsMap");
                onSuggestionToggle(id, position, toggleState);
            }
        }
        else
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

        //initializing the loader to fetch suggestions from the network
        if (!USE_FB) {
            SuggestionsLoaderCallbacks.initLoader(this, getSupportLoaderManager(), this, searchTerm, userLatLng, friendLatLng, midLatLng);
            return;
        }

        //check if user is currently logged into fb
        if (AccessToken.getCurrentAccessToken() != null) {
            Log.d(TAG, "onPlacesSuccess: User is logged in");

            //create fb graph request for searching places
            //provide SuggestionsLoaderCallbacks.SuggestionsLoaderListener as a callback (onLoadComplete) -- TODO: remove hack
            FbApiHelper.searchForPlaces(AccessToken.getCurrentAccessToken(), midLatLng, this);
        }
        else {
            Log.d(TAG, "onPlacesSuccess: User is not logged in");
        }
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
