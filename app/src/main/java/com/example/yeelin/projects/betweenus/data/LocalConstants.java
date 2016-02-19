package com.example.yeelin.projects.betweenus.data;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ninjakiki on 11/6/15.
 */
public interface LocalConstants {

    /**
     * Servlet Constants
     */
    String SCHEME = "https";
    String AUTHORITY = "betweenus-3636.appspot.com";
    String YELP_PATH = "yelp";
    String SEARCH_PATH = "search";
    String FETCH_PATH = "fetch";

    String REQUEST_METHOD = "GET";
    int CONNECT_TIMEOUT_MILLIS = 15000;
    int READ_TIMEOUT_MILLIS = 15000;

    /**
     * Possible data sources
     */
    @IntDef({YELP, FACEBOOK, GOOGLE})
    @Retention(RetentionPolicy.SOURCE)
    @interface DataSourceId {}
    int YELP = 0;
    int FACEBOOK = 1;
    int GOOGLE = 2;

    /**
     * Indicates that there was no data for the particular field
     */
    int NO_DATA_INTEGER = -1;
    double NO_DATA_DOUBLE = -1.0;

    /**
     * Indicates that paging is being performed in the forward or backward direction.
     */
    int NEXT_PAGE = 1;
    int PREVIOUS_PAGE = 0;
}
