package com.example.betweenus.backend.yelp;

import com.github.scribejava.apis.LinkedInApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Token;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuthService;

import java.io.InputStream;

/**
 * Created by ninjakiki on 2/17/16.
 */
public class YelpApiHelper {
    //member variables
    private OAuthService service;
    private Token accessToken;

    /**
     * Constructor
     */
    public YelpApiHelper() {
        this(YelpConstants.CONSUMER_KEY, YelpConstants.CONSUMER_SECRET, YelpConstants.TOKEN, YelpConstants.TOKEN_SECRET);
    }

    /**
     * Constructor
     * @param consumerKey
     * @param consumerSecret
     * @param token
     * @param tokenSecret
     */
    private YelpApiHelper(String consumerKey, String consumerSecret, String token, String tokenSecret) {
        service = new ServiceBuilder()
                //.provider(TwoStepOAuth.class)
                .apiKey(consumerKey)
                .apiSecret(consumerSecret)
                .build(LinkedInApi.instance());
                //.build();
        accessToken = new Token(token, tokenSecret);
    }

    /**
     * Search API: Specify location by neighborhood, address, or city
     * Creates and sends a request to the Search API by term and location.
     *
     * @param searchTerm String of the search term to be queried
     * @param searchLimit
     * @param location String of the location
     * @param latitude
     * @param longitude
     * @return JSON Response
     */
    public InputStream searchForBusinessesByLocation(String searchTerm, String searchLimit,
                                                     String location, String latitude, String longitude) {
        OAuthRequest request = createOAuthRequest(YelpConstants.SEARCH_PATH);

        request.addQuerystringParameter(YelpConstants.TERM, searchTerm);
        request.addQuerystringParameter(YelpConstants.LOCATION, location);
        request.addQuerystringParameter(YelpConstants.LATLNG, String.format("%s,%s", latitude, longitude));
        request.addQuerystringParameter(YelpConstants.LIMIT, String.valueOf(searchLimit));

        return sendRequestAndGetResponse(request);
    }

    /**
     * Search API: Specify location by geographical bounding box
     * Creates and sends a request to the Search API by term and bounding box
     * @param searchTerm
     * @param searchLimit
     * @param latitudeSW
     * @param longitudeSW
     * @param latitudeNE
     * @param longitudeNE
     * @return
     */
    public InputStream searchForBusinessesByBoundingBox(String searchTerm, String searchLimit,
                                                        String latitudeSW, String longitudeSW, String latitudeNE, String longitudeNE) {
        OAuthRequest request = createOAuthRequest(YelpConstants.SEARCH_PATH);

        request.addQuerystringParameter(YelpConstants.TERM, searchTerm);
        request.addQuerystringParameter(YelpConstants.BOUNDS, String.format("%s,%s|%s,%s", latitudeSW, longitudeSW, latitudeNE, longitudeNE));
        request.addQuerystringParameter(YelpConstants.LIMIT, searchLimit);

        return sendRequestAndGetResponse(request);
    }

    /**
     * Search API: Specify location by geographical coordinate
     * Creates and sends a request to the Search API by term and lat/long center
     * @param searchTerm
     * @param searchRadius
     * @param searchLimit
     * @param latitude
     * @param longitude
     * @return
     */
    public InputStream searchForBusinessesByGeoCoords(String searchTerm, String searchRadius, String searchLimit,
                                                      String latitude, String longitude) {
        OAuthRequest request = createOAuthRequest(YelpConstants.SEARCH_PATH);

        request.addQuerystringParameter(YelpConstants.TERM, searchTerm);
        request.addQuerystringParameter(YelpConstants.LATLNG, String.format("%s,%s", latitude, longitude));
        request.addQuerystringParameter(YelpConstants.RADIUS_FILTER, searchRadius);
        request.addQuerystringParameter(YelpConstants.LIMIT, searchLimit);

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
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + YelpConstants.API_HOST + path, service);
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

        Response response = request.send();
        return response.getStream();
    }
}
