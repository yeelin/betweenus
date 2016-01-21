package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.adapter.DrawerAdapter;
import com.example.yeelin.projects.betweenus.analytics.EventConstants;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.fragment.LocationEntryFragment;
import com.example.yeelin.projects.betweenus.fragment.SettingsFragment;
import com.example.yeelin.projects.betweenus.receiver.PlacesBroadcastReceiver;
import com.example.yeelin.projects.betweenus.service.PlacesService;
import com.example.yeelin.projects.betweenus.utils.AnimationUtils;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.example.yeelin.projects.betweenus.utils.PreferenceUtils;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.common.ConnectionResult;

import org.json.JSONObject;

public class LocationEntryActivity
        extends BasePlayServicesActivity
        implements LocationEntryFragment.LocationEntryFragmentListener,
        PlacesBroadcastReceiver.PlacesConnectionBroadcastListener {
//        AdapterView.OnItemClickListener {

    //logcat
    private static final String TAG = LocationEntryActivity.class.getCanonicalName();

    //save instance state
    private static final String STATE_SELECTED_ITEM_ID = LocationEntryActivity.class.getSimpleName() + ".selectedItemId";

    //request codes
    private static final int REQUEST_CODE_USER_LOCATION = 100;
    private static final int REQUEST_CODE_FRIEND_LOCATION = 110;

    //constants
//    private static final int SEARCH = 1;
//    private static final int LOGIN = 2;
//    private static final int PREFERENCES = 3;

    //member variables
    private PlacesBroadcastReceiver placesBroadcastReceiver;
    private String userPlaceId;
    private String friendPlaceId;

    //drawer-related
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
//    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    //private int selectedDrawerPosition = SEARCH;
    private int selectedItemId = R.id.nav_search;

    //drawer header
    private ProfilePictureView userProfilePic;
    private TextView userName;

    //facebook login constants
    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String PICTURE = "picture";
    private static final String FIELDS = "fields";
    private static final String REQUEST_FIELDS = TextUtils.join(",", new String[]{ID, NAME, PICTURE});

    //member variables
    private JSONObject user;

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

        //ensure app is properly initialized with default settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false); //false = this method can be safely called without overriding user's saved prefs

        //initialize the fb sdk
        FacebookSdk.sdkInitialize(getApplicationContext());

        //strict mode
        //turn this off if testing for map performance, otherwise things become very slow
//        if (BuildConfig.DEBUG) StrictMode.enableDefaults();

        setContentView(R.layout.activity_location_entry);
        //setup toolbar
        setupToolbar(R.id.locationEntry_toolbar, false);
        //setup navigation drawer
//        setupDrawer();
        setupDrawerAndNavigationView();

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
        }
        else {
            //read last drawer position from savedInstanceState
            //selectedDrawerPosition = savedInstanceState.getInt(STATE_SELECTED_DRAWER_POSITION);
            selectedItemId = savedInstanceState.getInt(STATE_SELECTED_ITEM_ID, selectedItemId);
        }
