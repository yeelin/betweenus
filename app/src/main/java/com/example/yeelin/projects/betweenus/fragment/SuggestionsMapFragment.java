package com.example.yeelin.projects.betweenus.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by ninjakiki on 7/28/15.
 */
public class SuggestionsMapFragment
        extends BaseMapFragment
        implements OnSuggestionsLoadedCallback, //tells fragment when data is loaded
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
    private YelpResult yelpResult;
    private SimpleArrayMap<Marker, String> markerToIdMap = new SimpleArrayMap<>();
    private SimpleArrayMap<String, String> idToRatingUrlMap = new SimpleArrayMap<>();
    private OnSuggestionItemClickListener itemClickListener;

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
            itemClickListener = (OnSuggestionItemClickListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement OnSuggestionItemClickListener");
        }
    }

    /**
     * Nullify the click listener
     */
    @Override
    public void onDetach() {
        itemClickListener = null;
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
        googleMap.setInfoWindowAdapter(new MapItemInfoWindowAdapter());

        //request for a callback when the map has finished rendering so that we can animate the camera
        googleMap.setOnMapLoadedCallback(this);


    }

    /**
     * OnSuggestionsLoadedCallback implementation
     * The loader has finished fetching the data.  This method is called by SuggestionsActivity to update the view.
     * @param yelpResult
     */
    public void onSuggestionsLoaded(@Nullable YelpResult yelpResult) {
        //debugging purposes
        if (yelpResult == null) {
            Log.d(TAG, "onSuggestionsLoaded: SuggestedItems is null. Loader must be resetting");
        } else {
            Log.d(TAG, "onSuggestionsLoaded: Item count:" + yelpResult.getBusinesses().size());
        }

        //check if map is null
        if (map == null) {
            Log.d(TAG, "onSuggestionsLoaded: Map is null, so nothing to do");
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
        //clear out the markerToId map as well
        markerToIdMap.clear();
        idToRatingUrlMap.clear();

        //check if new suggestions are null
        if (newYelpResult != null) {
            this.yelpResult = newYelpResult;

            //check if new items > 0 so that we don't create the builder unnecessarily
            Log.d(TAG, "updateMap: Adding markers to map. Item count:" + newYelpResult.getBusinesses().size());
            if (newYelpResult.getBusinesses().size() > 0) {
                //make sure the markerToId map can hold all the markers we are about to add
                markerToIdMap.ensureCapacity(newYelpResult.getBusinesses().size());
                idToRatingUrlMap.ensureCapacity(newYelpResult.getBusinesses().size());
                //LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                //loop through suggested items and:
                //1. place markers on the map
                //2. add markers to the markerToId map
                for (int i = 0; i < newYelpResult.getBusinesses().size(); i++) {
                    YelpBusiness business = newYelpResult.getBusinesses().get(i);

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(new LatLng(business.getLocation().getCoordinate().getLatitude(), business.getLocation().getCoordinate().getLongitude()))
                            .title(business.getName())
                            //.snippet(getString(R.string.map_marker_snippet, business.getRating(), business.getReview_count()))
                            .snippet(getString(R.string.review_count, business.getReview_count()))
                            .icon(BitmapDescriptorFactory.defaultMarker(HUE_PRIMARY));
                    Marker marker = map.addMarker(markerOptions);
                    markerToIdMap.put(marker, business.getId());
                    idToRatingUrlMap.put(business.getId(), business.getRating_img_url_large());
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

        if (cameraPosition == null && yelpResult != null) {
            //read the yelp result region so that we can specify the map bounds
            LatLngBounds mapBounds = computeMapBoundsFromYelpResult();
            //animate the camera over to the region specified by bounds
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(mapBounds, PADDING_BOUNDING_BOX));
        }
    }

    /**
     * Helper method that read the yelp result region and returns them as bounds
     * for the map
     * @return
     */
    private LatLngBounds computeMapBoundsFromYelpResult() {
        //read the yelp result region so that we can specify the map bounds
        YelpResultRegion yelpResultRegion = yelpResult.getRegion();

        //get region center
        LatLng center = new LatLng(
                yelpResultRegion.getCenter().getLatitude(),
                yelpResultRegion.getCenter().getLongitude());
        //get delta of lat/long from the center
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
        itemClickListener.onSuggestionClick(businessId);
    }

    /**
     *
     */
    private class MapItemInfoWindowAdapter
            implements GoogleMap.InfoWindowAdapter {
        private View view;
        private TextView title;
        private TextView snippet;

        public MapItemInfoWindowAdapter() {
            super();
            view = View.inflate(getActivity(), R.layout.map_info_contents, null);
            title = (TextView) view.findViewById(R.id.title);
            snippet = (TextView) view.findViewById(R.id.snippet);
        }

        /**
         * The API will first call getInfoWindow(Marker) and if null is returned,
         * it will then call getInfoContents(Marker).
         * @param marker
         * @return
         */
        @Override
        public View getInfoWindow(Marker marker) {
            Log.d(TAG, "getInfoWindow");
            return null;
        }

        /**
         *
         * @param marker
         * @return
         */
        @Override
        public View getInfoContents(Marker marker) {
            Log.d(TAG, "getInfoContents");
            String businessId = markerToIdMap.get(marker);
            String ratingUrl = idToRatingUrlMap.get(businessId);

            title.setText(marker.getTitle());
            //set the textview and the yelp stars
            snippet.setText(marker.getSnippet());
            //note: picasso only keeps a weak ref to the target so it may be gc-ed
            //use setTag so that target will be alive as long as the view is alive
            final Target target = ImageUtils.newTarget(getActivity(), snippet, marker);
            snippet.setTag(target);
            ImageUtils.loadImage(getActivity(), ratingUrl, target);

            return view;
        }
    }
}
