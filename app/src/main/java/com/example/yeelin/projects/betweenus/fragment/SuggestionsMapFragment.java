package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.adapter.MapItemInfoWindowAdapter;

import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.model.YelpResult;
import com.example.yeelin.projects.betweenus.model.YelpResultRegion;
import com.example.yeelin.projects.betweenus.utils.MapColorUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

/**
 * Created by ninjakiki on 7/28/15.
 * TODO: Need to include map attribution in about page
 * GoogleApiAvailability.getOpenSourceSoftwareLicenseInfo.
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

    //member variables
    private MapItemInfoWindowAdapter infoWindowAdapter; //custom renderer for info windows
    private YelpResult result;
    private boolean mapNeedsUpdate = false;

    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    private ArrayMap<String, Integer> selectedIdsMap;
    private SimpleArrayMap<String, Marker> idToMarkerMap = new SimpleArrayMap<>(); //for changing the selection state of the marker when onSelectionChanged event occurs
    private SimpleArrayMap<Marker, Pair<String, Integer>> markerToIdPositionPairMap = new SimpleArrayMap<>(); //for getting the business id and position corresponding to the clicked marker
    private SimpleArrayMap<String, String> idToRatingUrlMap = new SimpleArrayMap<>(); //for getting the rating image url corresponding to the clicked marker
    private Marker userLocationMarker; //for showing user's location marker
    private Marker friendLocationMarker; //for showing friend's location marker

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
        infoWindowAdapter = new MapItemInfoWindowAdapter(getActivity(), markerToIdPositionPairMap, idToRatingUrlMap);
        googleMap.setInfoWindowAdapter(infoWindowAdapter);

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
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    public void onSuggestionsLoaded(@Nullable YelpResult result, @NonNull ArrayMap<String,Integer> selectedIdsMap,
                                    LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
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
            this.userLatLng = userLatLng;
            this.friendLatLng = friendLatLng;
            this.midLatLng = midLatLng;
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
        markerToIdPositionPairMap.clear();
        idToMarkerMap.clear();
        idToRatingUrlMap.clear();
        //clear out any user/friend markers as well
        if (userLocationMarker != null) {
            userLocationMarker.remove();
            userLocationMarker = null;
        }
        if (friendLocationMarker != null) {
            friendLocationMarker.remove();
            friendLocationMarker = null;
        }

        //check if result is null
        if (result != null && result.getBusinesses().size() > 0) {
            Log.d(TAG, "updateMap: Adding markers to map. Item count:" + result.getBusinesses().size());

            //make sure the markerToId map can hold all the markers we are about to add
            markerToIdPositionPairMap.ensureCapacity(result.getBusinesses().size());
            idToMarkerMap.ensureCapacity(result.getBusinesses().size());
            idToRatingUrlMap.ensureCapacity(result.getBusinesses().size());
            addMarkersToMap();

            //add a circle around the center point
            addCircleToMap();
        }

        //we have updated the map, so set this to false
        mapNeedsUpdate = false;

        //zoom to bounds using the approx map size
        zoomMapToBounds(false, true, false); //false = don't include people, true = base it on display size, false = don't animate transition
    }

    /**
     * Adds markers to the map.
     * Also saves marker to business id mapping, and business id to rating url mapping.
     */
    private void addMarkersToMap() {
        //loop through suggested items and:
        //1. place markers on the map
        //2. add markers to the markerToId map
        for (int i=0; i<result.getBusinesses().size(); i++) {
            YelpBusiness business = result.getBusinesses().get(i);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(business.getLocation().getCoordinate().getLatitude(), business.getLocation().getCoordinate().getLongitude()))
                    .title(business.getName())
                    .snippet(getString(R.string.review_count, business.getReview_count()))
                    .icon(MapColorUtils.determineMarkerIcon(getContext(), selectedIdsMap.containsKey(business.getId())));
            Marker marker = map.addMarker(markerOptions);

            markerToIdPositionPairMap.put(marker, new Pair<>(business.getId(), i));
            idToMarkerMap.put(business.getId(), marker);
            idToRatingUrlMap.put(business.getId(), business.getRating_img_url_large());
        }
    }

    /**
     * Adds a circle centered at the center of the yelp result region, with radius specified
     * by the distance from the center to the nw point of the result region.
     */
    private void addCircleToMap() {
        Log.d(TAG, "addCircleToMap");

        //read the yelp result region so that we can specify the map bounds
        final YelpResultRegion resultRegion = result.getRegion();

        //get region center
        final LatLng center = new LatLng(
                resultRegion.getCenter().getLatitude(),
                resultRegion.getCenter().getLongitude());

        //get delta of lat/long from the center
        double latDelta = resultRegion.getSpan().getLatitude_delta();
        double longDelta = resultRegion.getSpan().getLongitude_delta();

        //compute nw from center
        LatLng nw = new LatLng(center.latitude + latDelta/2, center.longitude - longDelta/2);

        //compute radius using nw point
        double radius = SphericalUtil.computeDistanceBetween(center, nw);
        Log.d(TAG, String.format("addCircleToMap: Center:%s, Radius:%.2f", center, radius));

        //create circle and add to map
        final CircleOptions circleOptions = new CircleOptions()
                .center(center)
                .radius(radius)
                .strokeWidth(1)
                .strokeColor(MapColorUtils.COLOR_GRAY_500_OPACITY_40) //using argb defined in class since HEX defined in colors.xml don't appear to be working for this case
                .fillColor(MapColorUtils.COLOR_GRAY_500_OPACITY_40);
        map.addCircle(circleOptions);
    }

    /**
     * OnSelectionChangedCallback implementation
     * The contents of the idToMarkerMap array map has changed (even if the reference itself hasn't).
     * Change the color of the corresponding marker.
     * @param id id of the item whose selection has changed.
     * @param toggleState resulting toggle state (true means selected, false means not selected)
     */
    @Override
    public void onSelectionChanged(String id, boolean toggleState) {
        Log.d(TAG, String.format("onSelectionChanged: Id:%s, ToggleState:%s", id, toggleState));

        if(!idToMarkerMap.containsKey(id)) {
            Log.d(TAG, String.format("onSelectionChanged: Id could not be found in idToMarkerMap, so nothing to do. idToMarkerMap size:%d", idToMarkerMap.size()));
            return;
        }

        //find the marker corresponding to the id whose selection has changed
        Marker marker = idToMarkerMap.get(id);
        //change the color of the icon based on the current selection state
        if (marker != null) {
            marker.setIcon(MapColorUtils.determineMarkerIcon(getContext(), toggleState));
        }
    }

    /**
     * GoogleMap.OnMapLoadedCallback implementation
     * This method is called when the map has finished rendering.
     */
    @Override
    public void onMapLoaded() {
        Log.d(TAG, "onMapLoaded");
    }

    /**
     * Helper method that read the yelp result region and returns the bounds for the map
     * as a pair of points (sw, ne).
     * @return Pair<LatLng (sw), LatLng (ne)>
     */
    private Pair<LatLng, LatLng> computePairBoundsFromResult() {
        //read the yelp result region so that we can specify the map bounds
        final YelpResultRegion resultRegion = result.getRegion();

        //get region center
        final LatLng center = new LatLng(
                resultRegion.getCenter().getLatitude(),
                resultRegion.getCenter().getLongitude());
        //get delta of lat/long from the center
        double latDelta = resultRegion.getSpan().getLatitude_delta();
        double longDelta = resultRegion.getSpan().getLongitude_delta();

        //compute ne and sw from center
        LatLng ne = new LatLng(center.latitude + latDelta/2, center.longitude + longDelta/2);
        LatLng sw = new LatLng(center.latitude - latDelta/2, center.longitude - longDelta/2);

        //create bounds object
        return new Pair<>(sw, ne);
    }

    /**
     * Zooms the map to bounds based on specified parameters.
     * @param includePeople if true, then bounds will include user and friend's locations. Additional padding is provided.
     * @param useDisplaySize if true, then base map size on display size since layout hasn't happened yet
     * @param shouldAnimate if true, then animate the transition
     */
    private void zoomMapToBounds(boolean includePeople, boolean useDisplaySize, boolean shouldAnimate) {
        final Pair<LatLng, LatLng> pairBounds = computePairBoundsFromResult();
        final DisplayMetrics display = getResources().getDisplayMetrics();

        LatLngBounds mapBounds;
        int padding = display.widthPixels / 10;

        if (includePeople) { //bounds = result and people
            mapBounds = new LatLngBounds.Builder()
                    .include(userLatLng)
                    .include(friendLatLng)
                    .include(pairBounds.first)
                    .include(pairBounds.second)
                    .build();
        }
        else { //bounds = result only, no people
            mapBounds = new LatLngBounds(pairBounds.first, pairBounds.second); //first = sw, second = ne
        }

        if (shouldAnimate) {
            map.animateCamera(useDisplaySize ?
                    CameraUpdateFactory.newLatLngBounds(mapBounds, display.widthPixels, display.heightPixels, padding) :
                    CameraUpdateFactory.newLatLngBounds(mapBounds, padding));
        }
        else {
            map.moveCamera(useDisplaySize ?
                    CameraUpdateFactory.newLatLngBounds(mapBounds, display.widthPixels, display.heightPixels, padding) :
                    CameraUpdateFactory.newLatLngBounds(mapBounds, padding));
        }
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
        //do nothing if the tapped info window belongs the user and friend's location marker
        final String title = marker.getTitle();
        if ((userLocationMarker != null && userLocationMarker.getTitle().equalsIgnoreCase(title)) ||
                (friendLocationMarker != null && friendLocationMarker.getTitle().equalsIgnoreCase(title))) {
            Log.d(TAG, "onInfoWindowClick: Do nothing. Marker's title:"  + title);
            return;
        }

        //get the business id corresponding to the clicked marker
        final Pair<String,Integer> businessIdPositionPair = markerToIdPositionPairMap.get(marker);
        Log.d(TAG, String.format("onInfoWindowClick: Marker title:%s, BusinessId:%s", marker.getTitle(), businessIdPositionPair.first));

        //notify the activity that a suggestion was clicked
        suggestionActionListener.onSuggestionClick(businessIdPositionPair.first, marker.getTitle(), marker.getPosition(), businessIdPositionPair.second);
    }

    /**
     * Toggles the user and friend's location markers on the map.  This method is called by SuggestionActivity when the
     * menu item is toggled.
     * @param show
     */
    public void showPeopleLocation(boolean show) {
        Log.d(TAG, "showPeopleLocation: Show:" + show);
        if (show) {
            //show user's marker
            if (userLocationMarker == null) {
                //create and add new marker
                MarkerOptions userMarkerOptions = new MarkerOptions()
                        .position(userLatLng)
                        .title(getString(R.string.map_marker_user_location))
                        .icon(MapColorUtils.determineMarkerIcon(getContext(), false));
                userLocationMarker = map.addMarker(userMarkerOptions);

                //let the info window renderer know about the user marker
                infoWindowAdapter.setUserLocationMarker(userLocationMarker);
            }
            else {
                //user marker already exists, so set it to visible
                userLocationMarker.setVisible(true);
            }

            //show friend's marker
            if (friendLocationMarker == null) {
                //create and add new marker
                MarkerOptions friendMarkerOptions = new MarkerOptions()
                        .position(friendLatLng)
                        .title(getString(R.string.map_marker_friend_location))
                        .icon(MapColorUtils.determineMarkerIcon(getContext(), false));
                friendLocationMarker = map.addMarker(friendMarkerOptions);

                //let the info window renderer know about the friend marker
                infoWindowAdapter.setFriendLocationMarker(friendLocationMarker);
            }
            else {
                //friend marker already exists, so set it to visible
                friendLocationMarker.setVisible(true);
            }

            //recalculate map bounds to include user and friend markers
            zoomMapToBounds(true, false, true); //true = include people, false = don't use display size, true = animate transition
        }
        else {
            //hide user's marker
            if (userLocationMarker != null) userLocationMarker.setVisible(false);

            //hide friend's marker
            if (friendLocationMarker != null) friendLocationMarker.setVisible(false);

            //recalculate map bounds to show results only
            zoomMapToBounds(false, false, true); //false = don't include people, false = don't use display size, true = animate transition
        }
    }
}