//        selectDrawerItem(selectedDrawerPosition);
        navigationView.setCheckedItem(selectedItemId);
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
     * Sets up the drawer layout and new navigation view
     */
    private void setupDrawerAndNavigationView() {
        //set refs to toolbar, drawer layout and drawer view
        toolbar = (Toolbar) findViewById(R.id.locationEntry_toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        //enable action bar app icon to behave as toggle for nav drawer
        if (getSupportActionBar() != null) {
            //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        //listen for drawer open and close events
        //prefer using drawer toggle to setting a drawable as the home button because the latter has no animation
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close); //{
//            public void onDrawerClosed(View view) {
//                Log.d(TAG, "onDrawerClosed");
//            }
//            public void onDrawerOpened(View view) {
//                Log.d(TAG, "onDrawerOpened");
//                //fetch user login info from fb, on completion update userProfilePic and userName
//                //fetchUserInfo();
//            }
        //};
        drawerLayout.setDrawerListener(drawerToggle);

        //setup drawer content
        if (navigationView != null) {
            setupDrawerContent();
        }
    }

    /**
     * Sets up the drawer content using the new navigation view
     */
    private void setupDrawerContent() {
        //setup profile pic and user name in header of navigation view
        View headerView = navigationView.getHeaderView(0); //we have only 1 header
        userProfilePic = (ProfilePictureView) headerView.findViewById(R.id.drawer_user_profile_pic);
        userName = (TextView) headerView.findViewById(R.id.drawer_user_name);

        //fetch user login info from fb, on completion update userProfilePic and userName
        Log.d(TAG, "setupDrawerContent: Calling fetchUserInfo");
        fetchUserInfo();

        //setup item selection listener on navigation view
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);
                drawerLayout.closeDrawers();

                int id = item.getItemId();
                if (id == selectedItemId) {
                    Log.d(TAG, "onNavigationItemSelected: Same item selected, nothing to do");
                    return true;
                }

                selectedItemId = id;

                switch (id) {
                    case R.id.nav_search:
                        Log.d(TAG, "onNavigationItemSelected: Opening location entry");
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.locationEntry_fragmentContainer, LocationEntryFragment.newInstance())
                                .commit();
                        break;

                    case R.id.nav_login:
                        Log.d(TAG, "onNavigationItemSelected: Starting login activity");
                        startActivity(LoginActivity.buildIntent(LocationEntryActivity.this));
                        break;

                    case R.id.nav_settings:
                        Log.d(TAG, "onNavigationItemSelected: Opening settings");
                        getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.locationEntry_fragmentContainer, SettingsFragment.newInstance())
                                .commit();
                        //set the title to Settings
                        getSupportActionBar().setTitle(item.getTitle());
                        break;

                    default:
                        return true;
                }
                return true;
            }
        });
    }

//    /**
//     * Helper method to set up the navigation drawer layout and listview
//     */
//    private void setupDrawer() {
//        //set refs to toolbar and drawer layout
//        toolbar = (Toolbar) findViewById(R.id.locationEntry_toolbar);
//        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//
//        //enable action bar app icon to behave as toggle for nav drawer
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setHomeButtonEnabled(true);
//        }
//
//        //listen for drawer open and close events
//        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
//            public void onDrawerClosed(View view) {
//                Log.d(TAG, "onDrawerClosed");
//            }
//            public void onDrawerOpened(View view) {
//                Log.d(TAG, "onDrawerOpened");
//                fetchUserInfo();
//                updateUI();
//            }
//        };
//        drawerLayout.setDrawerListener(drawerToggle);
//
//        //set up the list for the drawer
//        setupDrawerList();
//    }

//    /**
//     * Helper method to set up the navigation drawer list view
//     */
//    private void setupDrawerList() {
//        //set reference to drawer's listview
//        drawerList = (ListView) findViewById(R.id.drawer_listView);
//
//        //setup list header
//        View header = LayoutInflater.from(this).inflate(R.layout.drawer_list_header, drawerList, false);
//        userProfilePic = (ProfilePictureView) header.findViewById(R.id.drawer_user_profile_pic);
//        userName = (TextView) header.findViewById(R.id.drawer_user_name);
//        drawerList.addHeaderView(header, null, false);
//
//        //set the adapter for the listview
//        drawerList.setAdapter(new DrawerAdapter(this));
//        //set the list view's click listener
//        drawerList.setOnItemClickListener(this);
//    }

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
     * Save the selected drawer position in case of configuration change.
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_ITEM_ID, selectedItemId);
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
     * Pass any config change to drawer toggle
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
        Log.d(TAG, String.format("onSearch: SearchTerm:%s, User PlaceId:%s, Friend PlaceId:%s", searchTerm, userPlaceId, friendPlaceId));

        //if the user wants to use FB as the data source (from settings) but hasn't logged in, prompt them to log in
        if (PreferenceUtils.getPreferredDataSource(this) == LocalConstants.FACEBOOK &&
                AccessToken.getCurrentAccessToken() == null) {
            Log.d(TAG, "onSearch: Fb is preferred data source but user is not logged in");

            View parent = findViewById(R.id.locationEntry_content); //parent == coordinator layout
            Snackbar.make(parent, R.string.snackbar_not_logged_in, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.snackbar_login, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(LoginActivity.buildIntent(LocationEntryActivity.this));
                        }
                    })
                    .show();
            return;
        }

        //log user launching search
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        logger.logEvent(EventConstants.EVENT_NAME_SEARCH);

        //start the suggestions activity that will host the list and map
        startActivity(SuggestionsActivity.buildIntent(this, searchTerm, userPlaceId, friendPlaceId));
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

