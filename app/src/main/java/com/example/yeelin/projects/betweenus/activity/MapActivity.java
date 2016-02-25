package com.example.yeelin.projects.betweenus.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.google.model.DirectionsResult;
import com.example.yeelin.projects.betweenus.data.google.model.Route;
import com.example.yeelin.projects.betweenus.loader.DirectionsLoaderCallbacks;
import com.example.yeelin.projects.betweenus.loader.callback.DirectionsLoaderListener;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.example.yeelin.projects.betweenus.utils.MapColorUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.squareup.picasso.Target;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ninjakiki on 11/12/15.
 */
public class MapActivity
        extends BaseActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        DirectionsLoaderListener {
    private static final String TAG = MapActivity.class.getCanonicalName();

    private static final String EXTRA_ID = MapActivity.class.getSimpleName() + ".id";
    private static final String EXTRA_NAME = MapActivity.class.getSimpleName() + ".name";
    private static final String EXTRA_LATLNG = MapActivity.class.getSimpleName() + ".latLng";
    private static final String EXTRA_TOGGLE_STATE = MapActivity.class.getSimpleName() + ".toggleState";
    private static final String EXTRA_RATING = MapActivity.class.getSimpleName() + ".rating";
    private static final String EXTRA_RATING_URL = MapActivity.class.getSimpleName() + ".ratingUrl";
    private static final String EXTRA_REVIEWS = MapActivity.class.getSimpleName() + ".reviews";
    private static final String EXTRA_LIKES = MapActivity.class.getSimpleName() + ".likes";
    private static final String EXTRA_NORMALIZED_LIKES = MapActivity.class.getSimpleName() + ".normalizedLikes";
    private static final String EXTRA_CHECKINS = MapActivity.class.getSimpleName() + ".checkins";
    private static final String EXTRA_USER_LATLNG = MapActivity.class.getSimpleName() + ".userLatLng";

    //request code
    public static final int REQUEST_LOCATION_PERMISSION = 200;

    //member variables
    private GoogleMap map;
    private DirectionsResult directionsResult;
    private Marker travelTimeMarker;
    private Polyline highlightedPolyline;
    private HashMap<Polyline, String> polyLineToTravelTimeMap;
    //place-related
    private String id;
    private String name;
    private LatLng latLng;
    private boolean toggleState;
    private double rating;
    private String ratingUrl;
    private int reviews;
    private int likes;
    private double normalizedLikes;
    private int checkins;
    //user-related
    private LatLng userLatLng;

    /**
     * Builds intent to start this activity
     * @param context
     * @param id
     * @param name
     * @param latLng
     * @param toggleState
     * @param rating
     * @param ratingUrl
     * @param reviews
     * @param likes
     * @param normalizedLikes
     * @param checkins
     * @return
     */
    public static Intent buildIntent(Context context, String id, String name, LatLng latLng, boolean toggleState,
                                     double rating, String ratingUrl, int reviews,
                                     int likes, double normalizedLikes, int checkins,
                                     LatLng userLatLng) {
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_LATLNG, latLng);
        intent.putExtra(EXTRA_TOGGLE_STATE, toggleState);

        intent.putExtra(EXTRA_RATING, rating);
        intent.putExtra(EXTRA_RATING_URL, ratingUrl);
        intent.putExtra(EXTRA_REVIEWS, reviews);

        intent.putExtra(EXTRA_LIKES, likes);
        intent.putExtra(EXTRA_NORMALIZED_LIKES, normalizedLikes);
        intent.putExtra(EXTRA_CHECKINS, checkins);

        intent.putExtra(EXTRA_USER_LATLNG, userLatLng);
        return intent;
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setup view and toolbar
        setContentView(R.layout.activity_map);
        setupToolbar(R.id.map_toolbar, true);

        //read extras
        Intent intent = getIntent();
        id = intent.getStringExtra(EXTRA_ID);
        name = intent.getStringExtra(EXTRA_NAME);
        latLng = intent.getParcelableExtra(EXTRA_LATLNG);
        toggleState = intent.getBooleanExtra(EXTRA_TOGGLE_STATE, false);

        rating = intent.getDoubleExtra(EXTRA_RATING, LocalConstants.NO_DATA_DOUBLE);
        ratingUrl = intent.getStringExtra(EXTRA_RATING_URL);
        reviews = intent.getIntExtra(EXTRA_REVIEWS, LocalConstants.NO_DATA_INTEGER);

        likes = intent.getIntExtra(EXTRA_LIKES, LocalConstants.NO_DATA_INTEGER);
        normalizedLikes = intent.getDoubleExtra(EXTRA_NORMALIZED_LIKES, LocalConstants.NO_DATA_DOUBLE);
        checkins = intent.getIntExtra(EXTRA_CHECKINS, LocalConstants.NO_DATA_INTEGER);

        userLatLng = intent.getParcelableExtra(EXTRA_USER_LATLNG);

        //load map asynchronously
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        //fetch directions and travel time
        Log.d(TAG, "onCreate: Calling directions loader");
        DirectionsLoaderCallbacks.initLoader(DirectionsLoaderCallbacks.DIRECTIONS,
                this, getSupportLoaderManager(), this, userLatLng, latLng);
    }

    /**
     * Handle user selection of menu options
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Up button was clicked
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: Up button clicked");
                navigateUpToParentActivity(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Callback when map is ready
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
        }

        //show user's current location on the map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.d(TAG, "onMapReady: shouldShowRequestPermissionRationale");
                //yes, show user the rationale before requesting permission
                Toast.makeText(this,
                        getString(R.string.location_permission_rationale),
                        Toast.LENGTH_LONG)
                        .show();
            }
            //ask user for permission
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
        else {
            //permission has been granted, continue as usual
            map.setMyLocationEnabled(true);
        }

        //for accessibility
        map.setContentDescription(getString(R.string.detail_map_contentDescription, name));
        //set a custom renderer for the contents of the info window
        map.setInfoWindowAdapter(new SimpleInfoWindowAdapter());
        //set click listener for the marker
        map.setOnMarkerClickListener(this);
        //set click listener for map
        map.setOnMapClickListener(this);

        //add marker for place
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(name)
                .snippet(id)
                .icon(MapColorUtils.determineMarkerIcon(toggleState, rating != LocalConstants.NO_DATA_DOUBLE ? rating : normalizedLikes));
        map.addMarker(markerOptions);

        //add marker for user
        MarkerOptions userMarkerOptions = new MarkerOptions()
                .icon(MapColorUtils.getUserMarkerIcon(this))
                .position(userLatLng);
        map.addMarker(userMarkerOptions);

//        LatLngBounds mapBounds = new LatLngBounds.Builder()
//                .include(latLng)
//                .include(userLatLng)
//                .build();

        //move camera to center of marker
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, getResources().getInteger(R.integer.default_detail_map_zoom)));
        //map.moveCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, 200));
        zoomMapToBounds(true, false);

        //if directions returned before map loaded, then we should show draw directions on the map now
        if (directionsResult != null) {
            Log.d(TAG, "onMapReady: Drawing directions immediately");
            drawRoute(directionsResult);
        }
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

    /**
     * Zooms the map to bounds based on specified parameters.
     * @param useDisplaySize
     * @param shouldAnimate
     */
    private void zoomMapToBounds(boolean useDisplaySize, boolean shouldAnimate) {
        final DisplayMetrics display = getResources().getDisplayMetrics();
        int toolbarHeight = getSupportActionBar() == null ? 0 : getSupportActionBar().getHeight();

        LatLngBounds mapBounds;
        int padding = display.widthPixels / 10;

        mapBounds = new LatLngBounds.Builder()
                    .include(userLatLng)
                    .include(latLng)
                    .build();
        padding += 150; //additional padding of 50 so that the markers are not clipped

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
     * Callback when a marker is clicked.  Do nothing since we want the default behavior
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: Marker was clicked");
        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    /**
     * Handle callback from DirectionsLoaderCallbacks
     * @param loaderId
     * @param directionsResult
     */
    @Override
    public void onLoadComplete(int loaderId, @Nullable DirectionsResult directionsResult) {
        Log.d(TAG, "onLoadComplete: DirectionsResult: " + directionsResult);

        //if the result is null, there is nothing to do
        if (directionsResult == null) return;

        //if the map is not ready, then store the result for now and update the map when it is ready
        if (map == null) {
            Log.d(TAG, "onLoadComplete: Map is not ready yet, saving directions result for later");
            this.directionsResult = directionsResult;
        }
        else {
            drawRoute(directionsResult);
        }
    }

    /**
     * Draw the directions route on the map
     * @param directionsResult
     */
    private void drawRoute(@NonNull DirectionsResult directionsResult) {
        final Route[] routes = directionsResult.getRoutes();
        if (routes == null) return;
        Log.d(TAG, "drawRoute: DirectionsResult num routes:" + routes.length);

        polyLineToTravelTimeMap = new HashMap<>(routes.length);
        //draw each route
        for (int i=0; i<routes.length; i++) {
            final List<LatLng> decodedPath = PolyUtil.decode(routes[i].getOverviewPolyline().getEncodedPath());
            final PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.GRAY)
                    .clickable(true) //enable clickability of polyline and listen for click events
                    .addAll(decodedPath);

            //get back the mutable polyline
            Polyline polyline = map.addPolyline(polylineOptions);
            polyLineToTravelTimeMap.put(polyline,
                    String.format("Route summary: %s\nDistance: %s\nTime: %s",
                            routes[i].getSummary(), routes[i].getLegs()[0].getDistance().getText(), routes[i].getLegs()[0].getDuration().getText()));
        }

        //listen to click events on polylines
        map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                Log.d(TAG, "onPolylineClick");

                //clear map of any previous residues
                removeAnyTravelTimeMarkers();
                removeAnyHighlightedPolylines();

                //highlight the new polyline
                highlightedPolyline = polyline;
                polyline.setColor(Color.DKGRAY);

                List<LatLng> points = polyline.getPoints();
                int middleIndex = points.size()/2;
                LatLng middlePoint = points.get(middleIndex);
                MarkerOptions middleMarkerOptions = new MarkerOptions()
                        .icon(MapColorUtils.getTravelTimeMarkerIcon(MapActivity.this, polyLineToTravelTimeMap.get(polyline)))
                        .position(middlePoint);
                travelTimeMarker = map.addMarker(middleMarkerOptions);
            }
        });
    }

    /**
     * Callback when there is a click anywhere on the map
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick");
        //clear out any travel time markers
        removeAnyTravelTimeMarkers();
        //un-highlight any polyline routes
        removeAnyHighlightedPolylines();
    }

    /**
     * Unhighlight any polyline routes if they are present.  The polyline color is reset to normal gray.
     */
    private void removeAnyHighlightedPolylines() {
        Log.d(TAG, "removeAnyHighlightedPolylines");
        if (highlightedPolyline != null) {
            highlightedPolyline.setColor(Color.GRAY);
        }
    }

    /**
     * Remove any travel time markers if they are present
     */
    private void removeAnyTravelTimeMarkers() {
        Log.d(TAG, "removeAnyTravelTimeMarkers");
        if (travelTimeMarker != null) {
            travelTimeMarker.remove();
        }
    }

    /**
     * SimpleInfoWindowAdapter
     * This class is an adapter for the info window of the marker
     */
    private class SimpleInfoWindowAdapter
            implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            //check if the click was for the place's marker
            //since we don't want to show an info window for anything other than a place marker
            if (!marker.getPosition().equals(latLng)) return null;

            final View view = View.inflate(MapActivity.this, R.layout.map_info_place, null);
            final TextView titleView = (TextView) view.findViewById(R.id.title);
            final TextView reviewsView = (TextView) view.findViewById(R.id.reviews);
            final TextView likesView = (TextView) view.findViewById(R.id.likes);
            final TextView checkinsView = (TextView) view.findViewById(R.id.checkins);

            //set title
            titleView.setText(name);

            //set rating and reviews
            if (rating == LocalConstants.NO_DATA_DOUBLE) {
                reviewsView.setVisibility(View.GONE);
            }
            else {
                reviewsView.setText(getString(R.string.review_count, reviews));
                //note: picasso only keeps a weak ref to the target so it may be gc-ed
                //use setTag so that target will be alive as long as the view is alive
                final Target target = ImageUtils.newTarget(MapActivity.this, reviewsView, marker, true);
                reviewsView.setTag(target);
                ImageUtils.loadImage(MapActivity.this,
                        ratingUrl, //TODO: provide placeholder rating image
                        target);
            }

            //set likes
            if (likes == LocalConstants.NO_DATA_INTEGER) {
                likesView.setVisibility(View.GONE);
            }
            else {
                likesView.setText(getResources().getQuantityString(R.plurals.short_like_count, likes, likes));
            }

            //set checkins
            if (checkins == LocalConstants.NO_DATA_INTEGER) {
                //we have yelp data
                checkinsView.setVisibility(View.GONE);
            }
            else {
                checkinsView.setText(getResources().getQuantityString(R.plurals.short_checkin_count, checkins, checkins));
            }

            return view;
        }
    }
}
