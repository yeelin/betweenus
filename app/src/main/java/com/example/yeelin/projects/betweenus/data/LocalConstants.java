package com.example.yeelin.projects.betweenus.data;

/**
 * Created by ninjakiki on 11/6/15.
 */
public interface LocalConstants {
    /**
     * Possible data sources
     */
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
