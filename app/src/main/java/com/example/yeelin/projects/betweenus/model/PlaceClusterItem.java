package com.example.yeelin.projects.betweenus.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by ninjakiki on 9/29/15.
 */
public class PlaceClusterItem implements ClusterItem {
    private final LatLng position;
    private final String id;
    private final String title;
    private final String snippet;
    private final String ratingUrl;
    private final int resultPosition;

    public PlaceClusterItem(LatLng position, String id, String title, String snippet, String ratingUrl, int resultPosition) {
        this.position = position;

        this.id = id;
        this.title = title;
        this.snippet = snippet;
        this.ratingUrl = ratingUrl;
        this.resultPosition = resultPosition;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getRatingUrl() {
        return ratingUrl;
    }

    public int getResultPosition() {
        return resultPosition;
    }
}
