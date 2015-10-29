package com.example.yeelin.projects.betweenus.data.fb.query;

import android.os.Bundle;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.fb.json.FbJsonDeserializerHelper;
import com.example.yeelin.projects.betweenus.data.fb.model.FbResult;
import com.example.yeelin.projects.betweenus.loader.LoaderId;
import com.example.yeelin.projects.betweenus.loader.SuggestionsLoaderCallbacks;
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
    public static void searchForPlaces(AccessToken accessToken, LatLng midLatLng, final SuggestionsLoaderCallbacks.SuggestionsLoaderListener listener) {
        //create the parameters for the request
        Bundle parameters = new Bundle();
        parameters.putString(FbConstants.QUERY, FbConstants.REQUEST_QUERY);
        parameters.putString(FbConstants.TYPE, FbConstants.REQUEST_TYPE);
        parameters.putString(FbConstants.CENTER, String.format("%f,%f", midLatLng.latitude, midLatLng.longitude));
        parameters.putString(FbConstants.DISTANCE, FbConstants.REQUEST_DISTANCE);
        parameters.putString(FbConstants.FIELDS, FbConstants.REQUEST_FIELDS);
        Log.d(TAG, String.format("searchForPlaces: MidLatLng:%f,%f", midLatLng.latitude, midLatLng.longitude));

        //create the graph request
        GraphRequest request = new GraphRequest(
                accessToken,
                FbConstants.SEARCH_ENDPOINT,
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        Log.d(TAG, "Raw: " + response.getRawResponse());
                        Log.d(TAG, "Query: " + response.getRequest().toString());

                        final FbResult result = FbJsonDeserializerHelper.deserializeFbResponse(response.getRawResponse());
                        listener.onLoadComplete(LoaderId.MULTI_PLACES, result); //TODO: Using the loaderId here is a total hack
                    }
                });
        GraphRequest.executeBatchAsync(request);
    }
}
