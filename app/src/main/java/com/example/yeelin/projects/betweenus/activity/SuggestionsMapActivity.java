package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.SimpleArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.loader.LoaderId;
import com.example.yeelin.projects.betweenus.loader.SuggestionsLoaderCallbacks;
import com.example.yeelin.projects.betweenus.model.PlaceClusterItem;
import com.example.yeelin.projects.betweenus.model.SimplifiedBusiness;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.example.yeelin.projects.betweenus.model.YelpResult;
import com.example.yeelin.projects.betweenus.model.YelpResultRegion;
import com.example.yeelin.projects.betweenus.receiver.PlacesBroadcastReceiver;
import com.example.yeelin.projects.betweenus.service.PlacesService;
import com.example.yeelin.projects.betweenus.utils.FormattingUtils;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.example.yeelin.projects.betweenus.utils.MapColorUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
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
 * Created by ninjakiki on 10/2/15.
 */
public class SuggestionsMapActivity
        extends FragmentActivity
        implements
        PlacesBroadcastReceiver.PlacesBroadcastListener,
        SuggestionsLoaderCallbacks.SuggestionsLoaderListener,
        OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<PlaceClusterItem>,
        ClusterManager.OnClusterInfoWindowClickListener<PlaceClusterItem>,
        ClusterManager.OnClusterItemClickListener<PlaceClusterItem>,
        ClusterManager.OnClusterItemInfoWindowClickListener<PlaceClusterItem> {

    private static final String TAG = SuggestionsMapActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_SEARCH_TERM = SuggestionsMapActivity.class.getSimpleName() + ".searchTerm";
    private static final String EXTRA_PLACE_IDS = SuggestionsMapActivity.class.getSimpleName() + ".placeIds";

    //constants
    private static final double HIGH_RATING = 4.0;

    //member variables
    //map-related
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private ClusterManager<PlaceClusterItem> clusterManager;
    private PlaceClusterRenderer clusterRenderer;
    private PlaceClusterItemInfoWindowAdapter clusterItemInfoWindowAdapter;
    private PlaceClusterInfoWindowAdapter clusterInfoWindowAdapter;
    private Marker userLocationMarker; //for showing user's location marker
    private Marker friendLocationMarker; //for showing friend's location marker
    private boolean mapNeedsUpdate = false;

    //data
    private YelpResult result;
    private String searchTerm;
    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    private ArrayMap<String,Integer> selectedIdsMap = new ArrayMap<>();
    private PlacesBroadcastReceiver placesBroadcastReceiver;
    //allows us to retrieve data back later
    private ArrayMap<String, PlaceClusterItem> idToClusterItemMap = new ArrayMap<>(); //needed to toggle the marker color
    private SparseArray<String> clusterSizeToTitleMap = new SparseArray<>();
    private SparseArray<String> highestRatingToFirstSnippetMap = new SparseArray<>();
    private SparseArray<String> placesWithHighRatingToSecondSnippetMap = new SparseArray<>();

    /**
     * Builds intent to start this activity
     * @param context
     * @param searchTerm
     * @param userPlaceId
     * @param friendPlaceId
     * @return
     */
    public static Intent buildIntent(Context context, String searchTerm, String userPlaceId, String friendPlaceId) {
        Intent intent = new Intent(context, SuggestionsMapActivity.class);

        //put extras
        intent.putExtra(EXTRA_SEARCH_TERM, searchTerm);

        ArrayList<String> placeIds = new ArrayList<>(2);
        placeIds.add(userPlaceId);
        placeIds.add(friendPlaceId);
        intent.putStringArrayListExtra(EXTRA_PLACE_IDS, placeIds);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //read extras from intent
        Intent intent = getIntent();
        searchTerm = intent.getStringExtra(EXTRA_SEARCH_TERM);
        ArrayList<String> placeIds = intent.getStringArrayListExtra(EXTRA_PLACE_IDS);

        //start service to fetch suggestions from the network
        Log.d(TAG, "onCreate: Starting PlacesService");
        startService(PlacesService.buildGetPlaceByIdIntent(this, placeIds));

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.support_map_fragment);
        supportMapFragment.getMapAsync(this);
    }

    /** Create a broadcast receiver and register for place broadcasts (success and failures)
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Registering for broadcasts");
        placesBroadcastReceiver = new PlacesBroadcastReceiver(this, this); //we are a place broadcast listener
    }

    /**
     * Unregister for place broadcasts
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Unregistering for broadcasts");
        placesBroadcastReceiver.unregister();
        super.onPause();
    }

    /**
     * PlacesBroadcastReceiver.PlacesBroadcastListener callback
     * We have successfully retrieved the latlng for the user and friend, so
     * call the loader to fetch data from Yelp.
     *
     * @param userLatLng
     * @param friendLatLng
     */
    @Override
    public void onPlacesSuccess(LatLng userLatLng, LatLng friendLatLng) {
        Log.d(TAG, String.format("onPlacesSuccess: User:%s Friend:%s", userLatLng, friendLatLng));

        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        midLatLng = LocationUtils.computeMidPoint(userLatLng, friendLatLng);

        //initializing the loader to fetch suggestions from the network
        SuggestionsLoaderCallbacks.initLoader(this, getSupportLoaderManager(), this, searchTerm, userLatLng, friendLatLng, midLatLng);
    }

    /**
     * PlacesBroadcastReceiver.PlacesBroadcastListener callback
     * We failed to retrieve the latlng for the user and friend, so display a snackbar
     * to inform the user as there is not much else we can do.  The snackbar allows the user to go back to the Location Entry screen.
     * TODO: Implement retry instead of just go back.
     *
     * @param statusCode
     * @param statusMessage
     */
    @Override
    public void onPlacesFailure(int statusCode, String statusMessage) {
        Log.d(TAG, String.format("onPlacesFailure: StatusCode:%d, Message:%s", statusCode, statusMessage));

        //create a snackbar to inform the user
        final View rootView = findViewById(R.id.root_layout);
        if (rootView != null) {
            final Snackbar snackbar = Snackbar.make(rootView, getString(R.string.get_place_by_id_error), Snackbar.LENGTH_LONG);
            //provide an action link on the snackbar to go back to the location entry screen
//            snackbar.setAction(getString(R.string.snackbar_go_back), new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log.d(TAG, "onPlacesFailure.onClick: Going back to Location Entry screen");
//                    navigateUpToParentActivity(SuggestionsMapActivity.this);
//                }
//            });
        }
    }

    /**
     * SuggestionsLoaderCallbacks.SuggestionsLoaderListener callback
     * When the loader delivers the results, this method would be called.  Depending on which fragment is in view,
     * the data would be passed to the appropriate fragment.
     * @param loaderId
     * @param result
     */
    @Override
    public void onLoadComplete(LoaderId loaderId, @Nullable YelpResult result) {
        if (loaderId != LoaderId.MULTI_PLACES) {
            Log.d(TAG, "onLoadComplete: Unknown loaderId:" + loaderId);
            return;
        }

        if (result == null) {
            Log.d(TAG, "onLoadComplete: SuggestedItems is null. Loader must be resetting");
            return;
        }

        Log.d(TAG, "onLoadComplete: Item count:" + result.getBusinesses().size());
        //reset the member variables
        this.result = result;

        Log.d(TAG, "onLoadComplete: Loading map");
        //check if map is null
        if (map == null) {
            Log.d(TAG, "onLoadComplete: Map is still null, so nothing to do now");
            mapNeedsUpdate = true; //tell ourselves that we need to update the map later when it is ready
        }
        else {
            //map is not null and the result is different so update the map
            updateMap();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        //initialize cluster manager and cluster renderer
        clusterManager = new ClusterManager<>(this, map);
        clusterRenderer = new PlaceClusterRenderer(this, map, clusterManager);
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
     * Updates the map with the result
     * This is similar to what the SuggestionsAdapter does in updateAllItems().
     */
    private void updateMap() {
        Log.d(TAG, "updateMap");

        //check if result is null
        if (result != null && result.getBusinesses().size() > 0) {
            Log.d(TAG, "updateMap: Adding cluster items to map. Count:" + result.getBusinesses().size());

            //make sure the idToClusterItemMap has enough space
            idToClusterItemMap.ensureCapacity(result.getBusinesses().size());

            //add cluster items and call cluster!
            addClusterItemsToClusterManager();
            clusterManager.cluster();
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

            //put it in the idToClusterItemMap
            idToClusterItemMap.put(business.getId(), clusterItem);
        }

        //add to the cluster manager in bulk
        clusterManager.addItems(idToClusterItemMap.values());
    }

    /**
     * Called when the cluster is clicked
     * @param cluster A collection of ClusterItems that are nearby each other.
     * @return
     */
    @Override
    public boolean onClusterClick(Cluster<PlaceClusterItem> cluster) {
        //Log.d(TAG, "onClusterClick: A cluster was clicked");
        return false;
    }

    /**
     * Called when the cluster's info window is clicked
     * @param cluster A collection of ClusterItems that are nearby each other.
     */
    @Override
    public void onClusterInfoWindowClick(Cluster<PlaceClusterItem> cluster) {
        //Log.d(TAG, "onClusterInfoWindowClick: A cluster's info window was clicked");
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
        //Log.d(TAG, "onClusterItemClick: A cluster item was clicked");

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
        //Log.d(TAG, "onClusterItemInfoWindowClick: A cluster item's info window was clicked");

        //notify the activity that a suggestion was clicked
        onSuggestionClick(placeClusterItem.getId(),
                placeClusterItem.getTitle(),
                placeClusterItem.getPosition(),
                placeClusterItem.getResultPosition());
    }

    /**
     * OnSuggestionActionListener implementation
     * Start the pager activity
     * @param id business id
     * @param name business name
     * @param latLng business latlng
     * @param position position of item in pager
     */
    public void onSuggestionClick(String id, String name, LatLng latLng, int position) {
        Log.d(TAG, String.format("onSuggestionClick: BusinessId:%s, Name:%s, Position:%d", id, name, position));

        final Intent pagerIntent = SuggestionsPagerActivity.buildIntent(this,
                position,
                buildSimplifiedBusinessList(),
                new ArrayList<>(selectedIdsMap.keySet()),
                new ArrayList<>(selectedIdsMap.values()),
                userLatLng, friendLatLng, midLatLng);
        startActivityForResult(pagerIntent, 100);
    }

    /**
     * Helper method to build the simplified business items array list for marshalling across to the
     * pager activity.
     * @return
     */
    private ArrayList<SimplifiedBusiness> buildSimplifiedBusinessList() {
        Log.d(TAG, "buildSimplifiedBusinessList");
        final ArrayList<SimplifiedBusiness> simplifiedBusinesses = new ArrayList<>(result.getBusinesses().size());

        for (int i=0; i<result.getBusinesses().size(); i++) {
            final YelpBusiness business = result.getBusinesses().get(i);
            simplifiedBusinesses.add(SimplifiedBusiness.newInstance(business));
        }
        return simplifiedBusinesses;
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
        final double latDelta = resultRegion.getSpan().getLatitude_delta();
        final double longDelta = resultRegion.getSpan().getLongitude_delta();

        //compute ne and sw from center
        final LatLng ne = new LatLng(center.latitude + latDelta/2, center.longitude + longDelta/2);
        final LatLng sw = new LatLng(center.latitude - latDelta/2, center.longitude - longDelta/2);

        //create bounds object
        return new Pair<>(sw, ne);
    }

    /**
     * Zooms the map to bounds based on specified parameters.
     * @param includePeople if true, then bounds will include user and friend's locations. Additional padding is provided.
     * @param useDisplaySize if true, then base map size on display size since layout hasn't happened yet
     * @param shouldAnimate if true, then animate the transition
     */
    private void zoomMapToBounds(final boolean includePeople, final boolean useDisplaySize, final boolean shouldAnimate) {
        final Pair<LatLng, LatLng> pairBounds = computePairBoundsFromResult();
        final DisplayMetrics display = getResources().getDisplayMetrics();

        final LatLngBounds mapBounds;
        final int padding = display.widthPixels / 10;

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
                    .icon(MapColorUtils.determineMarkerIcon(SuggestionsMapActivity.this, selectedIdsMap.containsKey(item.getId())));
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
            final View view = View.inflate(SuggestionsMapActivity.this, R.layout.map_info_place, null);

            //set the title
            final TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(marker.getTitle());

            //set the snippet (i.e. # of reviews) text
            final TextView snippet = (TextView) view.findViewById(R.id.snippet);
            snippet.setText(marker.getSnippet());

            //note: picasso only keeps a weak ref to the target so it may be gc-ed
            //use setTag so that target will be alive as long as the view is alive
            final PlaceClusterItem placeclusterItem = clusterRenderer.getClusterItem(marker);
            final Target target = ImageUtils.newTarget(SuggestionsMapActivity.this, snippet, marker);
            snippet.setTag(target);
            ImageUtils.loadImage(SuggestionsMapActivity.this,
                    placeclusterItem != null ? placeclusterItem.getRatingUrl() : null, //TODO: provide placeholder image
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
            final View view = View.inflate(SuggestionsMapActivity.this, R.layout.map_info_cluster, null);

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

                //check if the first snippet string already exists for this highest rating
                //reuse it if it does, so that we don't do another resource read
                int roundedHighestRating = (int) Math.round(highestRating);
                String clusterFirstSnippet = highestRatingToFirstSnippetMap.get(roundedHighestRating);
                if (clusterFirstSnippet == null) {
                    clusterFirstSnippet = getResources().getQuantityString(
                            R.plurals.map_cluster_infoWindow_first_snippet,
                            roundedHighestRating,
                            FormattingUtils.getDecimalFormatterNoRounding(1).format(highestRating));
                    highestRatingToFirstSnippetMap.put(roundedHighestRating, clusterFirstSnippet);
                }
                firstSnippet.setText(clusterFirstSnippet);

                //check if the highest rating place has less than 4 stars
                //if so no need to report numWithHighRating
                if (highestRating < 4) {
                    secondSnippet.setVisibility(View.GONE);
                }
                else {
                    //check if the second snippet string already exists for number with high ratings
                    //reuse it if it does, so that we don't do another resource read
                    String clusterSecondSnippet = placesWithHighRatingToSecondSnippetMap.get(numWithHighRating);
                    if (clusterSecondSnippet == null) {
                        clusterSecondSnippet = getString(R.string.map_cluster_infoWindow_second_snippet, numWithHighRating);
                        placesWithHighRatingToSecondSnippetMap.put(numWithHighRating, clusterSecondSnippet);
                    }
                    secondSnippet.setText(clusterSecondSnippet);
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

                final View view = View.inflate(SuggestionsMapActivity.this, R.layout.map_info_people_location, null);
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
