package com.example.yeelin.projects.betweenus.data.yelp.query;

/**
 * Created by ninjakiki on 10/29/15.
 */
public class YelpConstants {
    /**
     * Update OAuth credentials below from the Yelp Developers API site:
     * http://www.yelp.com/developers/getting_started/api_access
     */
    public static final String CONSUMER_KEY = "XscMpy2EVnUv8N_g1KUqEg";
    public static final String CONSUMER_SECRET = "UMn3ASsDQ9n1SEzvRDR2rE1QEM0";
    public static final String TOKEN = "iEMsmq_91rS7C4poXa8hySvlldNP-5d5";
    public static final String TOKEN_SECRET = "DSEdDgz399mmvb3x_BjmCdOQekM";

    //url components
    public static final String API_HOST = "api.yelp.com";
    public static final String SEARCH_PATH = "/v2/search";
    public static final String BUSINESS_PATH = "/v2/business";

    //query parameters
    public static final String TERM = "term";
    public static final String LOCATION = "location";
    public static final String LATLNG = "ll";
    public static final String LIMIT = "limit";
    public static final String BOUNDS = "bounds";

    //values
    public static final int SEARCH_LIMIT = 20;
}
