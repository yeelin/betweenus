package com.example.yeelin.projects.betweenus.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.analytics.EventConstants;
import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalBusinessLocation;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.example.yeelin.projects.betweenus.fragment.callback.OnSelectionChangedCallback;
import com.example.yeelin.projects.betweenus.fragment.callback.OnSuggestionActionListener;
import com.example.yeelin.projects.betweenus.fragment.callback.OnSuggestionsLoadedCallback;
import com.example.yeelin.projects.betweenus.data.generic.model.PlaceClusterItem;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.example.yeelin.projects.betweenus.utils.MapColorUtils;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.squareup.picasso.Target;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/28/15.
 * TODO: Need to include map attribution in about page
 * GoogleApiAvailability.getOpenSourceSoftwareLicenseInfo.
 */
public class SuggestionsClusterMapFragment
        extends BaseMapFragment
        implements
        OnSuggestionsLoadedCallback, //tells fragment when data is loaded
        OnSelectionChangedCallback, //tells fragment when selections have changed
        ClusterManager.OnClusterClickListener<PlaceClusterItem>,
        ClusterManager.OnClusterInfoWindowClickListener<PlaceClusterItem>,
        ClusterManager.OnClusterItemClickListener<PlaceClusterItem>,
        ClusterManager.OnClusterItemInfoWindowClickListener<PlaceClusterItem> {

    //logcat
    private static final String TAG = SuggestionsClusterMapFragment.class.getCanonicalName();

    //saved instance state
    private static final String STATE_SHOWING_PEOPLE_LOCATION = SuggestionsClusterMapFragment.class.getSimpleName() + ".showingPeopleLocation";
    private static final String STATE_USER_LATLNG = SuggestionsClusterMapFragment.class.getSimpleName() + ".userLatLng";
    private static final String STATE_FRIEND_LATLNG = SuggestionsClusterMapFragment.class.getSimpleName() + ".friendLatLng";
    private static final String STATE_MID_LATLNG = SuggestionsClusterMapFragment.class.getSimpleName() + ".midLatLng";

    //constants
    private static final double MIN_HIGH_RATING = 3.9;

    //member variables
    //map-related
    private ClusterManager<PlaceClusterItem> clusterManager;
    private PlaceClusterRenderer clusterRenderer;
    private PlaceClusterItemInfoWindowAdapter clusterItemInfoWindowAdapter;
    private PlaceClusterInfoWindowAdapter clusterInfoWindowAdapter;
    private Marker userLocationMarker; //for showing user's location marker
    private Marker friendLocationMarker; //for showing friend's location marker
    private boolean mapNeedsUpdate = false;
    private boolean showingPeopleLocation = false;

    private ArrayList<LocalResult> localResultArrayList = new ArrayList<>();
    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    //selected ids map
    private ArrayMap<String, Integer> selectedIdsMap;
    //allows us to retrieve data back later
    private ArrayMap<String, PlaceClusterItem> idToClusterItemMap = new ArrayMap<>(); //needed to toggle the marker color
    private SparseArray<String> clusterSizeToTitleMap = new SparseArray<>();
    private SparseArray<String> placesWithHighRatingToSnippetMap = new SparseArray<>();

    private OnSuggestionActionListener suggestionActionListener;

    /**
     * Creates a new instance of the map fragment
     * @return
     */
    public static SuggestionsClusterMapFragment newInstance() {
        return new SuggestionsClusterMapFragment();
    }

    /**
     * Required public empty constructor
     */
    public SuggestionsClusterMapFragment() { }

    /**
     * Make sure the parent fragment or activity implements the suggestion click listener
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : context;
        try {
            suggestionActionListener = (OnSuggestionActionListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement OnSuggestionActionListener");
        }
    }

    /**
     * Configure the fragment. Request that onCreateOptionsMenu be called later.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            //restore last shown people state
            showingPeopleLocation = savedInstanceState.getBoolean(STATE_SHOWING_PEOPLE_LOCATION, false);
            Log.d(TAG, "onCreate: Saved instance state is not null. ShowingPeople:" + showingPeopleLocation);

            userLatLng = savedInstanceState.getParcelable(STATE_USER_LATLNG);
            friendLatLng = savedInstanceState.getParcelable(STATE_FRIEND_LATLNG);
            midLatLng = savedInstanceState.getParcelable(STATE_MID_LATLNG);
        }
    }

    /**
     * Inflate the fragment's menus items.
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_suggestions_map_fragment, menu);

        //set the people menu item icon and title to match the showingPeopleLocation state
        togglePeopleMenu(menu.findItem(R.id.action_show_people_location));
    }

    /**
     * Save out the showingPeopleLocation boolean so that we remember if the people markers were
     * being displayed or not at the time.
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SHOWING_PEOPLE_LOCATION, showingPeopleLocation);

        if (userLatLng != null) outState.putParcelable(STATE_USER_LATLNG, userLatLng);
        if (friendLatLng != null) outState.putParcelable(STATE_FRIEND_LATLNG, friendLatLng);
        if (midLatLng != null) outState.putParcelable(STATE_MID_LATLNG, midLatLng);
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
     * Handles user selection of menu options that were added by this fragment.
     * 1. Show as list
     * 2. Show people location
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppEventsLogger logger = AppEventsLogger.newLogger(getContext());
        Bundle parameters = new Bundle();

        switch (item.getItemId()) {
            //toggle over to the list view
            case R.id.action_show_list:
                Log.d(TAG, "onOptionsItemSelected: User wants to see results in a list");
                suggestionActionListener.showList();

                //log user switch to list view from map view
                parameters.putString(EventConstants.EVENT_PARAM_SOURCE_VIEW, EventConstants.EVENT_PARAM_VIEW_MAP);
                parameters.putString(EventConstants.EVENT_PARAM_DESTINATION_VIEW, EventConstants.EVENT_PARAM_VIEW_LIST);
                logger.logEvent(EventConstants.EVENT_NAME_SWITCHED_VIEWS, parameters);
                return true;

            //toggle people location on/off
            case R.id.action_show_people_location:
                Log.d(TAG, String.format("onOptionsItemSelected: People toggle clicked. Current:%s, Next:%s",
                        showingPeopleLocation ? "people" : "no people", !showingPeopleLocation ? "people" : "no people"));
                //update the boolean state
                showingPeopleLocation = !showingPeopleLocation;
                togglePeople(item);

                //log user toggle people location on/off
                parameters.putBoolean(EventConstants.EVENT_PARAM_SWITCHED_PEOPLE_LOCATION, showingPeopleLocation);
                logger.logEvent(EventConstants.EVENT_NAME_VIEWED_PEOPLE_LOCATION, parameters);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Updates the people toggle menu and markers.
     * @param item
     */
    private void togglePeople(MenuItem item) {
        //toggle people location menu item on/off
        togglePeopleMenu(item);

        //toggle the people markers
        togglePeopleMarkers(true); //true = update UI immediately
    }

    /**
     * Depending on the boolean showingPeopleLocation, this method toggles the icon and title of the
     * people menu item
     * @param item
     */
    private void togglePeopleMenu(MenuItem item) {
        if (showingPeopleLocation) {
            //showing people location, so menu should show the opposite state
            item.setIcon(R.drawable.ic_action_social_people);
            item.setTitle(R.string.action_hide_people_location);
        }
        else {
            //not showing people location, so menu should show the opposite state
            item.setIcon(R.drawable.ic_action_social_people_outline);
            item.setTitle(R.string.action_show_people_location);
        }
    }

    /**
     * Set up the click listener for the info window that is displayed when marker is tapped
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");
        //call BaseMapFragment's onMapReady method first
        super.onMapReady(googleMap);

        //initialize cluster manager and cluster renderer
        clusterManager = new ClusterManager<>(getContext(), map);
        clusterRenderer = new PlaceClusterRenderer(getContext(), map, clusterManager);
        clusterManager.setRenderer(clusterRenderer);

        //cluster manager will be the map's listener for the camera, marker clicks, and info window clicks
        map.setOnCameraChangeListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        map.setOnInfoWindowClickListener(clusterManager);

        //http://stackoverflow.com/questions/21885225/showing-custom-infowindow-for-android-maps-utility-library-for-android
        //1. set cluster manager's marker manager as the map's info window adapter
        map.setInfoWindowAdapter(clusterManager.getMarkerManager());
        //map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        //2. set a custom info window adapter for the marker collection
        clusterItemInfoWindowAdapter = new PlaceClusterItemInfoWindowAdapter();
        clusterManager.getMarkerCollection().setOnInfoWindowAdapter(clusterItemInfoWindowAdapter);
        //3. set a custom info window adapter for the cluster collection
        clusterInfoWindowAdapter = new PlaceClusterInfoWindowAdapter();
        clusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(clusterInfoWindowAdapter);

        //this class will handle cluster clicks and cluster info window clicks
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterInfoWindowClickListener(this);

        //this class will handle cluster item clicks and cluster item info window clicks
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.setOnClusterItemInfoWindowClickListener(this);

        //if the results had come in first before the map was ready and the map needs updating immediately, then do it.
        // otherwise, when the results come in, onSuggestionsLoaded will be called and the map will be updated the usual way
        if (mapNeedsUpdate) {
            Log.d(TAG, "onMapReady: Map needs update");
            updateMap();
        }
        else {
            Log.d(TAG, "onMapReady: Map doesn't need update yet");
        }
    }

    /**
     * OnSuggestionsLoadedCallback implementation
     * The service has finished fetching the latlngs.  Called by SuggestionsActivity to update the latlngs in
     * this fragment.
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    public void onLatLngLoad(LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        setLatLng(userLatLng, friendLatLng, midLatLng);
    }

    /**
     * Setter for latlngs
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    private void setLatLng(LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        this.midLatLng = midLatLng;
    }

    /**
     * OnSuggestionsLoadedCallback implementation
     * The data fragment has finished fetching the data.  This method is called by SuggestionsActivity to update the view.
     * @param localResult
     * @param selectedIdsMap
     * @param hasMoreData
     */
    public void onSinglePageLoad(@Nullable LocalResult localResult,
                                 @NonNull ArrayMap<String,Integer> selectedIdsMap,
                                 boolean hasMoreData) {

        if (localResult == null || localResult.getLocalBusinesses().size() == 0) {
            Log.d(TAG, "onSinglePageLoad: Local result is null or empty, so nothing to do");
            return;
        }

        localResultArrayList.add(localResult); //accumulate the result
        this.selectedIdsMap = selectedIdsMap;

        //check if map is null
        if (map == null) {
            Log.d(TAG, "onSinglePageLoad: Map is null, so nothing to do now. Map will update later when it is ready");
            mapNeedsUpdate = true; //tell ourselves that we need to update the map later when it is ready
            return;
        }

        //map is not null so update the map
        updateMap();
    }

    /**
     * SuggestionsLoadedCallback implementation
     * The data fragment has finished fetching multiple pages data.  Called by SuggestionsActivity to update the view.
     * @param localResultArrayList
     * @param selectedIdsMap
     * @param hasMoreData
     */
    @Override
    public void onMultiPageLoad(ArrayList<LocalResult> localResultArrayList,
                                @NonNull ArrayMap<String, Integer> selectedIdsMap,
                                boolean hasMoreData) {
        if (localResultArrayList == null || localResultArrayList.size() == 0) {
            Log.d(TAG, "onMultiPageLoad: Local result arraylist is null or empty, so nothing to do");
            return;
        }

        this.localResultArrayList = (ArrayList<LocalResult>) localResultArrayList.clone();
        this.selectedIdsMap = selectedIdsMap;

        //clear just in case there were things in it already
        idToClusterItemMap.clear();

        //check if map is null
        if (map == null) {
            Log.d(TAG, "onMultiPageLoad: Map is null, so nothing to do now. Map will update later when it is ready");
            mapNeedsUpdate = true; //tell ourselves that we need to update the map later when it is ready
            return;
        }

        //map is not null so update the map
        updateMap();
    }

    /**
     * Updates the map with the result
     * This is similar to what the SuggestionsAdapter does in updateItems().
     */
    private void updateMap() {
        //we have updated the map, so set this to false
        mapNeedsUpdate = false;

        //add new cluster items and call cluster!
        addClusterItemsToClusterManager();
        clusterManager.cluster();

        //update people location markers
        updatePeopleLocationMarkers();

        //zoom map to new bounds since we just added new cluster items
        zoomMapToBounds(true, false); //true = base it on display size, false = don't animate transition
    }

    /**
     * Loops through businesses in result and does the following:
     * 1. Creates a cluster item for each business.
     * 2. Stores reference to cluster item in the idToClusterItemMap
     */
    private void addClusterItemsToClusterManager() {
        //make sure the idToClusterItemMap has enough space
        //idToClusterItemMap.ensureCapacity(result.getLocalBusinesses().size());

        //loop through result
        for (int i=0; i<localResultArrayList.size(); i++) {
            final LocalResult localResult = localResultArrayList.get(i);

            for (int j = 0; j < localResult.getLocalBusinesses().size(); j++) {
                final LocalBusiness business = localResult.getLocalBusinesses().get(j);

                //create a new cluster item
                final PlaceClusterItem clusterItem = new PlaceClusterItem(
                        business.getLocalBusinessLocation().getLatLng(),
                        business.getId(),
                        business.getName(),
                        business.getRatingImageUrl(),
                        business.getRating(),
                        business.getReviewCount(),
                        business.getLikes(),
                        business.getNormalizedLikes(),
                        business.getCheckins(),
                        j,
                        getContext());

                //put it in the idToClusterItemMap
                idToClusterItemMap.put(business.getId(), clusterItem);
            }
        }

        //add to the cluster manager in bulk
        clusterManager.addItems(idToClusterItemMap.values());
    }

    /**
     * Adds a circle centered at the center of the yelp result region, with radius specified
     * by the distance from the center to the nw point of the result region.
     */
    @Deprecated
//    private void addCircleToMap() {
//        Log.d(TAG, "addCircleToMap");
//
//        //read the yelp result region so that we can specify the map bounds
//        final YelpResultRegion resultRegion = result.getRegion();
//
//        //get region center
//        final LatLng center = new LatLng(
//                resultRegion.getCenter().getLatitude(),
//                resultRegion.getCenter().getLongitude());
//
//        //get delta of lat/long from the center
//        double latDelta = resultRegion.getSpan().getLatitude_delta();
//        double longDelta = resultRegion.getSpan().getLongitude_delta();
//
//        //compute nw from center
//        LatLng nw = new LatLng(center.latitude + latDelta/2, center.longitude - longDelta/2);
//
//        //compute radius using nw point
//        double radius = SphericalUtil.computeDistanceBetween(center, nw);
//        Log.d(TAG, String.format("addCircleToMap: Center:%s, Radius:%.2f", center, radius));
//
//        //create circle and add to map
//        final CircleOptions circleOptions = new CircleOptions()
//                .center(center)
//                .radius(radius)
//                .strokeWidth(1)
//                .strokeColor(MapColorUtils.COLOR_GRAY_500_OPACITY_40) //using argb defined in class since HEX defined in colors.xml don't appear to be working for this case
//                .fillColor(MapColorUtils.COLOR_GRAY_500_OPACITY_40);
//        map.addCircle(circleOptions);
//    }

    /**
     * OnClusterClickListener<PlaceClusterItem>
     * Called when the cluster is clicked
     * @param cluster A collection of ClusterItems that are nearby each other.
     * @return
     */
    @Override
    public boolean onClusterClick(Cluster<PlaceClusterItem> cluster) {
        Log.d(TAG, "onClusterClick: A cluster was clicked");
        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // cluster is centered and for the cluster's info window to open, if it has one).
        return false;
    }

    /**
     * OnClusterInfoWindowClickListener<PlaceClusterItem>
     * Called when the cluster's info window is clicked
     * @param cluster A collection of ClusterItems that are nearby each other.
     */
    @Override
    public void onClusterInfoWindowClick(Cluster<PlaceClusterItem> cluster) {
        Log.d(TAG, "onClusterInfoWindowClick: A cluster's info window was clicked");
        declusterByClusterBounds(cluster);
    }

    /**
     * Attempts to decluster by increasing the zoom by the specified amount.
     * Actual declustering is not guaranteed.
     * @param cluster
     * @param increaseInZoom
     */
    @Deprecated
    private void declusterByIncreasingZoom(final Cluster<PlaceClusterItem> cluster, int increaseInZoom) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), map.getCameraPosition().zoom + increaseInZoom));
    }

    /**
     * Attempts to decluster by changing the map bounds to include just the items in the cluster.
     * Complete declustering is not guaranteed, but this will break the original cluster into at least 2 clusters.
     * @param cluster
     */
    private void declusterByClusterBounds(final Cluster<PlaceClusterItem> cluster) {
        final DisplayMetrics display = getResources().getDisplayMetrics();
        int padding = display.widthPixels / 10;

        final LatLngBounds.Builder builder = LatLngBounds.builder();
        for (PlaceClusterItem clusterItem : cluster.getItems()) {
            builder.include(clusterItem.getPosition());
        }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
    }

    /**
     * OnClusterItemClickListener<PlaceClusterItem>
     * Called when an individual cluster item (i.e. marker) is clicked.
     * This should be similar to onMarkerClick
     *
     * @param placeClusterItem ClusterItem represents a marker on the map.
     * @return
     */
    @Override
    public boolean onClusterItemClick(PlaceClusterItem placeClusterItem) {
        Log.d(TAG, "onClusterItemClick: A cluster item was clicked");

        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    /**
     * OnClusterItemInfoWindowClickListener<PlaceClusterItem>
     * Called when an individual cluster item's info window is clicked
     * This should be similar to onInfoWindowClick
     * @param placeClusterItem ClusterItem represents a marker on the map.
     */
    @Override
    public void onClusterItemInfoWindowClick(PlaceClusterItem placeClusterItem) {
        Log.d(TAG, "onClusterItemInfoWindowClick: A cluster item's info window was clicked");

        //notify the activity that a suggestion was clicked
        //open the view pager of detail fragments
        suggestionActionListener.onSuggestionClick(placeClusterItem.getId(),
                placeClusterItem.getTitle(),
                placeClusterItem.getPosition(),
                placeClusterItem.getResultPosition());

        //log user switch to detail pager view
        AppEventsLogger logger = AppEventsLogger.newLogger(getContext());
        Bundle parameters = new Bundle();
        parameters.putString(EventConstants.EVENT_PARAM_SOURCE_VIEW, EventConstants.EVENT_PARAM_VIEW_LIST);
        parameters.putString(EventConstants.EVENT_PARAM_DESTINATION_VIEW, EventConstants.EVENT_PARAM_VIEW_PAGER);
        logger.logEvent(EventConstants.EVENT_NAME_SWITCHED_VIEWS, parameters);
    }

    /**
     * Helper method that iterates over the fb and yelp results and returns the map bounds based
     * on the latlngs of the businesses in the result
     *
     * The map bounds are returned as a pair of points (sw, ne).
     * @return Pair<LatLng (sw), LatLng (ne)>
     */
    private Pair<LatLng, LatLng> computePairBoundsFromResult() {
        //get region center
//        final LatLng center = result.getResultCenter();
//
//        final LatLng ne;
//        final LatLng sw;
//        if (center != null) {
//            //note: we most likely have yelp data
//            //get delta of lat/long from the center
//            double latDelta = result.getResultLatitudeDelta();
//            double longDelta = result.getResultLongitudeDelta();
//
//            //compute ne and sw from center
//            ne = new LatLng(center.latitude + latDelta / 2, center.longitude + longDelta / 2);
//            sw = new LatLng(center.latitude - latDelta / 2, center.longitude - longDelta / 2);
//        }
//        else {
            //note: we most likely have fb data
            //build the bounds from all the latlngs in the result
        final LatLngBounds.Builder mapBoundsBuilder = new LatLngBounds.Builder();

        for (int i=0; i<localResultArrayList.size(); i++) {
            final LocalResult localResult = localResultArrayList.get(i);

            for (int j=0; j<localResult.getLocalBusinesses().size(); j++) {
                final LocalBusinessLocation location = localResult.getLocalBusinesses().get(j).getLocalBusinessLocation();
                if (location != null) {
                    mapBoundsBuilder.include(location.getLatLng());
                }
            }
        }

        //create bounds object
        final LatLngBounds mapBounds = mapBoundsBuilder.build();
        return new Pair<>(mapBounds.southwest, mapBounds.northeast);
    }

    /**
     * Zooms the map to bounds based on specified parameters.
     * @param useDisplaySize if true, then base map size on display size since layout hasn't happened yet
     * @param shouldAnimate if true, then animate the transition
     */
    private void zoomMapToBounds(boolean useDisplaySize, boolean shouldAnimate) {
        final Pair<LatLng, LatLng> pairBounds = computePairBoundsFromResult();
        final DisplayMetrics display = getResources().getDisplayMetrics();
        int toolbarHeight = ((AppCompatActivity)getActivity()).getSupportActionBar() == null ?
                0 : ((AppCompatActivity)getActivity()).getSupportActionBar().getHeight();

        LatLngBounds mapBounds;
        int padding = display.widthPixels / 10;

        if (showingPeopleLocation) { //bounds = result and people
            mapBounds = new LatLngBounds.Builder()
                    .include(userLatLng)
                    .include(friendLatLng)
                    .include(pairBounds.first)
                    .include(pairBounds.second)
                    .build();
            padding += 50; //additional padding of 50 so that the markers are not clipped
        }
        else { //bounds = result only, no people
            mapBounds = new LatLngBounds(pairBounds.first, pairBounds.second); //first = sw, second = ne
        }

        if (shouldAnimate) {
            map.animateCamera(useDisplaySize ?
                    CameraUpdateFactory.newLatLngBounds(mapBounds, display.widthPixels, display.heightPixels - toolbarHeight, padding) :
                    CameraUpdateFactory.newLatLngBounds(mapBounds, padding));
        }
        else {
            map.moveCamera(useDisplaySize ?
                    CameraUpdateFactory.newLatLngBounds(mapBounds, display.widthPixels, display.heightPixels - toolbarHeight, padding) :
                    CameraUpdateFactory.newLatLngBounds(mapBounds, padding));
        }
    }

    /**
     * Toggles the people location markers on the map.
     * This method is called by SuggestionActivity when the people menu item is toggled.
     *
     * @param updateImmediately if this is false, then only the member variable is set. the ui isn't updated until
     *                          the next time
     */
    private void togglePeopleMarkers(boolean updateImmediately) {
        if (updateImmediately && map != null) {
            updatePeopleLocationMarkers();
            zoomMapToBounds(false, true); //false = don't use display size, true = animate transition
        }
    }

    /**
     * Helper method that either puts people location markers on the map, or hides them.
     * Don't forget to call zoomMapToBounds after this method.
     */
    private void updatePeopleLocationMarkers () {
        Log.d(TAG, "updatePeopleLocation: Toggle state:" + showingPeopleLocation);

        if (showingPeopleLocation) {
            //show user's marker
            if (userLocationMarker == null) {
                //create and add new marker
                MarkerOptions userMarkerOptions = new MarkerOptions()
                        .icon(MapColorUtils.getUserMarkerIcon(getContext()))
                        .position(userLatLng);
                        //.title(getString(R.string.map_marker_user_location));
                userLocationMarker = map.addMarker(userMarkerOptions);
            }
            else {
                //user marker already exists, so set it to visible
                userLocationMarker.setVisible(true);
            }

            //show friend's marker
            if (friendLocationMarker == null) {
                //create and add new marker
                MarkerOptions friendMarkerOptions = new MarkerOptions()
                        .icon(MapColorUtils.getFriendMarkerIcon(getContext()))
                        .position(friendLatLng);
                        //.title(getString(R.string.map_marker_friend_location));
                friendLocationMarker = map.addMarker(friendMarkerOptions);
            }
            else {
                //friend marker already exists, so set it to visible
                friendLocationMarker.setVisible(true);
            }
        }
        else {
            //hide user's marker
            if (userLocationMarker != null) userLocationMarker.setVisible(false);

            //hide friend's marker
            if (friendLocationMarker != null) friendLocationMarker.setVisible(false);
        }
    }

    /**
     * OnSelectionChangedCallback implementation
     * The contents of the idToClusterItemMap array map has changed (even if the reference itself hasn't).
     * Change the color of the marker corresponding to the cluster item
     *
     * @param id id of the item whose selection has changed.
     * @param toggleState resulting toggle state (true means selected, false means not selected)
     */
    @Override
    public void onSelectionChanged(String id, boolean toggleState) {
        Log.d(TAG, String.format("onSelectionChanged: Id:%s, ToggleState:%s", id, toggleState));

        if(!idToClusterItemMap.containsKey(id)) {
            Log.d(TAG, String.format("onSelectionChanged: Id could not be found in idToClusterItemMap, so nothing to do. idToClusterItemMap size:%d", idToClusterItemMap.size()));
            return;
        }

        //find the cluster item corresponding to the id whose selection has changed
        final PlaceClusterItem clusterItem = idToClusterItemMap.get(id);
        if (clusterItem == null) {
            Log.d(TAG, "onSelectionChanged: Failed to retrieve cluster item with Id: " + id);
        }
        else {
            //get the marker from the cluster item
            final Marker marker = clusterRenderer.getMarker(clusterItem);
            if (marker == null) {
                //can't find marker
                //this means the cluster item hasn't been rendered as a marker yet
                //which is ok since onBeforeClusterItemRendered will be called later
                Log.d(TAG, "onSelectionChanged: Failed to retrieve marker from cluster item. This means marker hasn't been rendered yet. Cluster item title: " + clusterItem.getTitle());
            }
            else {
                //found marker, so change the icon
                Log.d(TAG, "onSelectionChanged: Success retrieving marker from cluster item. Cluster item title: " + clusterItem.getTitle());
                marker.setIcon(MapColorUtils.determineMarkerIcon(toggleState,
                        clusterItem.getRating() != LocalConstants.NO_DATA_DOUBLE ? clusterItem.getRating() : clusterItem.getNormalizedLikes()));
            }
        }
    }

    /**
     * PlaceClusterRenderer
     * The DefaultClusterRenderer is the default view for a ClusterManager. Markers are animated in and out of cluster.
     * This custom class tells the cluster manager how to render the cluster item and the cluster by
     * setting the marker options object to info contained in the cluster item or cluster.
     * The marker options object is what's used to create the marker on the map.
     */
    private class PlaceClusterRenderer
            extends DefaultClusterRenderer<PlaceClusterItem> {
        //logcat
        private final String TAG = PlaceClusterRenderer.class.getCanonicalName();

        /**
         * Constructor
         * @param context
         * @param map
         * @param clusterManager
         */
        public PlaceClusterRenderer(Context context, GoogleMap map, ClusterManager<PlaceClusterItem> clusterManager) {
            super(context, map, clusterManager);
        }

        /**
         * Called before the marker for a ClusterItem is added to the map.
         * Draws a single marker, i.e. one place. Sets the title, snippet and icon on the marker.
         *
         * @param item ClusterItem represents a marker on the map.
         * @param markerOptions
         */
        @Override
        protected void onBeforeClusterItemRendered(PlaceClusterItem item, MarkerOptions markerOptions) {
            super.onBeforeClusterItemRendered(item, markerOptions);

            Log.d(TAG, "onBeforeClusterItemRendered");
            //set the title, snippet and icon on the marker for the cluster item
            markerOptions.title(item.getTitle())
                    .snippet(item.getReviewsSnippet())
                    .icon(MapColorUtils.determineMarkerIcon(selectedIdsMap.containsKey(item.getId()),
                            item.getRating()!= LocalConstants.NO_DATA_DOUBLE ? item.getRating() : item.getNormalizedLikes()));
        }

        /**
         * Called before the marker for a Cluster is added to the map.
         * Draws a single cluster, i.e. multiple places.  Sets the title on the marker.
         *
         * @param cluster A collection of ClusterItems that are nearby each other.
         * @param markerOptions
         */
        @Override
        protected void onBeforeClusterRendered(Cluster<PlaceClusterItem> cluster, MarkerOptions markerOptions) {
            super.onBeforeClusterRendered(cluster, markerOptions);

            Log.d(TAG, "onBeforeClusterRendered");
            //check if the title string already exists for this cluster size
            //reuse it if it does, so that we don't do another resource read
            String clusterTitle = clusterSizeToTitleMap.get(cluster.getSize());
            if (clusterTitle == null) {
                clusterTitle = getString(R.string.map_cluster_infoWindow_title, cluster.getSize());
                clusterSizeToTitleMap.put(cluster.getSize(), clusterTitle);
            }

            //set title on the
            markerOptions.title(clusterTitle);
        }
    }

    /**
     * PlaceClusterItemInfoWindowAdapter
     * This class is an adapter for info windows of CLUSTER ITEM markers (not cluster markers!) in that it:
     * 1. provides the custom layout (via layout files) for a) place and b) user/friend location marker's info windows
     * 2. sets the views in the inflated layout to the values in the markers
     */
    private class PlaceClusterItemInfoWindowAdapter
            implements GoogleMap.InfoWindowAdapter {
        //logcat
        private final String TAG = PlaceClusterItemInfoWindowAdapter.class.getCanonicalName();

        //member variables
        private SimpleArrayMap<Marker, View> clusterItemMarkerToInfoWindowMap;

        /**
         * Constructor
         */
        public PlaceClusterItemInfoWindowAdapter() {
            clusterItemMarkerToInfoWindowMap = new SimpleArrayMap<>();
        }

        /**
         * The API will first call getInfoWindow(Marker) and if null is returned,
         * it will then call getInfoContents(Marker).
         * Returns null since we are not customizing the look and feel of the window per se.
         * @param marker
         * @return
         */
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        /**
         * Returns custom content for the info window given a cluster item marker.
         * Checks clusterItemMarkerToInfoWindowMap to see if an info window already exists for the marker.
         * 1. If it does, it returns the existing info window.
         * 2. Otherwise, it inflates a new info window depending on whether it is a people location or a place marker.
         *
         * @param marker
         * @return
         */
        @Override
        public View getInfoContents(Marker marker) {
            Log.d(TAG, "getInfoContents: Marker title: " + marker.getTitle());

            //check if view for the given marker already exists, and if so return it
            //there will be one view per marker instead of recycling views
            if (clusterItemMarkerToInfoWindowMap.containsKey(marker)) {
                Log.d(TAG, "getInfoContents: Map has infoWindow for marker so returning");
                return clusterItemMarkerToInfoWindowMap.get(marker);
            }

            //doesn't exist, so inflate a new one
            Log.d(TAG, "getInfoContents: Map doesn't have infoWindow for cluster marker so inflating a new map_info_place window");
            final View view = View.inflate(getContext(), R.layout.map_info_place, null);

            //retrieve the cluster item for the marker
            final PlaceClusterItem placeClusterItem = clusterRenderer.getClusterItem(marker);

            //get handles to all the textviews
            final TextView title = (TextView) view.findViewById(R.id.title);
            final TextView reviews = (TextView) view.findViewById(R.id.reviews);
            final TextView likes = (TextView) view.findViewById(R.id.likes);
            final TextView checkins = (TextView) view.findViewById(R.id.checkins);

            //set the title
            title.setText(marker.getTitle());

            if (placeClusterItem == null) {
                reviews.setVisibility(View.GONE);
                likes.setVisibility(View.GONE);
                checkins.setVisibility(View.GONE);
            }
            else {
                //set reviews
                if (placeClusterItem.getRating() == LocalConstants.NO_DATA_DOUBLE) {
                    reviews.setVisibility(View.GONE);
                }
                else {
                    reviews.setText(placeClusterItem.getReviewsSnippet());
                    //note: picasso only keeps a weak ref to the target so it may be gc-ed
                    //use setTag so that target will be alive as long as the view is alive
                    final Target target = ImageUtils.newTarget(getContext(), reviews, marker, true);
                    reviews.setTag(target);
                    ImageUtils.loadImage(getContext(),
                            placeClusterItem.getRatingUrl(), //TODO: provide placeholder rating image
                            target);
                }

                //set likes
                if (placeClusterItem.getLikes() == LocalConstants.NO_DATA_INTEGER) {
                    likes.setVisibility(View.GONE);
                }
                else {
                    likes.setText(placeClusterItem.getLikesSnippet());
                }

                //set checkins
                if (placeClusterItem.getCheckins() == LocalConstants.NO_DATA_INTEGER) {
                    checkins.setVisibility(View.GONE);
                }
                else {
                    checkins.setText(placeClusterItem.getCheckinsSnippet());
                }
            }

            //store the info window view so that we can return it if requested again
            clusterItemMarkerToInfoWindowMap.put(marker, view);
            return view;
        }
    }

    /**
     * PlaceClusterInfoWindowAdapter
     * This class is an adapter for info windows of CLUSTER markers in that it:
     * 1. provides the custom layout (via layout files) for cluster markers only
     * 2. sets the views in the inflated layout to the values in the markers
     */
    private class PlaceClusterInfoWindowAdapter
            implements GoogleMap.InfoWindowAdapter {

        //logcat
        private final String TAG = PlaceClusterInfoWindowAdapter.class.getCanonicalName();

        //member variables
        private SimpleArrayMap<Marker, View> clusterMarkerToInfoWindowMap;

        /**
         * Constructor
         */
        public PlaceClusterInfoWindowAdapter() {
            clusterMarkerToInfoWindowMap = new SimpleArrayMap<>();
        }

        /**
         * The API will first call getInfoWindow(Marker) and if null is returned,
         * it will then call getInfoContents(Marker).
         * Returns null since we are not customizing the look and feel of the window per se.
         * @param marker
         * @return
         */
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        /**
         * Returns custom content for the info window given a cluster marker.
         * Checks clusterMarkerToInfoWindowMap to see if an info window already exists for the marker.
         * 1. If it does, it returns the existing info window.
         * 2. Otherwise, it inflates a new info window depending on whether it is a people location or a place marker.
         *
         * @param marker
         * @return
         */
        @Override
        public View getInfoContents(Marker marker) {
            Log.d(TAG, "getInfoContents: Marker title: " + marker.getTitle());

            //check if view for the given marker already exists, and if so return it
            //there will be one view per marker instead of recycling views
            if (clusterMarkerToInfoWindowMap.containsKey(marker)) {
                Log.d(TAG, "getInfoContents: Map has infoWindow for marker so returning");
                return clusterMarkerToInfoWindowMap.get(marker);
            }

            //doesn't exist, so inflate a new one
            Log.d(TAG, "getInfoContents: Map doesn't have infoWindow for cluster marker so inflating a new map_info_cluster window");
            final View view = View.inflate(getContext(), R.layout.map_info_cluster, null);

            //retrieve the cluster for the marker
            final Cluster<PlaceClusterItem> cluster = clusterRenderer.getCluster(marker);

            //get handles to all the textviews
            final TextView title = (TextView) view.findViewById(R.id.title);
            final TextView highRating = (TextView) view.findViewById(R.id.high_rating);
            final TextView bestRating = (TextView) view.findViewById(R.id.best_rating);
            final TextView mostLikes = (TextView) view.findViewById(R.id.most_likes);
            final TextView mostCheckins = (TextView) view.findViewById(R.id.most_checkins);

            //set title
            title.setText(marker.getTitle());

            //set highRating and bestRating
            if (cluster == null) {
                highRating.setVisibility(View.GONE);
                bestRating.setVisibility(View.GONE);
                mostLikes.setVisibility(View.GONE);
                mostCheckins.setVisibility(View.GONE);
            }
            else {
                //compute the best rating in the cluster and the places with high ratings, i.e. >= 4
                double bestRatingSoFar = 0.0;
                String bestRatingSoFarUrl = "";
                int numWithHighRating = 0;
                int mostLikesSoFar = 0;
                int mostCheckinsSoFar = 0;

                for (PlaceClusterItem clusterItem : cluster.getItems()) {
                    //high rating
                    if (clusterItem.getRating() >= MIN_HIGH_RATING) ++numWithHighRating;

                    //best rating
                    if (clusterItem.getRating() > bestRatingSoFar) {
                        bestRatingSoFar = clusterItem.getRating();
                        bestRatingSoFarUrl = clusterItem.getRatingUrl();
                    }

                    //most likes
                    if (clusterItem.getLikes() > mostLikesSoFar) mostLikesSoFar = clusterItem.getLikes();

                    //most checkins
                    if (clusterItem.getCheckins() > mostCheckinsSoFar) mostCheckinsSoFar = clusterItem.getCheckins();
                }

                //show bestRatingSoFar only if it's greater than its original value
                if (bestRatingSoFar == 0.0) {
                    //hide ratings-related UX
                    highRating.setVisibility(View.GONE);
                    bestRating.setVisibility(View.GONE);
                }
                else {
                    //show bestRating: load the rating image using the best rating url
                    final Target target = ImageUtils.newTarget(getContext(), bestRating, marker, false);
                    bestRating.setTag(target);
                    ImageUtils.loadImage(getContext(), bestRatingSoFarUrl, target);

                    //check if the best rating place has less than 4 stars
                    //if so no need to report numWithHighRating
                    if (bestRatingSoFar < MIN_HIGH_RATING) {
                        highRating.setVisibility(View.GONE);
                    } else {
                        //check if the highRating snippet string already exists for number with high ratings
                        //reuse it if it does, so that we don't do another resource read
                        String highRatingSnippet = placesWithHighRatingToSnippetMap.get(numWithHighRating);
                        if (highRatingSnippet == null) {
                            highRatingSnippet = getResources().getQuantityString(
                                    R.plurals.map_cluster_infoWindow_high_rating,
                                    numWithHighRating,
                                    numWithHighRating);
                            placesWithHighRatingToSnippetMap.put(numWithHighRating, highRatingSnippet);
                        }
                        highRating.setText(highRatingSnippet);
                    }
                }

                //show mostLikesSoFar only if it's greater than its original value
                if (mostLikesSoFar == 0) mostLikes.setVisibility(View.GONE);
                else mostLikes.setText(getResources().getQuantityString(R.plurals.most_likes, mostLikesSoFar, mostLikesSoFar));

                //show mostCheckinsSoFar only if it's greater than its original value
                if (mostCheckinsSoFar == 0) mostCheckins.setVisibility(View.GONE);
                else mostCheckins.setText(getResources().getQuantityString(R.plurals.most_checkins, mostCheckinsSoFar, mostCheckinsSoFar));
            }

            //store the info window view in a map so that we can return it if requested again
            clusterMarkerToInfoWindowMap.put(marker, view);
            return view;
        }
    }

    /**
     * CustomInfoWindowAdapter
     * This class is an adapter for info windows of markers
     * 1. handles only people location markers
     * 2. farms out the other types of markers to other adapters
     */
    @Deprecated
    private class CustomInfoWindowAdapter
            implements GoogleMap.InfoWindowAdapter {
        //logcat
        private final String TAG = CustomInfoWindowAdapter.class.getCanonicalName();

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            Log.d(TAG, "getInfoContents: Marker title: " + marker.getTitle());

            if (clusterRenderer.getCluster(marker) != null) {
                //we are dealing with a cluster
                Log.d(TAG, "getInfoContents: Redirecting to cluster info window adapter");
                return clusterInfoWindowAdapter.getInfoContents(marker);
            }
            else if (clusterRenderer.getClusterItem(marker) != null) {
                //we are dealing with a cluster item
                Log.d(TAG, "getInfoContents: Redirecting to cluster item info window adapter");
                return clusterItemInfoWindowAdapter.getInfoContents(marker);
            }
            else if ((userLocationMarker != null && userLocationMarker.getTitle().equalsIgnoreCase(marker.getTitle())) ||
                    (friendLocationMarker != null && friendLocationMarker.getTitle().equalsIgnoreCase(marker.getTitle()))) {
                //we are dealing with a user or friend location marker
                Log.d(TAG, "getInfoContents: Inflating a new map_info_people_location window");

                final View view = View.inflate(getContext(), R.layout.map_info_people_location, null);
                final TextView title = (TextView) view.findViewById(R.id.title);
                title.setText(marker.getTitle());
                return view;
            }
            else {
                Log.d(TAG, "getInfoContents: Unknown marker");
                return null;
            }
        }
    }
}