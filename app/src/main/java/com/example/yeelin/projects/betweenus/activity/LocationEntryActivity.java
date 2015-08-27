package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.example.yeelin.projects.betweenus.BuildConfig;
import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.LocationEntryFragment;
import com.example.yeelin.projects.betweenus.receiver.PlacesBroadcastReceiver;
import com.example.yeelin.projects.betweenus.service.PlacesService;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.google.android.gms.common.ConnectionResult;

public class LocationEntryActivity
        extends BasePlayServicesActivity
        implements LocationEntryFragment.LocationEntryFragmentListener,
        PlacesBroadcastReceiver.PlacesConnectionBroadcastListener {

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

        //turn on strict mode
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }

        setContentView(R.layout.activity_location_entry);
        //setup toolbar
        setupToolbar(R.id.locationEntry_toolbar, false);

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
            Log.d(TAG, "onCreate: Saved instance state is not null");
        }
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

    /** Create a broadcast receiver and register for connection broadcasts (success and failures)
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Registering for broadcasts");
        placesBroadcastReceiver = new PlacesBroadcastReceiver(this, this); //we are a connection broadcast listener
    }

    /**
     * Unregister for connection broadcasts
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Unregistering for broadcasts");
        placesBroadcastReceiver.unregister();
        super.onPause();
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
            String placeId = data.getStringExtra(LocationSearchActivity.EXTRA_PLACE_ID);
            String description = data.getStringExtra(LocationSearchActivity.EXTRA_PLACE_DESC);

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
}
