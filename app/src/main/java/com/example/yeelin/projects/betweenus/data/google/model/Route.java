package com.example.yeelin.projects.betweenus.data.google.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;

/**
 * Created by ninjakiki on 2/22/16.
 */
public class Route {
    private final String summary;
    private final Polyline overview_polyline;
    private final Bounds bounds;
    private final String copyrights;
    private final Leg[] legs;

    public Route(String summary, Polyline overviewPolyline, Bounds bounds, String copyrights, Leg[] legs) {
        this.summary = summary;
        this.overview_polyline = overviewPolyline;
        this.bounds = bounds;
        this.copyrights = copyrights;
        this.legs = legs;
    }

    public String getSummary() {
        return summary;
    }

    public Polyline getOverviewPolyline() {
        return overview_polyline;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public String getCopyrights() {
        return copyrights;
    }

    public Leg[] getLegs() {
        return legs;
    }

    @Override
    public String toString() {
        return String.format("Summary:%s, Polyline:%s, Legs:%s",
                summary, overview_polyline, Arrays.toString(legs));
    }

    public static class Polyline {
        private final String points;

        public Polyline(String points) {
            this.points = points;
        }

        public String getEncodedPath() {
            return points;
        }

        @Override
        public String toString() {
            return points;
        }
    }

    /**
     * Bounds
     */
    public static class Bounds {
        private final Point northeast;
        private final Point southwest;

        public Bounds(Point ne, Point sw) {
            this.northeast = ne;
            this.southwest = sw;
        }

        public LatLng getNortheast() {
            return new LatLng(northeast.getLatitude(), northeast.getLongitude());
        }

        public LatLng getSouthwest() {
            return new LatLng(southwest.getLatitude(), southwest.getLongitude());
        }

        @Override
        public String toString() {
            return String.format("(Ne:%s),(Sw:%s)");
        }
    }

    /**
     * Point
     */
    public static class Point {
        private final double latitude;
        private final double longitude;

        public Point(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }

        @Override
        public String toString() {
            return String.format("%.2f,%.2f", latitude, longitude);
        }
    }
}
