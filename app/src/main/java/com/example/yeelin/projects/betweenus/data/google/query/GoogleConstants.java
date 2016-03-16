package com.example.yeelin.projects.betweenus.data.google.query;

/**
 * Created by ninjakiki on 2/22/16.
 */
public abstract class GoogleConstants {
    //query parameters
    public static class DirectionsParamNames {
        public static final String ORIGIN = "origin";
        public static final String DESTINATION = "destination";
    }

    public static class DistanceMatrixParamNames {
        public static final String ORIGINS = "origins";
        public static final String DESTINATIONS = "destinations";
    }
}
