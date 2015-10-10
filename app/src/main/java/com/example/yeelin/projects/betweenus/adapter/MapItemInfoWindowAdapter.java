package com.example.yeelin.projects.betweenus.adapter;

import android.content.Context;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.utils.ImageUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Target;

/**
 * Created by ninjakiki on 8/6/15.
 * Creates custom content for the info window for each marker.
 * The info window is the view that is displayed when a marker is clicked.
 */
public class MapItemInfoWindowAdapter
        implements GoogleMap.InfoWindowAdapter {
    //logcat
    private static final String TAG = MapItemInfoWindowAdapter.class.getCanonicalName();

    //member variables
    private Context context;
    private SimpleArrayMap<Marker, Pair<String,Integer>> markerToIdPositionPairMap;
    private SimpleArrayMap<String, String> idToRatingUrlMap;
    private SimpleArrayMap<Marker, View> markerToInfoWindowMap;
    private Marker userLocationMarker;
    private Marker friendLocationMarker;

    /**
     * Constructor
     * @param context
     * @param markerToIdPositionPairMap
     * @param idToRatingUrlMap
     */
    public MapItemInfoWindowAdapter(Context context, SimpleArrayMap<Marker, Pair<String,Integer>> markerToIdPositionPairMap, SimpleArrayMap<String, String> idToRatingUrlMap) {
        super();
        this.context = context;
        this.markerToIdPositionPairMap = markerToIdPositionPairMap;
        this.idToRatingUrlMap = idToRatingUrlMap;

        markerToInfoWindowMap = new SimpleArrayMap<>();
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
        Log.d(TAG, "getInfoWindow");
        return null;
    }

    /**
     * Returns custom content for the info window given a marker.
     * Checks markerToInfoWindowMap to see if an info window already exists for the marker.
     * 1. If it does, it returns the existing info window.
     * 2. Otherwise, it inflates a new info window depending on whether it is a people location or a place marker.
     *
     * @param marker
     * @return
     */
    @Override
    public View getInfoContents(Marker marker) {
        Log.d(TAG, "getInfoContents");

        //check if view for the given marker already exists, and if so return it
        //there will be one view per marker instead of recycling views
        if (markerToInfoWindowMap.containsKey(marker)) {
            Log.d(TAG, "getInfoContents: Map has infoWindow for marker so returning");
            return markerToInfoWindowMap.get(marker);
        }

        //doesn't exist, so inflate a new one
        final View view;

        //check if this marker is the userLocationMarker or the friendLocationMarker
        if ((userLocationMarker != null && userLocationMarker.getTitle().equalsIgnoreCase(marker.getTitle())) ||
                (friendLocationMarker != null && friendLocationMarker.getTitle().equalsIgnoreCase(marker.getTitle()))) {
            //yes, this is either the user or friend marker, so inflate map_info_people_location view
            Log.d(TAG, "getInfoContents: Map doesn't have infoWindow for marker so inflating a new map_info_people_location window");
            view = View.inflate(context, R.layout.map_info_people_location, null);
        }
        else {
            //not user or friend marker, so inflate map_info_place view
            Log.d(TAG, "getInfoContents: Map doesn't have infoWindow for marker so inflating a new map_info_place window");
            view = View.inflate(context, R.layout.map_info_place, null);

            //set the snippet (i.e. # of reviews) text
            TextView snippet = (TextView) view.findViewById(R.id.snippet);
            snippet.setText(marker.getSnippet());

            //set the rating image
            final Pair<String,Integer> businessIdPositionPair = markerToIdPositionPairMap.get(marker);
            final String ratingUrl = idToRatingUrlMap.get(businessIdPositionPair.first);

//            //only uncomment this if you want to force a network fetch for the rating image
//            Picasso.with(getActivity())
//                    .invalidate(ratingUrl);

            //note: picasso only keeps a weak ref to the target so it may be gc-ed
            //use setTag so that target will be alive as long as the view is alive
            final Target target = ImageUtils.newTarget(context, snippet, marker, true);
            snippet.setTag(target);
            ImageUtils.loadImage(context, ratingUrl, target);
        }

        //set the title
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        //store the info window view so that we can return it if requested again
        markerToInfoWindowMap.put(marker, view);
        return view;
    }

    /**
     * Setter for userLocationMarker, used by SuggestionsMapFragment
     * @param userLocationMarker
     */
    public void setUserLocationMarker(Marker userLocationMarker) {
        this.userLocationMarker = userLocationMarker;
    }

    /**
     * Setter for friendLocationMarker, used by SuggestionsMapFragment
     * @param friendLocationMarker
     */
    public void setFriendLocationMarker(Marker friendLocationMarker) {
        this.friendLocationMarker = friendLocationMarker;
    }
}

