package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.adapter.MapItemInfoWindowAdapter;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.model.YelpResult;
import com.example.yeelin.projects.betweenus.model.YelpResultRegion;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Target;

/**
 * Created by ninjakiki on 7/28/15.
 */
public class SuggestionsMapFragment
        extends BaseMapFragment
        implements OnSuggestionsLoadedCallback, //tells fragment when data is loaded
        OnSelectionChangedCallback, //tells fragment when selections have changed
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLoadedCallback,
        GoogleMap.OnMarkerClickListener {

    //logcat
    private static final String TAG = SuggestionsMapFragment.class.getCanonicalName();

    //map configurations
    private static final int DEFAULT_ZOOM = 13; //default zoom when user location is displayed
    private static final int PADDING_BOUNDING_BOX = 50; //space (in px) to leave between the bounding box edges and the view edges. This value is applied to all four sides of the bounding box.

    //marker colors
    private static final float HUE_PRIMARY = 231f;
    private static final float HUE_ACCENT = 174f;

    //member variables
    private YelpResult result;
    private boolean mapNeedsUpdate = false;

    private ArrayMap<String, String> selectedIdsMap;
    private SimpleArrayMap<Marker, String> markerToIdMap = new SimpleArrayMap<>();
    private SimpleArrayMap<String, String> idToRatingUrlMap = new SimpleArrayMap<>();

    private OnSuggestionActionListener suggestionActionListener;

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
     * Make sure the parent fragment or activity implements the suggestion click listener
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : activity;
        try {
            suggestionActionListener = (OnSuggestionActionListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement OnSuggestionActionListener");
        }
    }

    /**
     * Nullify the click listener
     */
    @Override
    public void onDetach() {
        suggestionActionListener = null;
        super.onDetach();
    }

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

        //set a custom renderer for the contents of info windows.
        googleMap.setInfoWindowAdapter(new MapItemInfoWindowAdapter(getActivity(), markerToIdMap, idToRatingUrlMap));

        //request for a callback when the map has finished rendering so that we can animate the camera
        googleMap.setOnMapLoadedCallback(this);

        //if the results had come in first before the map was ready and the map needs updating immediately, then do it.
        // otherwise, when the results come in, onSuggestionsLoaded will be called and the map will be updated the usual way
        if (mapNeedsUpdate) {
            updateMap();
        }
    }

    /**
     * OnSuggestionsLoadedCallback implementation
     * The loader has finished fetching the data.  This method is called by SuggestionsActivity to update the view.
     * @param result
     * @param selectedIdsMap
     */
    public void onSuggestionsLoaded(@Nullable YelpResult result, @NonNull ArrayMap<String,String> selectedIdsMap) {
        Log.d(TAG, "onSuggestionsLoaded");
        this.selectedIdsMap = selectedIdsMap;

        //if it's the same items, do nothing. Otherwise, you end up clearing the map only to add
        //the same markers back
        if (this.result == result) {
            Log.d(TAG, "updateMap: this.result == result. Nothing to do");
            return;
        }
        else {
            //result is different so save a reference to it
            this.result = result;
        }

        //check if map is null
        if (map == null) {
            Log.d(TAG, "onSuggestionsLoaded: Map is null, so nothing to do now");
            mapNeedsUpdate = true; //tell ourselves that we need to update the map later when it is ready
            return;
        }

        //map is not null and the result is different so update the map
        updateMap();
    }

    /**
     * Updates the map with the result
     * This is similar to what the SuggestionsAdapter does in updateAllItems().
     */
    private void updateMap() {
        Log.d(TAG, "updateMap");

        //clear out the map first before adding new markers
        map.clear();
        //clear out the camera position too
        cameraPosition = null;
        //clear out the markerToId and idToRatingUrl maps as well
        markerToIdMap.clear();
        idToRatingUrlMap.clear();

        //check if result is null
        if (result != null && result.getBusinesses().size() > 0) {
            Log.d(TAG, "updateMap: Adding markers to map. Item count:" + result.getBusinesses().size());

            //make sure the markerToId map can hold all the markers we are about to add
            markerToIdMap.ensureCapacity(result.getBusinesses().size());
            idToRatingUrlMap.ensureCapacity(result.getBusinesses().size());
            addMarkersToMap();

            //set the camera to the user's location first so that we can later animate to result region
            Location userLocation = map.getMyLocation();
            if (userLocation != null) {
                Log.d(TAG, "updateMap: userLocation != null");
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), DEFAULT_ZOOM);
                map.moveCamera(cameraUpdate);
            }
        }

        //we have updated the map, so set this to false
        mapNeedsUpdate = false;
    }

    /**
     * Adds markers to the map.
     * Also saves marker to business id mapping, and business id to rating url mapping.
     */
    private void addMarkersToMap() {
        //LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        //loop through suggested items and:
        //1. place markers on the map
        //2. add markers to the markerToId map
        for (int i=0; i<result.getBusinesses().size(); i++) {
            YelpBusiness business = result.getBusinesses().get(i);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(business.getLocation().getCoordinate().getLatitude(), business.getLocation().getCoordinate().getLongitude()))
                    .title(business.getName())
                    .snippet(getString(R.string.review_count, business.getReview_count()))
                    .icon(BitmapDescriptorFactory.defaultMarker(HUE_PRIMARY));
            Marker marker = map.addMarker(markerOptions);

            markerToIdMap.put(marker, business.getId());
            idToRatingUrlMap.put(business.getId(), business.getRating_img_url_large());
            //boundsBuilder.include(markerOptions.getPosition());
        }
        //bounds = boundsBuilder.build();
    }

    /**
     * TODO: Implement onSelectionChanged
     */
    @Override
    public void onSelectionChanged() {
        Log.d(TAG, "onSelectionChanged");
    }

    /**
     * GoogleMap.OnMapLoadedCallback implementation
     * This method is called when the map has finished rendering.  Animate the camera to the bounds computed
     * from the yelp result.
     */
    @Override
    public void onMapLoaded() {
        Log.d(TAG, "onMapLoaded");
        //Note: bounds doesn't works work since some markers may end up on the extreme edge of the map
//        if (bounds != null) {
//            Log.d(TAG, "onMapLoaded: Bounds != null");
//            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));
//        }

        if (cameraPosition == null && result != null) {
            //read the yelp result region so that we can specify the map bounds
            LatLngBounds mapBounds = computeMapBoundsFromResult();
            //animate the camera over to the region specified by bounds
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, PADDING_BOUNDING_BOX));
        }
    }

    /**
     * Helper method that read the yelp result region and returns them as bounds
     * for the map
     * @return
     */
    private LatLngBounds computeMapBoundsFromResult() {
        //read the yelp result region so that we can specify the map bounds
        YelpResultRegion resultRegion = result.getRegion();

        //get region center
        LatLng center = new LatLng(
                resultRegion.getCenter().getLatitude(),
                resultRegion.getCenter().getLongitude());
        //get delta of lat/long from the center
        double latDelta = resultRegion.getSpan().getLatitude_delta();
        double longDelta = resultRegion.getSpan().getLongitude_delta();

        //compute north, south, east, west from center
        LatLng north = new LatLng(center.latitude + latDelta/2, center.longitude);
        LatLng south = new LatLng(center.latitude - latDelta/2, center.longitude);
        LatLng east = new LatLng(center.latitude, center.longitude + longDelta/2);
        LatLng west = new LatLng(center.latitude, center.longitude - longDelta/2);

        //compute ne and sw from center
        LatLng ne = new LatLng(north.latitude, east.longitude);
        LatLng sw = new LatLng(south.latitude, west.longitude);

        //create bounds object
        return new LatLngBounds(sw, ne);
    }

    /**
     * GoogleMap.OnMarkerClickListener callback
     * Handles what happens when the marker is tapped
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick");
        //marker.setIcon(BitmapDescriptorFactory.defaultMarker(HUE_ACCENT));
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
        //get the business id corresponding to the clicked marker
        String businessId = markerToIdMap.get(marker);
        Log.d(TAG, String.format("onInfoWindowClick: Marker title:%s, BusinessId:%s", marker.getTitle(), businessId));

        //notify the activity that a suggestion was clicked
        suggestionActionListener.onSuggestionClick(businessId, marker.getTitle());
    }
}
