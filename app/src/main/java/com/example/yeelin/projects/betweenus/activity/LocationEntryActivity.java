package com.example.yeelin.projects.betweenus.activity;

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
     * LocationEntryFragment.LocationEntryFragmentListener callback
     *
     * @param userLocation
     * @param friendLocation
     */
    @Override
    public void onSearch(String userLocation, String friendLocation) {
        Log.d(TAG, String.format("onSearch: Location 1: %s, Location 2: %s", userLocation, friendLocation));

        //start the place suggestion activity
        startActivity(PlaceActivity.buildIntent(this, 0, "Place name"));
    }
}
