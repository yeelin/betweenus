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

import java.util.Iterator;

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
     * @param imageHeightPx
     * @param imageWidthPx
     * @param listener
     */
    public static void searchForPlaces(AccessToken accessToken, LatLng midLatLng, int imageHeightPx, int imageWidthPx,
                                       final SuggestionsLoaderListener listener) {
        //create the parameters for the request
        Bundle parameters = new Bundle();
        parameters.putString(FbConstants.ParamNames.QUERY, FbConstants.ParamValues.QUERY_RESTAURANT);
        parameters.putString(FbConstants.ParamNames.TYPE, FbConstants.ParamValues.TYPE_PLACE);
        parameters.putString(FbConstants.ParamNames.CENTER, String.format("%f,%f", midLatLng.latitude, midLatLng.longitude));
        parameters.putString(FbConstants.ParamNames.DISTANCE, FbConstants.ParamValues.DISTANCE_THREE_MILE_RADIUS);
        parameters.putString(FbConstants.ParamNames.LIMIT, FbConstants.ParamValues.LIMIT_20);
        parameters.putString(FbConstants.ParamNames.FIELDS, FbConstants.ParamValues.buildSimpleFields(imageHeightPx, imageWidthPx));

        //create the graph request
        final GraphRequest request = new GraphRequest(
                accessToken,
                FbConstants.Endpoints.SEARCH,
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        Log.d(TAG, String.format("searchForPlaces: Raw:%s, Query:%s", response.getRawResponse(), response.getRequest()));
                        final FbResult result = FbJsonDeserializerHelper.deserializeFbResponse(response.getRawResponse());

                        //Remove any pages that are not Restaurant/cafe or Local business categories
                        //This is needed to cleanup fb data
                        if (result != null) {
                            for (Iterator<FbPage> iterator = result.getPages().iterator(); iterator.hasNext();) {
                                FbPage page = iterator.next();
                                if (!page.getCategory().startsWith(FbConstants.Response.CATEGORY_RESTAURANT) &&
                                        !page.getCategory().startsWith(FbConstants.Response.CATEGORY_LOCAL)) {
                                    iterator.remove();
                                }
                            }
                        }

                        //notify the listener that we have the result
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
    public static void getPlaceDetails(AccessToken accessToken, String id, int imageHeightPx, int imageWidthPx,
                                       final SingleSuggestionLoaderListener listener) {
        //create the parameters for the request
        Bundle parameters = new Bundle();
        parameters.putString(FbConstants.ParamNames.FIELDS, FbConstants.ParamValues.buildDetailFields(imageHeightPx, imageWidthPx));

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
