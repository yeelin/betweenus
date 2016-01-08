package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.adapter.DrawerAdapter;
import com.example.yeelin.projects.betweenus.analytics.EventConstants;
import com.example.yeelin.projects.betweenus.fragment.LocationEntryFragment;
import com.example.yeelin.projects.betweenus.receiver.PlacesBroadcastReceiver;
import com.example.yeelin.projects.betweenus.service.PlacesService;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;

public class LocationEntryActivity
        extends BasePlayServicesActivity
        implements LocationEntryFragment.LocationEntryFragmentListener,
        PlacesBroadcastReceiver.PlacesConnectionBroadcastListener,
        AdapterView.OnItemClickListener {

    //logcat
    private static final String TAG = LocationEntryActivity.class.getCanonicalName();

    //request codes
    private static final int REQUEST_CODE_USER_LOCATION = 100;
    private static final int REQUEST_CODE_FRIEND_LOCATION = 110;

    //constants
    //TODO: remove hardcoded search term
    public static final String DEFAULT_SEARCH_TERM = "restaurants";

    //member variables
    private PlacesBroadcastReceiver placesBroadcastReceiver;
    private String userPlaceId;
    private String friendPlaceId;

    //drawer-related
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    /**
     * Builds the appropriate intent to start this activity.
     * @param context
     * @return
     */
    public static Intent buildIntent(Context context) {
        Intent intent = new Intent(context, LocationEntryActivity.class);
        return intent;
    }

    /**
     * Creates the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize the fb sdk
        FacebookSdk.sdkInitialize(getApplicationContext());

        //strict mode
        //turn this off if testing for map performance, otherwise things become very slow
//        if (BuildConfig.DEBUG) StrictMode.enableDefaults();

        setContentView(R.layout.activity_location_entry);
        //setup toolbar
        setupToolbar(R.id.locationEntry_toolbar, false);
        //setup navigation drawer
        setupDrawer();

        //check if the fragment exists, otherwise, create it
        if (savedInstanceState == null) {
            Fragment locationEntryFragment = getSupportFragmentManager().findFragmentById(R.id.locationEntry_fragmentContainer);
            if (locationEntryFragment == null) {
                Log.d(TAG, "onCreate: Creating a new location entry fragment");
                //create a location fragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.locationEntry_fragmentContainer, LocationEntryFragment.newInstance())
                        .commit();
            }
            selectDrawerItem(0);
        }
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //sync toggle state
        drawerToggle.syncState();
    }

    /**
     * Helper method to set up the navigation drawer layout and listview
     */
    private void setupDrawer() {
        //set refs to toolbar and drawer layout
        toolbar = (Toolbar) findViewById(R.id.locationEntry_toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        //enable action bar app icon to behave as toggle for nav drawer
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //listen for drawer open and close events
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                Log.d(TAG, "onDrawerClosed");
            }
            public void onDrawerOpened(View view) {
                Log.d(TAG, "onDrawerOpened");
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        //set up the list for the drawer
        setupDrawerList();
    }

    /**
     * Helper method to set up the navigation drawer list view
     */
    private void setupDrawerList() {
        //set reference to drawer's listview
        drawerList = (ListView) findViewById(R.id.drawer_listView);
        //set the adapter for the listview
        drawerList.setAdapter(new DrawerAdapter(this));
        //set the list view's click listener
        drawerList.setOnItemClickListener(this);
    }

    /**
     * Start the PlacesService. More specifically, we want to call connect on the google api client
     * to reduce latency later
     */
    @Override
    protected void onStart() {
        super.onStart();
        //start the places service
        Log.d(TAG, "onStart: Starting PlacesService");
        startService(PlacesService.buildApiConnectIntent(this));
    }

    /**
     * Create a broadcast receiver and register for connection broadcasts (success and failures).
     * Log activation.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Registering for broadcasts");
        placesBroadcastReceiver = new PlacesBroadcastReceiver(this, this); //we are a connection broadcast listener

        //fb logs install and 'app activate' app events
        AppEventsLogger.activateApp(this);
    }

    /**
     * Unregister for connection broadcasts. Log deactivation.
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Unregistering for broadcasts");
        placesBroadcastReceiver.unregister();

        super.onPause();

        //fb logs 'app deactivate' app event
        AppEventsLogger.deactivateApp(this);
    }

    /**
     * Destroy the PlacesService since we are going away.
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Destroying PlacesService");
        stopService(PlacesService.buildStopServiceIntent(this));
        super.onDestroy();
    }

    /**
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //pass any config change to drawer toggle
        drawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * LocationEntryFragment.LocationEntryFragmentListener callback
     * The user wants to input a location. Starts the LocationSearchActivity to allow user to input
     * either the user's or friend's location
     * @param locationType
     */
    @Override
    public void onInputLocation(int locationType) {
        Log.d(TAG, "onInputLocation: Location type: " + locationType);

        switch (locationType) {
            case LocationUtils.USER_LOCATION:
                startActivityForResult(
                        LocationSearchActivity.buildIntent(this, LocationUtils.USER_LOCATION),
                        REQUEST_CODE_USER_LOCATION);
                break;

            case LocationUtils.FRIEND_LOCATION:
                startActivityForResult(
                        LocationSearchActivity.buildIntent(this, LocationUtils.FRIEND_LOCATION),
                        REQUEST_CODE_FRIEND_LOCATION);
                break;

            default:
                Log.d(TAG, "onInputLocation: Unknown location type: " + locationType);
        }
    }

    /**
     * LocationEntryFragment.LocationEntryFragmentListener callback
     * The user has entered both user and friend location and wants to see the list of suggestions.
     * Start the suggestions activity.
     *
     * @param searchTerm
     * @param userPlaceId
     * @param friendPlaceId
     */
    @Override
    public void onSearch(String searchTerm, String userPlaceId, String friendPlaceId) {
        Log.d(TAG, String.format("onSearch: User PlaceId:%s, Friend PlaceId:%s", userPlaceId, friendPlaceId));

        //log user launching search
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        logger.logEvent(EventConstants.EVENT_NAME_SEARCH);

        //start the suggestions activity that will host the list and map
        startActivity(SuggestionsActivity.buildIntent(this, DEFAULT_SEARCH_TERM, userPlaceId, friendPlaceId));
    }

    /**
     * Inspect the result from LocationSearchActivity and set the location for user or friend.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_CODE_USER_LOCATION && requestCode != REQUEST_CODE_FRIEND_LOCATION) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (resultCode == Activity.RESULT_OK && data != null) {
            final String placeId = data.getStringExtra(LocationSearchActivity.EXTRA_PLACE_ID);
            if (requestCode == REQUEST_CODE_USER_LOCATION)
                userPlaceId = placeId;
            else
                friendPlaceId = placeId;
            final String description = data.getStringExtra(LocationSearchActivity.EXTRA_PLACE_DESC);

            LocationEntryFragment locationEntryFragment = (LocationEntryFragment) getSupportFragmentManager().findFragmentById(R.id.locationEntry_fragmentContainer);
            locationEntryFragment.setUserLocation(
                    requestCode == REQUEST_CODE_USER_LOCATION ? LocationUtils.USER_LOCATION : LocationUtils.FRIEND_LOCATION,
                    placeId,
                    description);
        }
    }

    /**
     * PlacesBroadcastReceiver.PlacesConnectionBroadcastListener callback
     * We are now connected to google places api
     */
    @Override
    public void onConnectSuccess() {
        Log.d(TAG, "onConnectSuccess");
    }

    /**
     * PlacesBroadcastReceiver.PlacesConnectionBroadcastListener callback
     * We failed to connect to google places api.  Handle the error.
     * @param connectionResult
     * @param resolutionType
     */
    @Override
    public void onConnectFailure(ConnectionResult connectionResult, int resolutionType) {
        Log.d(TAG, "onConnectFailure");
        if (connectionResult.hasResolution()) {
            // request the user take immediate action to resolve the error
            try {
                Log.d(TAG, "onConnectFailure: startResolutionForResult");
                //check for the result in onActivityResult() in the activity class
                connectionResult.startResolutionForResult(this, REQUEST_CODE_PLAY_SERVICES_RESOLUTION);

            }
            catch (IntentSender.SendIntentException e) {
                Log.d(TAG, "onConnectFailure: Exception. Starting service again");
                //there was an error when attempting the resolution. try to reconnect one more time.
                startService(PlacesService.buildApiConnectIntent(this));
            }
        }
        else {
            Log.d(TAG, "onConnectFailure: No resolution so showing error dialog");
            // Show error dialog using GooglePlayServicesUtil.getErrorDialog()
            showPlayServicesErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Handles menu item click. Since we have just the home button and it serves
     * as the drawer toggle, we pass it to the drawer toggle.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // The action bar home/up action should open or close the drawer.
                // ActionBarDrawerToggle will take care of this.
                return drawerToggle.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * AdapterView.OnItemClickListener implementation
     * Handles clicks on items in the drawer list
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick: Position:" + position);
        selectDrawerItem(position);
    }

    /**
     * Helper method to handle the selection of a drawer item
     * @param position
     */
    private void selectDrawerItem(int position) {
        //update selected item and then close the drawer
        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);

        switch (position) {
            case 0:
                Log.d(TAG, "selectDrawerItem: Starting location entry activity");
                break;
            case 1:
                Log.d(TAG, "selectDrawerItem: Starting login activity");
                startActivity(LoginActivity.buildIntent(this));
                break;
            case 2:
                Log.d(TAG, "selectDrawerItem: Settings activity has not been implemented");
                break;
        }
    }
}
