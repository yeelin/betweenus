package com.example.yeelin.projects.betweenus.fragment;

import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.model.YelpResult;
import com.example.yeelin.projects.betweenus.model.YelpResultRegion;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by ninjakiki on 7/28/15.
 */
public class SuggestionsMapFragment
        extends BaseMapFragment
        implements SuggestionsCallbacks,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLoadedCallback,
        GoogleMap.OnMarkerClickListener {
    //logcat
    private static final String TAG = SuggestionsMapFragment.class.getCanonicalName();

    private static final int DEFAULT_ZOOM = 13;
    private static final float HUE_PRIMARY = 231f;
    private static final float HUE_ACCENT = 174f;

    //member variables
    private YelpResult yelpResult;

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

        //set the click listener for the marker
        googleMap.setOnMarkerClickListener(this);

        //set the click listener for the info window that is displayed when marker is tapped
        googleMap.setOnInfoWindowClickListener(this);

        //request for a callback when the map has finished rendering so that we can animate the camera
        googleMap.setOnMapLoadedCallback(this);
    }

    /**
     * The loader has finished fetching the data.  This method is called by SuggestionsActivity to update the view.
     * @param yelpResult
     */
    public void onLoadComplete(@Nullable YelpResult yelpResult) {
        //debugging purposes
        if (yelpResult == null) {
            Log.d(TAG, "onLoadComplete: SuggestedItems is null. Loader must be resetting");
        } else {
            Log.d(TAG, "onLoadComplete: Item count:" + yelpResult.getBusinesses().size());
        }

        //check if map is null
        if (map == null) {
            Log.d(TAG, "onLoadComplete: Map is null, so nothing to do");
            return;
        }

        //map is not null so update it
        updateMap(yelpResult);
    }

    /**
     * Updates the map with a new list of items
     * This is similar to what the SuggestionsAdapter does in updateAllItems().
     * @param newYelpResult
     */
    private void updateMap(@Nullable YelpResult newYelpResult) {
        //if it's the same items, do nothing. Otherwise, you end up clearing the map only to add
        //the same markers back
        if (yelpResult == newYelpResult) {
            Log.d(TAG, "updateMap: yelpResult == newYelpResult. Nothing to do");
            return;
        }

        //clear out the map first before adding new markers
        map.clear();
        //clear out the camera position too
        cameraPosition = null;

        //check if new suggestions are null
        if (newYelpResult != null) {
            this.yelpResult = newYelpResult;

            //check if new items > 0 so that we don't create the builder unnecessarily
            Log.d(TAG, "updateMap: Adding markers to map. Item count:" + newYelpResult.getBusinesses().size());
            if (newYelpResult.getBusinesses().size() > 0) {
                //LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                //loop through suggested items and add markers to map
                for (int i = 0; i < newYelpResult.getBusinesses().size(); i++) {
                    YelpBusiness business = newYelpResult.getBusinesses().get(i);

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(new LatLng(business.getLocation().getCoordinate().getLatitude(), business.getLocation().getCoordinate().getLongitude()))
                            .title(business.getName())
                            .snippet(getString(R.string.map_marker_snippet, business.getRating(), business.getReview_count()))
                            .icon(BitmapDescriptorFactory.defaultMarker(HUE_PRIMARY));
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

        if (cameraPosition == null && yelpResult != null) {
            //read the yelp result region
            YelpResultRegion yelpResultRegion = yelpResult.getRegion();
            LatLng center = new LatLng(
                    yelpResultRegion.getCenter().getLatitude(),
                    yelpResultRegion.getCenter().getLongitude());
            double latDelta = yelpResultRegion.getSpan().getLatitude_delta();
            double longDelta = yelpResultRegion.getSpan().getLongitude_delta();

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

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(HUE_ACCENT));
        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
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
}
