package com.example.yeelin.projects.betweenus.data.google.query;

/**
 * Created by ninjakiki on 2/22/16.
 */
public abstract class GoogleConstants {
    //query parameters for directions
    public static class DirectionsParamNames {
        public static final String ORIGIN = "origin";
        public static final String DESTINATION = "destination";
    }

    //query parameters for distance matrix
    public static class DistanceMatrixParamNames {
        public static final String ORIGINS = "origins";
        public static final String DESTINATIONS = "destinations";
    }

    //query parameters for nearby search
    public static class NearbySearchParamNames {
        public static final String LOCATION = "location";
        public static final String RADIUS = "radius";
        public static final String TYPE = "type";
    }

    //query parameters for text search
    public static class TextSearchParamNames {
        public static final String QUERY = "query";
        public static final String LOCATION = "location";
        public static final String RADIUS = "radius";
        public static final String TYPE = "type";
    }

    //query parameters for place details
    public static class PlaceDetailsParamNames {
        public static final String PLACE_ID = "placeid";
    }

    //query parameters for place photos
    public static class PlacePhotosParamNames {
        public static final String PHOTO_REFERENCE = "photoreference";
        public static final String MAX_HEIGHT = "maxheight";
        public static final String MAX_WIDTH = "maxwidth";
    }
}
