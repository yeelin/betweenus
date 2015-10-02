package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.model.PlaceClusterItem;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.model.YelpResult;
import com.example.yeelin.projects.betweenus.model.YelpResultRegion;
import com.example.yeelin.projects.betweenus.utils.FormattingUtils;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.example.yeelin.projects.betweenus.utils.MapColorUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.squareup.picasso.Target;

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
    //constants
    private static final double HIGH_RATING = 4.0;

    //member variables
    private ClusterManager<PlaceClusterItem> clusterManager;
    private PlaceClusterRenderer clusterRenderer;
    private PlaceClusterItemInfoWindowAdapter clusterItemInfoWindowAdapter;
    private PlaceClusterInfoWindowAdapter clusterInfoWindowAdapter;

    private YelpResult result;
    private boolean mapNeedsUpdate = false;

    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    //selected ids map
    private ArrayMap<String, Integer> selectedIdsMap;

    //allows us to retrieve data back later
    private SimpleArrayMap<String, PlaceClusterItem> idToClusterItemMap = new SimpleArrayMap<>(); //needed to toggle the marker color
    private Marker userLocationMarker; //for showing user's location marker
    private Marker friendLocationMarker; //for showing friend's location marker

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
        //map.setInfoWindowAdapter(clusterManager.getMarkerManager());
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
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
        //clear out the idToClusterItemMap
        idToClusterItemMap.clear();
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
            Log.d(TAG, "updateMap: Adding cluster items to map. Count:" + result.getBusinesses().size());

            //make sure the idToClusterItemMap has enough space
            idToClusterItemMap.ensureCapacity(result.getBusinesses().size());

            //add cluster items and call cluster!
            addClusterItemsToClusterManager();
            clusterManager.cluster();

            //add a circle around the center point
            //addCircleToMap();
        }

        //we have updated the map, so set this to false
        mapNeedsUpdate = false;

        //zoom to bounds using the approx map size
        zoomMapToBounds(false, true, false); //false = don't include people, true = base it on display size, false = don't animate transition
    }

    /**
     * Loops through business result and creates a cluster item for each business.
     * Stores references to cluster item in the idToClusterItemMap.
     */
    private void addClusterItemsToClusterManager() {
        //loop through result
        for (int i=0; i<result.getBusinesses().size(); i++) {
            final YelpBusiness business = result.getBusinesses().get(i);

            //create a new cluster item
            final PlaceClusterItem clusterItem = new PlaceClusterItem(
                    new LatLng(business.getLocation().getCoordinate().getLatitude(), business.getLocation().getCoordinate().getLongitude()),
                    business.getId(),
                    business.getName(),
                    getString(R.string.review_count, business.getReview_count()),
                    business.getRating_img_url_large(),
                    business.getRating(),
                    i);

            //add it to the cluster manager
            clusterManager.addItem(clusterItem);

            //put it in the idToClusterItemMap
            idToClusterItemMap.put(business.getId(), clusterItem);
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
                marker.setIcon(determineMarkerIcon(toggleState));
            }
        }
    }

    /**
     * Called when the cluster is clicked
     * @param cluster A collection of ClusterItems that are nearby each other.
     * @return
     */
    @Override
    public boolean onClusterClick(Cluster<PlaceClusterItem> cluster) {
        Log.d(TAG, "onClusterClick: A cluster was clicked");
        return false;
    }

    /**
     * Called when the cluster's info window is clicked
     * @param cluster A collection of ClusterItems that are nearby each other.
     */
    @Override
    public void onClusterInfoWindowClick(Cluster<PlaceClusterItem> cluster) {
        Log.d(TAG, "onClusterInfoWindowClick: A cluster's info window was clicked");
    }

    /**
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
     * Called when an individual cluster item's info window is clicked
     * This should be similar to onInfoWindowClick
     * @param placeClusterItem ClusterItem represents a marker on the map.
     */
    @Override
    public void onClusterItemInfoWindowClick(PlaceClusterItem placeClusterItem) {
        Log.d(TAG, "onClusterItemInfoWindowClick: A cluster item's info window was clicked");

        //notify the activity that a suggestion was clicked
        suggestionActionListener.onSuggestionClick(placeClusterItem.getId(),
                placeClusterItem.getTitle(),
                placeClusterItem.getPosition(),
                placeClusterItem.getResultPosition());
    }

    /**
     * Helper method that returns the correct hue based on the given toggle state
     * @param toggleState
     * @return
     */
    private BitmapDescriptor determineMarkerIcon(boolean toggleState) {
        return toggleState ?
                BitmapDescriptorFactory.defaultMarker(MapColorUtils.getInstance(getActivity()).getAccentDarkHue()) :
                BitmapDescriptorFactory.defaultMarker(MapColorUtils.getInstance(getActivity()).getPrimaryDarkHue());
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
     * Toggles the user and friend's location markers on the map.
     * This method is called by SuggestionActivity when the menu item is toggled.
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
                        .icon(determineMarkerIcon(false));
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
                        .position(friendLatLng)
                        .title(getString(R.string.map_marker_friend_location))
                        .icon(determineMarkerIcon(false));
                friendLocationMarker = map.addMarker(friendMarkerOptions);
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
                    .snippet(item.getSnippet())
                    .icon(determineMarkerIcon(selectedIdsMap.containsKey(item.getId())));
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
            markerOptions.title(getString(R.string.map_cluster_infoWindow_title, cluster.getSize()));
        }

