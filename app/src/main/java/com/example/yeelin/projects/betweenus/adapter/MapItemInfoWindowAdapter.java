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
    private SimpleArrayMap<Marker, View> markerToViewMap;

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
        markerToViewMap = new SimpleArrayMap<>();
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
     * Returns custom content for the info window given a marker
     * @param marker
     * @return
     */
    @Override
    public View getInfoContents(Marker marker) {
        Log.d(TAG, "getInfoContents");

        //check if view for the given marker already exists, and if so return it
        //there will be one view per marker instead of recycling views
        if (markerToViewMap.containsKey(marker)) {
            Log.d(TAG, "getInfoContents: Map contains view for marker so returning");
            return markerToViewMap.get(marker);
        }

        //doesn't exist, so inflate a new one
        Log.d(TAG, "getInfoContents: Map doesn't have view for marker so inflating a new one");
        View view = View.inflate(context, R.layout.map_info_contents, null);

        //set the title
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(marker.getTitle());

        //set the snippet (i.e. # of reviews) text
        TextView snippet = (TextView) view.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());

        //set the rating image
        String ratingUrl = idToRatingUrlMap.get(markerToIdPositionPairMap.get(marker).first);

//            //only uncomment this if you want to force a network fetch for the rating image
//            Picasso.with(getActivity())
//                    .invalidate(ratingUrl);

        //note: picasso only keeps a weak ref to the target so it may be gc-ed
        //use setTag so that target will be alive as long as the view is alive
        final Target target = ImageUtils.newTarget(context, snippet, marker);
        snippet.setTag(target);
        ImageUtils.loadImage(context, ratingUrl, target);

        //store the info window view so that we can return it if requested again
        markerToViewMap.put(marker, view);

        return view;
    }
}

