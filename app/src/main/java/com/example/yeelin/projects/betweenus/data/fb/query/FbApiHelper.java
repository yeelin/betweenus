package com.example.yeelin.projects.betweenus.data.fb.query;

import android.os.Bundle;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.fb.json.FbJsonDeserializerHelper;
import com.example.yeelin.projects.betweenus.data.fb.model.FbPage;
import com.example.yeelin.projects.betweenus.data.fb.model.FbResult;
import com.example.yeelin.projects.betweenus.loader.LoaderId;
import com.example.yeelin.projects.betweenus.loader.callback.SingleSuggestionLoaderListener;
import com.example.yeelin.projects.betweenus.loader.callback.SuggestionsLoaderListener;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ninjakiki on 10/29/15.
 */
public class FbApiHelper {
    //logcat
    private static final String TAG = FbApiHelper.class.getCanonicalName();

    /**
     * Creates a fb graph api request to search for places around the given latlng.
     * The request is made asynchronously so the caller must provide a callback.
     * @param accessToken
     * @param midLatLng
     * @param listener
     */
    public static void searchForPlaces(AccessToken accessToken, LatLng midLatLng, final SuggestionsLoaderListener listener) {
        //create the parameters for the request
        Bundle parameters = new Bundle();
        parameters.putString(FbConstants.QUERY, FbConstants.REQUEST_QUERY);
        parameters.putString(FbConstants.TYPE, FbConstants.REQUEST_TYPE);
        parameters.putString(FbConstants.CENTER, String.format("%f,%f", midLatLng.latitude, midLatLng.longitude));
        parameters.putString(FbConstants.DISTANCE, FbConstants.REQUEST_DISTANCE);
        parameters.putString(FbConstants.FIELDS, FbConstants.REQUEST_FIELDS_SIMPLE);
        Log.d(TAG, String.format("searchForPlaces: MidLatLng:%f,%f", midLatLng.latitude, midLatLng.longitude));

        //create the graph request
        final GraphRequest request = new GraphRequest(
                accessToken,
                FbConstants.SEARCH_ENDPOINT,
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        Log.d(TAG, String.format("searchForPlaces: Raw:%s, Query:%s", response.getRawResponse(), response.getRequest()));

                        final FbResult result = FbJsonDeserializerHelper.deserializeFbResponse(response.getRawResponse());
                        listener.onLoadComplete(LoaderId.MULTI_PLACES, result); //TODO: Using the loaderId here is a total hack
                    }
                });
        GraphRequest.executeBatchAsync(request);
    }

    /**
     * Creates a fb graph api request to retrieve the details about a single place given its id.
     * The request is made asynchronously so the caller must provide a callback.
     * @param accessToken
     * @param id
     * @param listener
     */
    public static void getPlaceDetails(AccessToken accessToken, String id, final SingleSuggestionLoaderListener listener) {
        //create the parameters for the request
        Bundle parameters = new Bundle();
        parameters.putString(FbConstants.FIELDS, FbConstants.REQUEST_FIELDS_DETAIL);

        //create the graph request
        GraphRequest request = new GraphRequest(
                accessToken,
                "/" + id,
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        Log.d(TAG, String.format("getPlaceDetails: Raw:%s, Query:%s", response.getRawResponse(), response.getRequest()));

                        FbPage page = FbJsonDeserializerHelper.deserializeFbSingleResponse(response.getRawResponse());
                        listener.onLoadComplete(LoaderId.SINGLE_PLACE, page);
                    }
                });
        GraphRequest.executeBatchAsync(request);
    }
}