//        /**
//         * Always render as clusters if there is more than 3
//         * @param cluster
//         * @return
//         */
//        @Override
//        protected boolean shouldRenderAsCluster(Cluster<PlaceClusterItem> cluster) {
//            return cluster.getSize() > 3;
//        }
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

            //set the title
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(marker.getTitle());

            //set the snippet (i.e. # of reviews) text
            TextView snippet = (TextView) view.findViewById(R.id.snippet);
            snippet.setText(marker.getSnippet());

            //note: picasso only keeps a weak ref to the target so it may be gc-ed
            //use setTag so that target will be alive as long as the view is alive
            final Target target = ImageUtils.newTarget(getContext(), snippet, marker);
            snippet.setTag(target);
            ImageUtils.loadImage(getContext(),
                    clusterRenderer.getClusterItem(marker) != null ? clusterRenderer.getClusterItem(marker).getRatingUrl() : null, //TODO: provide placeholder image
                    target);

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

            //set the title
            final TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(marker.getTitle());

            //set first and second snippets
            final Cluster<PlaceClusterItem> cluster = clusterRenderer.getCluster(marker);
            final TextView firstSnippet = (TextView) view.findViewById(R.id.first_snippet);
            final TextView secondSnippet = (TextView) view.findViewById(R.id.second_snippet);
            if (cluster == null) {
                firstSnippet.setVisibility(View.GONE);
                secondSnippet.setVisibility(View.GONE);
            }
            else {
                double highestRating = 0.0;
                int numWithHighRating = 0;

                for (PlaceClusterItem clusterItem : cluster.getItems()) {
                    if (clusterItem.getRating() >= HIGH_RATING) {
                        ++numWithHighRating;
                    }
                    if (clusterItem.getRating() > highestRating) {
                        highestRating = clusterItem.getRating();
                    }
                }

                firstSnippet.setText(getResources().getQuantityString(R.plurals.map_cluster_infoWindow_first_snippet,
                        (int) Math.round(highestRating),
                        FormattingUtils.getDecimalFormatterNoRounding(1).format(highestRating)));
                if (highestRating < HIGH_RATING) {
                    //the highest rating place has less than 4 stars, so no need to report numWithHighRating
                    secondSnippet.setVisibility(View.GONE);
                }
                else {
                    secondSnippet.setText(getString(R.string.map_cluster_infoWindow_second_snippet, numWithHighRating));
                }
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
                TextView title = (TextView) view.findViewById(R.id.title);
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