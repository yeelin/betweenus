package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.example.yeelin.projects.betweenus.utils.MapColorUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Target;

/**
 * Created by ninjakiki on 11/12/15.
 */
public class MapActivity
        extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
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
                                     int likes, double normalizedLikes, int checkins) {
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

        //load map asynchronously
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
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
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

        final UiSettings mapUiSettings = googleMap.getUiSettings();
        if (mapUiSettings != null) {
            //do not allow user to access maps and navigation app from the map, the toolbar is distracting
            mapUiSettings.setMapToolbarEnabled(false);
        }

        //show user's current location on the map
        googleMap.setMyLocationEnabled(true);
        //for accessibility
        googleMap.setContentDescription(getString(R.string.detail_map_contentDescription, name));
        //set a custom renderer for the contents of the info window
        googleMap.setInfoWindowAdapter(new SimpleInfoWindowAdapter());
        //set click listener for the marker
        googleMap.setOnMarkerClickListener(this);

        //add marker
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(name)
                .snippet(id)
                .icon(MapColorUtils.determineMarkerIcon(toggleState, rating != LocalConstants.NO_DATA_DOUBLE ? rating : normalizedLikes));
        googleMap.addMarker(markerOptions);

        //move camera to center of marker
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, getResources().getInteger(R.integer.default_detail_map_zoom)));
    }

    /**
     *
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
