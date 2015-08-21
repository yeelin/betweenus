package com.example.yeelin.projects.betweenus.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.yeelin.projects.betweenus.BuildConfig;
import com.example.yeelin.projects.betweenus.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;

/**
 * Created by ninjakiki on 7/28/15.
 */
public abstract class BaseMapFragment
        extends SupportMapFragment
        implements OnMapReadyCallback {
    //logcat
    private static final String TAG = BaseMapFragment.class.getCanonicalName();

    //saved instance state
    private static final String STATE_CAMERA_POSITION = BaseMapFragment.class.getSimpleName() + ".cameraPosition";

    //member variables
    protected GoogleMap map;
    protected CameraPosition cameraPosition;

    /**
     * Required empty constructor
     */
    public BaseMapFragment() { }

    /**
     * Create the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            cameraPosition = savedInstanceState.getParcelable(STATE_CAMERA_POSITION);
        }
    }

    /**
     * Configure the view
     * Sets a callback object which will be triggered when the google map instance is ready to be used.
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    /**
     * Restore the map to the previous camera position if any
     */
    @Override
    public void onResume() {
        super.onResume();

        //restore the map to the previous camera position if any
        if (map != null && cameraPosition != null) {
            Log.d(TAG, "onResume: map != null and cameraPosition != null, so restoring map camera");
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    /**
     * Save the current camera position so that we can write it to saved instance state later
     */
    @Override
    public void onPause() {
        super.onPause();

        if (map != null) {
            cameraPosition = map.getCameraPosition();
        }
    }

    /**
     * Save the camera position to saved instance state
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (cameraPosition != null) {
            outState.putParcelable(STATE_CAMERA_POSITION, cameraPosition);
        }
    }

    /**
     * Nullify the google map reference
     */
    @Override
    public void onDestroyView() {
        map = null;
        super.onDestroyView();
    }

    /**
     * OnMapReadyCallback callback
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        map = googleMap;

        UiSettings mapUiSettings = map.getUiSettings();
        if (mapUiSettings != null) {
            //allow user to access maps and navigation app from the map
            mapUiSettings.setMapToolbarEnabled(false);
            //add this only because we need to test on emulator
            if (BuildConfig.DEBUG) {
                mapUiSettings.setZoomControlsEnabled(true);
            }
        }

        //show user's current location on the map
        map.setMyLocationEnabled(true);

        //for accessibility
        map.setContentDescription(getString(R.string.map_contentDescription));
    }
}
