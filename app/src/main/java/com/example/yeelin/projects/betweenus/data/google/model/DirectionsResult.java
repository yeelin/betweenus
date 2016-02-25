package com.example.yeelin.projects.betweenus.data.google.model;

import java.util.Arrays;

/**
 * Created by ninjakiki on 2/22/16.
 */
public class DirectionsResult {
    private final GeocodedWaypoint[] geocoded_waypoints;
    private final Route[] routes;

    public DirectionsResult(GeocodedWaypoint[] geocodedWaypoints, Route[] routes) {
        this.geocoded_waypoints = geocodedWaypoints.clone();
        this.routes = routes.clone();
    }

    public GeocodedWaypoint[] getGeocodedWaypoints() { return geocoded_waypoints; }

    public Route[] getRoutes() { return routes; }

    @Override
    public String toString() {
        return String.format("Waypoints:%s, Routes:%s",
                Arrays.toString(geocoded_waypoints), Arrays.toString(routes));
    }

    /**
     * GeocodedWaypoint
     */
    public static class GeocodedWaypoint {
        private final String geocoder_status; //ok, zero results, partial_match
        private final String place_id;

        public GeocodedWaypoint(String geocoderStatus, String placeId) {
            this.geocoder_status = geocoderStatus;
            this.place_id = placeId;
        }

        public String getGeocoderStatus() {
            return geocoder_status;
        }

        public String getPlaceId() {
            return place_id;
        }

        @Override
        public String toString() {
            return String.format("GeoStatus:%s, PlaceId:%s", geocoder_status, place_id);
        }
    }
}
