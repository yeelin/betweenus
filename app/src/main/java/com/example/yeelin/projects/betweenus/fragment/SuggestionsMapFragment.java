package com.example.yeelin.projects.betweenus.fragment;

import android.location.Location;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ninjakiki on 7/28/15.
 */
public class SuggestionsMapFragment
        extends BaseMapFragment
        implements SuggestionsCallbacks,
        GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapLoadedCallback {
    //logcat
    private static final String TAG = SuggestionsMapFragment.class.getCanonicalName();

    private static final int DEFAULT_ZOOM = 13;

    //member variables
    private List<YelpBusiness> items;

    /**
     * Creates a new instance of the map fragment
     * @return
     */
    public static SuggestionsMapFragment newInstance() {
        return new SuggestionsMapFragment();
    }

    /**
     * Required public empty constructor
     */
    public SuggestionsMapFragment() { }

    /**
     * Set up the click listener for the info window that is displayed when marker is tapped
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        super.onMapReady(googleMap);

        //set the click listener for the info window that is displayed when marker is tapped
        googleMap.setOnInfoWindowClickListener(this);

        //request for a callback when the map has finished rendering so that we can animate the camera
        googleMap.setOnMapLoadedCallback(this);
    }

    /**
     * GoogleMap.OnInfoWindowClickListener callback
     * Handles what happens when the info window is tapped
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, "onInfoWindowClick: Marker title:" + marker.getTitle());
        //TODO: call listener to start the detail activity
    }

    /**
     * The loader has finished fetching the data.  This method is called by SuggestionsActivity to update the view.
     * @param suggestedItems
     */
    public void onLoadComplete(@Nullable ArrayList<YelpBusiness> suggestedItems) {
        //debugging purposes
        if (suggestedItems == null) {
            Log.d(TAG, "onLoadComplete: SuggestedItems is null. Loader must be resetting");
        } else {
            Log.d(TAG, "onLoadComplete: Item count:" + suggestedItems.size());
        }

        //check if map is null
        if (map == null) {
            Log.d(TAG, "onLoadComplete: Map is null, so nothing to do");
            return;
        }

        //map is not null so update it
        updateMap(suggestedItems);
    }

    /**
     * Updates the map with a new list of items
     * This is similar to what the SuggestionsAdapter does in updateAllItems().
     * @param newItems
     */
    private void updateMap(@Nullable ArrayList<YelpBusiness> newItems) {
        //if it's the same items, do nothing. Otherwise, you end up clearing the map only to add
        //the same markers back
        if (items == newItems) {
            Log.d(TAG, "updateMap: items == suggestedItems. Nothing to do");
            return;
        }

        //clear out the map first before adding new markers
        map.clear();
        //clear out the camera position too
        cameraPosition = null;

        //check if new suggestions are null
        if (newItems != null) {
            this.items = newItems;

            //check if new items > 0 so that we don't create the builder unnecessarily
            Log.d(TAG, "updateMap: Adding markers to map. Item count:" + newItems.size());
            if (newItems.size() > 0) {
                //LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                //loop through suggested items and add markers to map
                for (int i = 0; i < newItems.size(); i++) {
                    YelpBusiness business = newItems.get(i);

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(new LatLng(business.getLocation().getCoordinate().getLatitude(), business.getLocation().getCoordinate().getLongitude()))
                            .title(business.getName())
                            .snippet(getString(R.string.map_marker_snippet, business.getRating(), business.getReview_count()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    map.addMarker(markerOptions);

                    //boundsBuilder.include(markerOptions.getPosition());
                }
                //bounds = boundsBuilder.build();
            }

            //set the camera to the user's location first so that we can then
            Location userLocation = map.getMyLocation();
            if (userLocation != null) {
                Log.d(TAG, "updateMap: userLocation != null");
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), DEFAULT_ZOOM);
                map.moveCamera(cameraUpdate);
            }
        }
    }

    /**
     *
     */
    @Override
    public void onMapLoaded() {
        Log.d(TAG, "onMapLoaded");
        //Note: bounds doesn't works work since some markers may end up on the extreme edge of the map
//        if (bounds != null) {
//            Log.d(TAG, "onMapLoaded: Bounds != null");
//            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
//        }

        if (cameraPosition == null) {
            //TODO: remove hard coded values after we plumb the yelp result region all the way through
            LatLng center = new LatLng(47.726622, -122.269217);
            double latDelta = 0.024100;
            double longDelta = 0.056876;

            //compute north, south, east, west from center
            LatLng north = new LatLng(center.latitude + latDelta/2, center.longitude);
            LatLng south = new LatLng(center.latitude - latDelta/2, center.longitude);
            LatLng east = new LatLng(center.latitude, center.longitude + longDelta/2);
            LatLng west = new LatLng(center.latitude, center.longitude - longDelta/2);

            //compute ne and sw from center
            LatLng ne = new LatLng(north.latitude, east.longitude);
            LatLng sw = new LatLng(south.latitude, west.longitude);

            //create bounds object
            LatLngBounds bounds = new LatLngBounds(sw, ne);
            //animate the camera over to the region specified by bounds
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
        }
    }
}
