package com.example.yeelin.projects.betweenus.data.fb.query;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.fb.json.FbJsonDeserializerHelper;
import com.example.yeelin.projects.betweenus.data.fb.model.FbPage;
import com.example.yeelin.projects.betweenus.data.fb.model.FbPagePhotos;
import com.example.yeelin.projects.betweenus.data.fb.model.FbResult;

import com.example.yeelin.projects.betweenus.utils.MapColorUtils;
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
    private static GraphRequest nextPlacesRequest;
    private static GraphRequest previousPlacesRequest;
    private static GraphRequest nextPhotosRequest;
    private static GraphRequest previousPhotosRequest;

    /**
     * Creates a fb graph api request to search for places around the given latlng.
     * The request is made synchronously so caller must make sure this method runs on a background thread.
     * @param context
     * @param accessToken
     * @param midLatLng
     * @param imageHeightPx
     * @param imageWidthPx
     */
    public static FbResult searchForPlaces(Context context, AccessToken accessToken, @NonNull LatLng midLatLng, int imageHeightPx, int imageWidthPx) {
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
                null,
                context.getString(R.string.facebook_api_version));

        //execute the request and wait (ok since we are on a bg thread)
        final GraphResponse response = request.executeAndWait();

        //store the next/previous places requests in case we need it later
        nextPlacesRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
        previousPlacesRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.PREVIOUS);

        //deserialize the response into a FbResult
        return processFbPlacesResponse(response);
    }

    /**
     * Creates a fb graph api request to get the next page of search results given a url and a paging direction
     * The request is made synchronously so caller must make sure this method runs on a background thread.
     * @param context
     * @param accessToken
     * @param url
     * @param pagingDirection
     * @return
     */
    public static FbResult searchForMorePlaces(Context context, AccessToken accessToken, @NonNull String url, int pagingDirection) {
        //use paging direction to determine which request to use
        GraphRequest request;
        if (pagingDirection == LocalConstants.NEXT_PAGE) request = nextPlacesRequest;
        else request = previousPlacesRequest;

        //execute the request and wait (ok since we are on a bg thread)
        final GraphResponse response = request.executeAndWait();

        //store the next/previous places request in case we need it later
        nextPlacesRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
        previousPlacesRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.PREVIOUS);

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
        Log.d(TAG, String.format("processFbPlacesResponse: Query:%s", response.getRequest()));
        final FbResult result = FbJsonDeserializerHelper.deserializeFbResponse(response.getRawResponse());

        //Remove any pages that are not Restaurant/cafe or Local business categories
        //This is needed to cleanup fb data
        return normalizeLikes(sanitizeFbPlacesResponse(result));
    }

    /**
     * Helper method that removes any result in the response that are likely not to be a restaurant or a local business.
     * @param result
     * @return
     */
    private static FbResult sanitizeFbPlacesResponse(@Nullable FbResult result) {
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
     * Helper method that computes the normalized likes so that it's on the same scale as Yelp ratings [0, 0.5, ..., 4.5, 5.0] inclusive.
     * @param result
     * @return
     */
    private static FbResult normalizeLikes(@Nullable FbResult result) {
        Log.d(TAG, "normalizeLikes: Normalizing likes");

        if (result != null) {
            int minLikes = 0;
            int maxLikes = 0;

            //loop through result to find min and max of dataset
            for (int i = 0; i < result.getLocalBusinesses().size(); i++) {
                final LocalBusiness business = result.getLocalBusinesses().get(i);
                if (business.getLikes() < minLikes) minLikes = business.getLikes();
                if (business.getLikes() > maxLikes) maxLikes = business.getLikes();
            }

            //compute diff, number of buckets, and bucket width
            int diff = maxLikes - minLikes;
            int buckets = MapColorUtils.NUM_RATING_BUCKETS-1;
            double bucketWidth = 1.0 * diff / buckets;
            Log.d(TAG, String.format("normalizeLikes: Min:%d, Max:%d, Diff:%d, BucketWidth:%f", minLikes, maxLikes, diff, bucketWidth));

            //normalize data
            for (int i = 0; i < result.getLocalBusinesses().size(); i++) {
                final FbPage page = (FbPage) result.getLocalBusinesses().get(i);
                double normalizedLikesOn10PointScale = (page.getLikes() - minLikes) / bucketWidth;
                long roundedNormalizedLikesOn10PointScale = Math.round(normalizedLikesOn10PointScale);
                double normalizedLikesOn5PointScale = roundedNormalizedLikesOn10PointScale / 2.0;
                page.setNormalizedLikes(normalizedLikesOn5PointScale);
                Log.d(TAG, String.format("normalizeLikes: Name:%s, Likes:%d, 10PtScale:%.2f, 5PtScale:%.2f", page.getName(), page.getLikes(), normalizedLikesOn10PointScale, normalizedLikesOn5PointScale));
            }
        }
        return result;
    }

    /**
     * Creates a fb graph api request to retrieve the details about a single place given its id.
     * The request is made synchronously so caller must make sure this method runs on a background thread.
     * @param context
     * @param accessToken
     * @param id
     * @param imageHeightPx
     * @param imageWidthPx
     */
    public static FbPage getPlaceDetails(Context context, AccessToken accessToken, String id, int imageHeightPx, int imageWidthPx) {
        //create the parameters for the request
        Bundle parameters = new Bundle();
        parameters.putString(FbConstants.ParamNames.FIELDS, FbConstants.ParamValues.buildDetailFields(imageHeightPx, imageWidthPx));

        //create the graph request
        final GraphRequest request = new GraphRequest(
                accessToken,
                "/" + id,
                parameters,
                HttpMethod.GET,
                null,
                context.getString(R.string.facebook_api_version));
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
        Log.d(TAG, String.format("processFbPlaceDetailsResponse: Query:%s", response.getRequest()));
        return FbJsonDeserializerHelper.deserializeFbSingleResponse(response.getRawResponse());
    }

    /**
     * Creates a fb graph api request to retrieve the photo urls for a single place given its id.
     * This request is made synchronously so caller must make sure that this method runs on a background thread.
     * @param context
     * @param accessToken
     * @param id
     * @return
     */
    public static FbPagePhotos getPlacePhotos(Context context, AccessToken accessToken, @NonNull String id) {

        Bundle parameters = new Bundle(2);
        parameters.putString(FbConstants.ParamNames.TYPE, FbConstants.ParamValues.TYPE_UPLOADED);
        parameters.putString(FbConstants.ParamNames.FIELDS, FbConstants.ParamValues.buildPhotosFields());

        //create te graph request
        final GraphRequest request = new GraphRequest(
                accessToken,
                "/" + id + FbConstants.Endpoints.PHOTOS,
                parameters,
                HttpMethod.GET,
                null,
                context.getString(R.string.facebook_api_version));


        //execute the request and wait (ok since we are on a bg thread)
        final GraphResponse response = GraphRequest.executeAndWait(request);

        //store the next/previous photos requests in case we need it later
        nextPhotosRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
        previousPhotosRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.PREVIOUS);

        return processFbPlacePhotosResponse(response);
    }

    /**
     * Creates a fb graph api request to get the next page of search results given a url and a paging direction.
     * The request is made synchronously so caller must make sure this method runs on a background thread.
     * @param context
     * @param accessToken
     * @param url
     * @param pagingDirection
     * @return
     */
    public static FbPagePhotos getMorePlacePhotos(Context context, AccessToken accessToken, @NonNull String url, int pagingDirection) {
        //use paging direction to determine which request to use
        GraphRequest request;
        if (pagingDirection == LocalConstants.NEXT_PAGE) request = nextPhotosRequest;
        else request = previousPhotosRequest;

        //execute the request and wait (ok since we are on a bg thread)
        final GraphResponse response = GraphRequest.executeAndWait(request);

        //store the next/previous photos requests in case we need it later
        nextPhotosRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
        previousPhotosRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.PREVIOUS);

        return processFbPlacePhotosResponse(response);
    }

    /**
     * Helper method that deserializes the response from a graph api photos request into an
     * array of photo urls and processes the photo urls.
     * @param response
     * @return
     */
    private static FbPagePhotos processFbPlacePhotosResponse(GraphResponse response) {
        Log.d(TAG, String.format("processFbPlacePhotosResponse: Query:%s", response.getRequest()));
        //deserialize response
        final FbPagePhotos fbPagePhotos = FbJsonDeserializerHelper.deserializeFbPhotosResponse(response.getRawResponse());
        //process photos
        fbPagePhotos.processPhotos();
        return fbPagePhotos;
    }
}
