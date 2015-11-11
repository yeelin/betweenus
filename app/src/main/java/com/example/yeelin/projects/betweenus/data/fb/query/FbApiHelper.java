package com.example.yeelin.projects.betweenus.data.fb.query;

import android.os.Bundle;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.fb.json.FbJsonDeserializerHelper;
import com.example.yeelin.projects.betweenus.data.fb.model.FbPage;
import com.example.yeelin.projects.betweenus.data.fb.model.FbResult;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by ninjakiki on 10/29/15.
 */
public class FbApiHelper {
    //logcat
    private static final String TAG = FbApiHelper.class.getCanonicalName();

    /**
     * Creates a fb graph api request to search for places around the given latlng.
     * The request is made synchronously so caller must make sure this method runs on a background thread.
     * @param accessToken
     * @param midLatLng
     * @param imageHeightPx
     * @param imageWidthPx
     */
    public static FbResult searchForPlaces(AccessToken accessToken, LatLng midLatLng, int imageHeightPx, int imageWidthPx) {
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
                HttpMethod.GET);

        //execute the request and wait (ok since we are on a bg thread)
        final GraphResponse response = request.executeAndWait();

        //deserialize the response into a FbResult
        return processFbPlacesResponse(response);
    }

    /**
     * Helper method that deserializes the response from a graph api places search into a FbResult.
     * Also removes any result in the response that are likely not to be a restaurant or a local business.
     * @param response
     * @return
     */
    private static FbResult processFbPlacesResponse(GraphResponse response) {
        Log.d(TAG, String.format("processFbPlacesResponse: Raw:%s, Query:%s", response.getRawResponse(), response.getRequest()));
        final FbResult result = FbJsonDeserializerHelper.deserializeFbResponse(response.getRawResponse());

        //Remove any pages that are not Restaurant/cafe or Local business categories
        //This is needed to cleanup fb data
        return sanitizeFbPlacesResponse(result);
    }

    /**
     * Helper method that removes any result in the response that are likely not to be a restaurant or a local business.
     * @param result
     * @return
     */
    private static FbResult sanitizeFbPlacesResponse(FbResult result) {
        if (result != null) {
            for (Iterator<FbPage> iterator = result.getPages().iterator(); iterator.hasNext();) {
                FbPage page = iterator.next();
                boolean validCategory = false;
                boolean validCategoryList = false;

                //check category field to see if it contains "restaurant" or "local"
                String category = page.getCategory().toLowerCase();
                if (category.contains(FbConstants.Response.CATEGORY_RESTAURANT) || category.contains(FbConstants.Response.CATEGORY_LOCAL)) {
                    validCategory = true;
                }

                //if category is valid, then check category_list field to see if any one of the categories contain "restaurant"
                if (validCategory) {
                    String[] categoryArray = page.getCategoryList();
                    for (int i = 0; i < categoryArray.length; i++) {
                        category = categoryArray[i].toLowerCase();
                        if (category.contains(FbConstants.Response.CATEGORY_RESTAURANT) || category.contains(FbConstants.Response.CATEGORY_LOCAL)) {
                            validCategoryList = true;
                            break;
                        }
                    }
                }


                //if both validCategory and validCategoryList are true, this result is good, so don't remove.
                //otherwise, remove the result
                if (!(validCategory && validCategoryList)) {
                    Log.d(TAG, String.format("searchForPlaces: Not one category is restaurant. Name:%s, Category:%s, CategoryList:%s",
                            page.getName(), page.getCategory(), Arrays.toString(page.getCategoryList())));
                    iterator.remove();
                }
            }
        }
        return result;
    }

    /**
     * Creates a fb graph api request to retrieve the details about a single place given its id.
     * The request is made synchronously so caller must make sure this method runs on a background thread.
     * @param accessToken
     * @param id
     */
    public static FbPage getPlaceDetails(AccessToken accessToken, String id, int imageHeightPx, int imageWidthPx) {
        //create the parameters for the request
        Bundle parameters = new Bundle();
        parameters.putString(FbConstants.ParamNames.FIELDS, FbConstants.ParamValues.buildDetailFields(imageHeightPx, imageWidthPx));

        //create the graph request
        final GraphRequest request = new GraphRequest(
                accessToken,
                "/" + id,
                parameters,
                HttpMethod.GET);
        //execute the request and wait (ok since we are on a bg thread)
        final GraphResponse response = GraphRequest.executeAndWait(request);
        return processFbPlaceDetailsResponse(response);
    }

    /**
     * Helper method that deserializes the response from a graph api places search into a FbPage.
     * @param response
     * @return
     */
    private static FbPage processFbPlaceDetailsResponse(GraphResponse response) {
        Log.d(TAG, String.format("processFbPlaceDetailsResponse: Raw:%s, Query:%s", response.getRawResponse(), response.getRequest()));
        return FbJsonDeserializerHelper.deserializeFbSingleResponse(response.getRawResponse());
    }
}
