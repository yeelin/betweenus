package com.example.yeelin.projects.betweenus.fragment;

import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
        GoogleMap.OnInfoWindowClickListener {
    //logcat
    private static final String TAG = SuggestionsMapFragment.class.getCanonicalName();

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
        super.onMapReady(googleMap);

        //set the click listener for the info window that is displayed when marker is tapped
        googleMap.setOnInfoWindowClickListener(this);
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


        //check if new suggestions are null
        if (newItems != null) {
            this.items = newItems;

            //loop through suggested items and add markers to map
            Log.d(TAG, "updateMap: Adding markers to map. Item count:" + newItems.size());
            for (int i = 0; i < newItems.size(); i++) {
                YelpBusiness business = newItems.get(i);

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(business.getLocation().getCoordinate().getLatitude(), business.getLocation().getCoordinate().getLongitude()))
                        .title(business.getName())
                        .snippet(getString(R.string.map_marker_snippet, business.getRating(), business.getReview_count()))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                map.addMarker(markerOptions);
            }
        }
    }
}
