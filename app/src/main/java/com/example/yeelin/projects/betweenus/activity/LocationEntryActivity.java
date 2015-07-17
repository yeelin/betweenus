package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.example.yeelin.projects.betweenus.BuildConfig;
import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.LocationEntryFragment;

public class LocationEntryActivity
        extends BaseActivity
        implements LocationEntryFragment.LocationEntryFragmentListener {

    //logcat
    private static final String TAG = LocationEntryActivity.class.getCanonicalName();

    //request codes
    private static final int REQUEST_CODE_USER_LOCATION = 100;
    private static final int REQUEST_CODE_FRIEND_LOCATION = 110;
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

    @Override
    public void inputUserLocation() {
        Log.d(TAG, "inputUserLocation");
        startActivityForResult(
                SearchActivity.buildIntent(this, SearchActivity.USER),
                REQUEST_CODE_USER_LOCATION);

    }

    @Override
    public void inputFriendLocation() {
        Log.d(TAG, "inputFriendLocation");
        startActivityForResult(
                SearchActivity.buildIntent(this, SearchActivity.FRIEND),
                REQUEST_CODE_FRIEND_LOCATION);
    }

    /**
     *
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
            String locationName = data.getStringExtra(SearchActivity.EXTRA_LOCATION_NAME);
            double latitude = data.getDoubleExtra(SearchActivity.EXTRA_LOCATION_LATITUDE, 0);
            double longitude = data.getDoubleExtra(SearchActivity.EXTRA_LOCATION_LONGITUDE, 0);

            LocationEntryFragment locationEntryFragment = (LocationEntryFragment) getSupportFragmentManager().findFragmentById(R.id.locationEntry_fragmentContainer);
            if (requestCode == REQUEST_CODE_USER_LOCATION) {
                locationEntryFragment.setUserLocation(locationName, latitude, longitude);
            }
            else {
                locationEntryFragment.setFriendLocation(locationName, latitude, longitude);
            }
        }
    }

    /**
     * LocationEntryFragment.LocationEntryFragmentListener callback
     *
     * @param userLocation
     * @param friendLocation
     */
    @Override
    public void onSearch(String userLocation, String friendLocation) {
        Log.d(TAG, String.format("onSearch: User location:%s, Friend location:%s", userLocation, friendLocation));

        //start the place suggestion activity
        startActivity(PlaceActivity.buildIntent(this, 0, "Place name"));
    }
}
