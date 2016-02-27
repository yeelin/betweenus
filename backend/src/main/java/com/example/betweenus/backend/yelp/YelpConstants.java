package com.example.betweenus.backend.yelp;

/**
 * Created by ninjakiki on 2/17/16.
 */
public abstract class YelpConstants {
    /**
     * Update OAuth credentials below from the Yelp Developers API site:
     * http://www.yelp.com/developers/getting_started/api_access
     */
    public static final String CONSUMER_KEY = "XscMpy2EVnUv8N_g1KUqEg";
    public static final String CONSUMER_SECRET = "UMn3ASsDQ9n1SEzvRDR2rE1QEM0";
    public static final String TOKEN = "iEMsmq_91rS7C4poXa8hySvlldNP-5d5";
    public static final String TOKEN_SECRET = "DSEdDgz399mmvb3x_BjmCdOQekM";

    //url components
    public static final String HTTP = "http://";
    public static final String API_HOST = "api.yelp.com";
    public static final String SEARCH_PATH = "/v2/search";
    public static final String BUSINESS_PATH = "/v2/business";

    //query parameters
    public static final String TERM = "term";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String LATLNG = "ll";
    public static final String LIMIT = "limit";
    public static final String BOUNDS = "bounds";
    public static final String RADIUS_FILTER = "radius_filter";
    public static final String ID = "id";
}
