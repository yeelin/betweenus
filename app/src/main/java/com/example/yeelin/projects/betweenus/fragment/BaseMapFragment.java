package com.example.yeelin.projects.betweenus.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

    //request code
    private static final int REQUEST_LOCATION_PERMISSION = 100;

    //saved instance state
    private static final String STATE_CAMERA_POSITION = BaseMapFragment.class.getSimpleName() + ".cameraPosition";

    //member variables
    protected GoogleMap map;
    protected CameraPosition cameraPosition;

    /**
     * Required empty constructor
     */
    public BaseMapFragment() {
    }

    /**
     * Create the fragment and restore any saved camera position
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate: Restoring camera position");
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
        Log.d(TAG, "onResume");
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
            Log.d(TAG, "onPause: Getting camera position");
            cameraPosition = map.getCameraPosition();
        }
    }

    /**
     * Save the camera position to saved instance state
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);

        if (cameraPosition != null) {
            Log.d(TAG, "onSaveInstanceState: Saving camera position");
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

        final UiSettings mapUiSettings = map.getUiSettings();
        if (mapUiSettings != null) {
            //do not allow user to access maps and navigation app from the map, the toolbar is distracting
            mapUiSettings.setMapToolbarEnabled(false);
            //add this only if we need to test on emulator
            //if (BuildConfig.DEBUG) mapUiSettings.setZoomControlsEnabled(true);
        }

        //show user's current location on the map
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.d(TAG, "onMapReady: shouldShowRequestPermissionRationale");
                //TODO: display ui and wait for user interaction
                Toast.makeText(getContext(),
                        getString(R.string.location_permission_rationale),
                        Toast.LENGTH_LONG)
                        .show();

                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
        }
        else {
            //permission has been granted, continue as usual
            Log.d(TAG, "onMapReady: Location permission was previously granted");
            map.setMyLocationEnabled(true);
        }

        //for accessibility
        map.setContentDescription(getString(R.string.map_contentDescription));
    }

    /**
     * Handle the user's response to request for location permission
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_LOCATION_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length == 2 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            //permission has been granted, continue as usual
            Log.d(TAG, "onRequestPermissionsResult: Location permission has been granted");
            try {
                map.setMyLocationEnabled(true);
            }
            catch (SecurityException e) {
                Log.d(TAG, "onRequestPermissionsResult: Unexpected security exception: " + e.getMessage());
            }
        }
        else {
            Log.d(TAG, "onRequestPermissionsResult: Location permission was denied");
        }
    }
}
