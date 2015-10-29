package com.example.yeelin.projects.betweenus.data.yelp.query;

import android.util.Log;


import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import java.io.InputStream;

/**
 * Created by ninjakiki on 7/20/15.
 */
public class YelpApiHelper {
    //logcat
    private static final String TAG = YelpApiHelper.class.getCanonicalName();

    //member variables
    private OAuthService service;
    private Token accessToken;

    /**
     * Constructor
     */
    public YelpApiHelper() {
        this.service = new ServiceBuilder()
                        .provider(TwoStepOAuth.class)
                        .apiKey(YelpConstants.CONSUMER_KEY)
                        .apiSecret(YelpConstants.CONSUMER_SECRET)
                        .build();
        this.accessToken = new Token(YelpConstants.TOKEN, YelpConstants.TOKEN_SECRET);
    }

    /**
     * Constructor
     * @param consumerKey
     * @param consumerSecret
     * @param token
     * @param tokenSecret
     */
    public YelpApiHelper(String consumerKey, String consumerSecret, String token, String tokenSecret) {
        this.service = new ServiceBuilder()
                        .provider(TwoStepOAuth.class)
                        .apiKey(consumerKey)
                        .apiSecret(consumerSecret)
                        .build();
        this.accessToken = new Token(token, tokenSecret);
    }

    /**
     * Search API: Specify location by neighborhood, address, or city
     * Creates and sends a request to the Search API by term and location.
     *
     * @param searchTerm String of the search term to be queried
     * @param location String of the location
     * @return JSON Response
     */
    public InputStream searchForBusinessesByLocation(String searchTerm,
                                                     String location, double latitude, double longitude) {
        OAuthRequest request = createOAuthRequest(YelpConstants.SEARCH_PATH);

        request.addQuerystringParameter(YelpConstants.TERM, searchTerm);
        request.addQuerystringParameter(YelpConstants.LOCATION, location);
        request.addQuerystringParameter(YelpConstants.LATLNG, String.format("%f,%f", latitude, longitude));
        request.addQuerystringParameter(YelpConstants.LIMIT, String.valueOf(YelpConstants.SEARCH_LIMIT));

        return sendRequestAndGetResponse(request);
    }

    /**
     * Search API: Specify location by geographical bounding box
     * Creates and sends a request to the Search API by term and bounding box
     * @param searchTerm
     * @param latitudeSW
     * @param longitudeSW
     * @param latitudeNE
     * @param longitudeNE
     * @return
     */
    public InputStream searchForBusinessesByBoundingBox(String searchTerm,
                                                        double latitudeSW, double longitudeSW, double latitudeNE, double longitudeNE) {
        OAuthRequest request = createOAuthRequest(YelpConstants.SEARCH_PATH);

        request.addQuerystringParameter(YelpConstants.TERM, searchTerm);
        request.addQuerystringParameter(YelpConstants.BOUNDS, String.format("%f,%f|%f,%f", latitudeSW, longitudeSW, latitudeNE, longitudeNE));
        request.addQuerystringParameter(YelpConstants.LIMIT, String.valueOf(YelpConstants.SEARCH_LIMIT));

        return sendRequestAndGetResponse(request);
    }

    /**
     * Search API: Specify location by geographical coordinate
     * Creates and sends a request to the Search API by term and lat/long center
     * @param searchTerm
     * @param latitude
     * @param longitude
     * @return
     */
    public InputStream searchForBusinessesByGeoCoords(String searchTerm,
                                                      double latitude, double longitude) {
        OAuthRequest request = createOAuthRequest(YelpConstants.SEARCH_PATH);

        request.addQuerystringParameter(YelpConstants.TERM, searchTerm);
        request.addQuerystringParameter(YelpConstants.LATLNG, String.format("%f,%f", latitude, longitude));
        request.addQuerystringParameter(YelpConstants.LIMIT, String.valueOf(YelpConstants.SEARCH_LIMIT));

        return sendRequestAndGetResponse(request);
    }

    /**
     * Business API
     * Creates and sends a request to the Business API by business ID.
     *
     * @param businessID business ID of the requested business
     * @return JSON Response
     */
    public InputStream searchByBusinessId(String businessID) {
        OAuthRequest request = createOAuthRequest(YelpConstants.BUSINESS_PATH + "/" + businessID);

        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and returns an OAuthRequest based on the API endpoint specified.
     *
     * @param path API endpoint to be queried
     * @return OAuthRequest
     */
    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + YelpConstants.API_HOST + path);
        return request;
    }

    /**
     * Sends an OAuthRequest and returns the Response body.
     *
     * @param request OAuthRequest corresponding to the API request
     * @return Stream of API response
     */
    private InputStream sendRequestAndGetResponse(OAuthRequest request) {
        this.service.signRequest(this.accessToken, request);

        Log.d(TAG, "sendRequestAndGetResponse: Complete URL:" + request.getCompleteUrl());
        Log.d(TAG, "sendRequestAndGetResponse: Header: " + request.getHeaders());

        Response response = request.send();
        return response.getStream();
    }

    /**
     * Queries the Search API based on the command line arguments and takes the first result to query
     * the Business API.
     *
     * @param yelpApi
     * @param term
     * @param location
     */
//    private static void queryAPI(YelpApiHelper yelpApi, String term, String location) {
//
//        String searchResponseJSON = yelpApi.searchForBusinessesByLocation(term, location);
//
//        JSONParser parser = new JSONParser();
//        JSONObject response = null;
//        try {
//            response = (JSONObject) parser.parse(searchResponseJSON);
//        } catch (ParseException pe) {
//            System.out.println("Error: could not parse JSON response:");
//            System.out.println(searchResponseJSON);
//            System.exit(1);
//        }
//
//        JSONArray businesses = (JSONArray) response.get("businesses");
//        JSONObject firstBusiness = (JSONObject) businesses.get(0);
//        String firstBusinessID = firstBusiness.get("id").toString();
//        System.out.println(String.format(
//                "%s businesses found, querying business info for the top result \"%s\" ...",
//                businesses.size(), firstBusinessID));
//
//        // Select the first business and display business details
//        String businessResponseJSON = yelpApi.searchByBusinessId(firstBusinessID.toString());
//        System.out.println(String.format("Result for business \"%s\" found:", firstBusinessID));
//        System.out.println(businessResponseJSON);
//    }
}