//    /**
//     * AdapterView.OnItemClickListener implementation
//     * Handles clicks on items in the drawer list
//     * @param parent
//     * @param view
//     * @param position
//     * @param id
//     */
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Log.d(TAG, "onItemClick: Position:" + position);
//        selectDrawerItem(position);
//    }

//    /**
//     * Helper method to handle the selection of a drawer item
//     * @param position
//     */
//    private void selectDrawerItem(int position) {
//        //update selected item and then close the drawer
//        drawerList.setItemChecked(position, true);
//        drawerLayout.closeDrawer(drawerList);
//
//        if (selectedDrawerPosition == position) {
//            Log.d(TAG, "selectDrawerItem: Same position selected, nothing to do");
//            return;
//        }
//
//        selectedDrawerPosition = position;
//
//        switch (position) {
//            case SEARCH:
//                Log.d(TAG, "selectDrawerItem: Opening location entry");
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.locationEntry_fragmentContainer, LocationEntryFragment.newInstance())
//                        .commit();
//                break;
//
//            case LOGIN:
//                Log.d(TAG, "selectDrawerItem: Starting login activity");
//                startActivity(LoginActivity.buildIntent(this));
//                break;
//
//            case PREFERENCES:
//                Log.d(TAG, "selectDrawerItem: Opening preferences");
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.locationEntry_fragmentContainer, SettingsFragment.newInstance())
//                        .commit();
//                break;
//        }
//    }

    /**
     * Fetches the user info (profile pic and name) from Facebook
     */
    private void fetchUserInfo() {
        //check if we have an access token
        if (AccessToken.getCurrentAccessToken() == null) {
            //no, user is not logged in
            Log.d(TAG, "fetchUserInfo: User is not logged in");
            //TODO: ask user to login?
            return;
        }

        //yes, user is logged in
//        Log.d(TAG, "fetchUserInfo: User is logged in");

        //create the graph request
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        user = object;
                        Log.d(TAG, "onCompleted: Calling updateDrawerHeader");
                        updateDrawerHeader();
                    }
                });

        //create the parameters for the request
        Bundle parameters = new Bundle();
        parameters.putString(FIELDS, REQUEST_FIELDS);
        request.setParameters(parameters);
        GraphRequest.executeBatchAsync(request);
    }

    /**
     * Updates the UI with the profile pic and name.
     */
    private void updateDrawerHeader() {
        if (AccessToken.getCurrentAccessToken() == null || user == null) {
            //no, user is not logged in
            Log.d(TAG, "updateDrawerHeader: User is not logged in");
            userProfilePic.setProfileId(null);
            userName.setVisibility(View.INVISIBLE);
            return;
        }

        //yes, user is logged in
//        Log.d(TAG, "updateDrawerHeader: User is logged in");

        //read the Graph API Json result
        String id = user.optString(ID);
        String name = user.optString(NAME);
        String picture = user.optString(PICTURE);
//        Log.d(TAG, String.format("updateDrawerHeader: Id:%s, Name:%s, Pic:%s", id, name, picture));

        //set the user's name
        userName.setText(user.optString(NAME));
        if (userName.getVisibility() == View.INVISIBLE) {
//            Log.d(TAG, "updateDrawerHeader: Fading in the username");
            AnimationUtils.fadeInView(this, userName);
        }

        //set the user's profile picture
        final Profile profile = Profile.getCurrentProfile();
        if (profile == null) {
            userProfilePic.setProfileId(null);
            return;
        }

        Log.d(TAG, String.format("updateDrawerHeader: Id:%s, First:%s, Middle:%s, Last:%s, Name:%s, Link:%s",
                profile.getId(), profile.getFirstName(), profile.getMiddleName(), profile.getLastName(), profile.getName(), profile.getLinkUri().toString()));
        userProfilePic.setProfileId(profile.getId());
    }
}
