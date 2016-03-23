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
 * https://developers.google.com/places/web-service/search#PlaceSearchResponses
 */
public class PlaceSearchResult implements LocalResult {
    //possible values: OK, ZERO_RESULTS, OVER_QUERY_LIMIT, REQUEST_DENIED, INVALID_REQUEST
    private final String status;
    //result may also contain attribution information which must be displayed to the user.
    private final String[] html_attributions;

    //By default, each Nearby Search or Text Search returns up to 20 establishment results per query;
    // however, each search can return as many as 60 results, split across three pages.
    // If your search will return more than 20, then the search response will include an additional value
    // — next_page_token. Pass the value of the next_page_token to the pagetoken parameter of a new search
    // to see the next set of results.
    // If the next_page_token is null, or is not returned, then there are no further results.
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
        return LocalConstants.NO_DATA_DOUBLE;
    }

    @Override
    public double getResultLongitudeDelta() {
        return LocalConstants.NO_DATA_DOUBLE;
    }

    @Override
    public int getDataSource() {
        return LocalConstants.GOOGLE;
    }
}
