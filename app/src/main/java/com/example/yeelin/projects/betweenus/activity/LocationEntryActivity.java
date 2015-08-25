package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.example.yeelin.projects.betweenus.BuildConfig;
import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.LocationEntryFragment;
import com.example.yeelin.projects.betweenus.service.PlacesFetchService;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;

public class LocationEntryActivity
        extends BasePlayServicesActivity
        implements LocationEntryFragment.LocationEntryFragmentListener {

    //logcat
    private static final String TAG = LocationEntryActivity.class.getCanonicalName();

    //request codes
    private static final int REQUEST_CODE_USER_LOCATION = 100;
    private static final int REQUEST_CODE_FRIEND_LOCATION = 110;

    //TODO: remove hardcoded search term
    public static final String DEFAULT_SEARCH_TERM = "restaurants";

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
     * Destroy the PlacesFetchService since we are going away.
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Destroying PlacesFetchService");
        stopService(PlacesFetchService.buildPlaceDetailsStopFetchIntent(this));
        super.onDestroy();
    }

    /**
     * Starts the Search activity to acquire either the user or friend's location
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
     * Inspect the result from SearchActivity and set the location for user or friend.
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
     * LocationEntryFragment.LocationEntryFragmentListener callback
     * Start the suggested places activity
     *
     * @param searchTerm
     * @param userPlaceId
     * @param friendPlaceId
     */
    @Override
    public void onSearch(String searchTerm, String userPlaceId, String friendPlaceId) {
        Log.d(TAG, String.format("onSearch: User PlaceId:%s, Friend PlaceId:%s", userPlaceId, friendPlaceId));

        //start the suggestions list activity
        startActivity(SuggestionsActivity.buildIntent(this, DEFAULT_SEARCH_TERM, userPlaceId, friendPlaceId));
    }
}
