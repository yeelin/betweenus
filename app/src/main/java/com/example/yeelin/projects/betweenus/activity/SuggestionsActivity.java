package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.SuggestionsListFragment;
import com.example.yeelin.projects.betweenus.fragment.SuggestionsMapFragment;
import com.example.yeelin.projects.betweenus.loader.LoaderId;
import com.example.yeelin.projects.betweenus.loader.SuggestionsLoaderCallbacks;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/13/15.
 */
public class SuggestionsActivity
        extends BaseActivity
        implements SuggestionsLoaderCallbacks.SuggestionsLoaderListener {
    //logcat
    private static final String TAG = SuggestionsActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_SEARCH_TERM = SuggestionsActivity.class.getSimpleName() + ".searchTerm";
    private static final String EXTRA_USER_LOCATION = SuggestionsActivity.class.getSimpleName() + ".userLocation";
    private static final String EXTRA_FRIEND_LOCATION = SuggestionsActivity.class.getSimpleName() + ".friendLocation";

    //fragment tags
    private static final String FRAGMENT_TAG_LIST = SuggestionsListFragment.class.getSimpleName();
    private static final String FRAGMENT_TAG_MAP = SuggestionsMapFragment.class.getSimpleName();

    //saved instance state
    private static final String STATE_SHOWING_MAP = SuggestionsActivity.class.getSimpleName() + ".showingMap";

    //member variables
    private boolean showingMap = false;
    private ArrayList<YelpBusiness> suggestedItems;

    /**
     * Builds the appropriate intent to start this activity.
     * @param context
     * @param userLocation
     * @param friendLocation
     * @return
     */
    public static Intent buildIntent(Context context, String searchTerm,
                                     Location userLocation, Location friendLocation) {
        Intent intent = new Intent(context, SuggestionsActivity.class);

        //put extras
        intent.putExtra(EXTRA_SEARCH_TERM, searchTerm);
        intent.putExtra(EXTRA_USER_LOCATION, userLocation);
        intent.putExtra(EXTRA_FRIEND_LOCATION, friendLocation);

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
        setContentView(R.layout.activity_suggestions);
        //setup toolbar
        setupToolbar(R.id.suggestions_toolbar, true);

        //read extras from intent
        Intent intent = getIntent();
        String searchTerm = intent.getStringExtra(EXTRA_SEARCH_TERM);
        Location userLocation = intent.getParcelableExtra(EXTRA_USER_LOCATION);
        Location friendLocation = intent.getParcelableExtra(EXTRA_FRIEND_LOCATION);

        //check if the fragments exists, otherwise create it
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: Saved instance state is null");
            Fragment listFragment = SuggestionsListFragment.newInstance();
            Fragment mapFragment = SuggestionsMapFragment.newInstance();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.suggestions_fragmentContainer, listFragment, FRAGMENT_TAG_LIST)
                    .add(R.id.suggestions_fragmentContainer, mapFragment, FRAGMENT_TAG_MAP)
                    .hide(mapFragment) //hide the map fragment, by default show the list first
                    .commit();
        }
        else {
            //restore the state when we last left the activity (either showing map or list)
            Log.d(TAG, "onCreate: Saved instance state is not null");
            showingMap = savedInstanceState.getBoolean(STATE_SHOWING_MAP, false);

            toggleListAndMapFragments();
        }

        //initializing the loader to fetch suggestions from the network
        SuggestionsLoaderCallbacks.initLoader(
                this,
                getSupportLoaderManager(),
                this,
                searchTerm,
                userLocation,
                friendLocation);
    }

    /**
     * Inflate the menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_suggestions, menu);

        //configure the toggle item to show the correct icon and title depending on boolean showingMap
        MenuItem toggleItem = menu.findItem(R.id.action_toggle);
        toggleMenuItemIcon(toggleItem);
        toggleMenuItemTitle(toggleItem);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
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
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: Up button clicked");
                navigateUpToParentActivity(this);
                return true;

            case R.id.action_select:
                if (showingMap) {
                    Log.d(TAG, "onOptionsItemSelected: Need to implement onSelectAndSend in map fragment");
                    //TODO: Handle select in map fragment
                }
                else {
                    SuggestionsListFragment listFragment = (SuggestionsListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
                    if (listFragment != null) {
                        ArrayList<String> selectedItemIds = listFragment.onSelectAndSend();
                        Log.d(TAG, "onOptionsItemSelected: Selected Item Ids:" + selectedItemIds);

                        //start the invite activity and pass it the selected ids
                        startActivity(InvitationActivity.buildIntent(this, selectedItemIds));
                    }
                }
                return true;

            case R.id.action_toggle:
                Log.d(TAG, String.format("onOptionsItemSelected: Toggle clicked. Current:%s, Next:%s", showingMap ? "map" : "list", !showingMap ? "map" : "list"));
                showingMap = !showingMap;
                toggleMenuItemIcon(item);
                toggleMenuItemTitle(item);
                toggleListAndMapFragments();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Saves out the boolean showingMap so that we know which fragment is being displayed
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SHOWING_MAP, showingMap);
    }

    /**
     * Depending on the boolean showingMap, this method toggles the icon of the given menu item
     * @param item
     */
    private void toggleMenuItemIcon(MenuItem item) {
        item.setIcon(showingMap ? R.drawable.ic_action_view_list : R.drawable.ic_action_maps_map);
    }

    /**
     * Depending on the boolean showingMap, this method toggles the title of the given menu item
     * @param item
     */
    private void toggleMenuItemTitle(MenuItem item) {
        item.setTitle(showingMap ? R.string.action_view_as_list : R.string.action_view_as_map);
    }

    /**
     * Depending on the boolean showingMap, this method toggles the visibility of the list and map fragments
     */
    private void toggleListAndMapFragments() {
        Log.d(TAG, "toggleListAndMapFragments: Item count:" + this.suggestedItems.size());
        SuggestionsListFragment listFragment = (SuggestionsListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
        SuggestionsMapFragment mapFragment = (SuggestionsMapFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP);
        if (listFragment == null) {
            Log.w(TAG, "toggleListAndMapFragments: List fragment is null");
            return;
        }
        if (mapFragment == null) {
            Log.w(TAG, "toggleListAndMapFragments: Map fragment is null");
            return;
        }
        if (showingMap) {
            Log.d(TAG, "toggleListAndMapFragments: Showing map fragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(listFragment)
                    .show(mapFragment)
                    .commit();
            mapFragment.onLoadComplete(this.suggestedItems);
        }
        else {
            Log.d(TAG, "toggleListAndMapFragments: Showing list fragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(mapFragment)
                    .show(listFragment)
                    .commit();
            listFragment.onLoadComplete(this.suggestedItems);
        }
    }

    /**
     * SuggestionsLoaderCallbacks.SuggestionsLoaderListener callback
     * When the loader delivers the results, this method would be called.  Depending on which fragment is in view,
     * the data would be passed to the appropriate fragment.
     * @param loaderId
     * @param suggestedItems
     */
    @Override
    public void onLoadComplete(LoaderId loaderId, @Nullable ArrayList<YelpBusiness> suggestedItems) {
        if (loaderId != LoaderId.MULTI_PLACES) {
            Log.d(TAG, "onLoadComplete: Unknown loaderId:" + loaderId);
            return;
        }

        //debugging purposes
        if (suggestedItems == null) {
            Log.d(TAG, "onLoadComplete: SuggestedItems is null. Loader must be resetting");
        }
        else {
            Log.d(TAG, "onLoadComplete: Item count:" + suggestedItems.size());
        }

        this.suggestedItems = suggestedItems;
        if (showingMap) {
            Log.d(TAG, "onLoadComplete: Notifying List fragment");
            SuggestionsMapFragment mapFragment = (SuggestionsMapFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP);
            if (mapFragment != null) {
                mapFragment.onLoadComplete(suggestedItems);
            }
        }
        else {
            Log.d(TAG, "onLoadComplete: Notifying List fragment");
            SuggestionsListFragment listFragment = (SuggestionsListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_LIST);
            if (listFragment != null) {
                listFragment.onLoadComplete(suggestedItems);
            }
        }
    }
}
