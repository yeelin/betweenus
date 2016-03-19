package com.example.yeelin.projects.betweenus.data.google.model;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ninjakiki on 3/17/16.
 */
public class PlaceSearchResult implements LocalResult {
    private final String status;
    private final String[] html_attributions;
    private final String next_page_token;
    private final Place[] results;

    public PlaceSearchResult(String status, String[] html_attributions, String next_page_token, Place[] results) {
        this.status = status;
        this.html_attributions = html_attributions;
        this.next_page_token = next_page_token;
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public String[] getHtml_attributions() {
        return html_attributions;
    }

    public String getNext_page_token() {
        return next_page_token;
    }

    public Place[] getResults() {
        return results;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<results.length; i++) {
            stringBuilder.append("[" + results[i] + "]");
            if (i != results.length - 1) stringBuilder.append(",");
        }
        return stringBuilder.toString();
    }

    @Override
    public ArrayList<LocalBusiness> getLocalBusinesses() {
        return new ArrayList<LocalBusiness>(Arrays.asList(results));
    }

    @Override
    public String getAfterId() {
        return next_page_token;
    }

    @Override
    public String getNextUrl() {
        return next_page_token;
    }

    @Override
    public LatLng getResultCenter() {
        return null;
    }

    @Override
    public double getResultLatitudeDelta() {
        return 0;
    }

    @Override
    public double getResultLongitudeDelta() {
        return 0;
    }

    @Override
    public int getDataSource() {
        return LocalConstants.GOOGLE;
    }
}
