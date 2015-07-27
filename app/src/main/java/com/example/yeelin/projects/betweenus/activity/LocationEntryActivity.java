package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.example.yeelin.projects.betweenus.BuildConfig;
import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.LocationEntryFragment;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;

public class LocationEntryActivity
        extends BaseActivity
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
            Location location = data.getParcelableExtra(LocationSearchActivity.EXTRA_LOCATION);

            LocationEntryFragment locationEntryFragment = (LocationEntryFragment) getSupportFragmentManager().findFragmentById(R.id.locationEntry_fragmentContainer);
            if (requestCode == REQUEST_CODE_USER_LOCATION) {
                locationEntryFragment.setUserLocation(LocationUtils.USER_LOCATION, location);
            }
            else {
                locationEntryFragment.setUserLocation(LocationUtils.FRIEND_LOCATION, location);
            }
        }
    }

    /**
     * LocationEntryFragment.LocationEntryFragmentListener callback
     * Start the suggested places activity
     *
     * @param searchTerm
     * @param userLocation
     * @param friendLocation
     */
    @Override
    public void onSearch(String searchTerm, Location userLocation, Location friendLocation) {
        Log.d(TAG, String.format("onSearch: User location:%s, Friend location:%s", userLocation.toString(), friendLocation.toString()));

        //start the suggestions list activity
        startActivity(SuggestionsActivity.buildIntent(this, DEFAULT_SEARCH_TERM, userLocation, friendLocation));
    }
}
