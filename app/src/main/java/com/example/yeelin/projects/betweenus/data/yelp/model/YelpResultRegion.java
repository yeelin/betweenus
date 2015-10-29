package com.example.yeelin.projects.betweenus.data.yelp.model;

/**
 * Created by ninjakiki on 7/23/15.
 */
public class YelpResultRegion {
    //Span of suggested map bounds
    private final Span span;
    //Center position of map bounds
    private final Center center;

    public YelpResultRegion(Span span, Center center) {
        this.span = span;
        this.center = center;
    }

    public Span getSpan() {
        return span;
    }

    public Center getCenter() {
        return center;
    }

    @Override
    public String toString() {
        return String.format("Span:%s, Center:%s", span, center);
    }

    /**
     * Span of suggested map bounds
     */
    public static class Span {
        //Latitude width of map bounds
        private final double latitude_delta;
        //Longitude height of map bounds
        private final double longitude_delta;

        public Span(double latitude_delta, double longitude_delta) {
            this.latitude_delta = latitude_delta;
            this.longitude_delta = longitude_delta;
        }

        public double getLatitude_delta() {
            return latitude_delta;
        }

        public double getLongitude_delta() {
            return longitude_delta;
        }

        @Override
        public String toString() {
            return String.format("LatDelta:%f, LongDelta:%f", latitude_delta, longitude_delta);
        }
    }

    /**
     * Center position of map bounds
     */
    public static class Center {
        //Latitude position of map bounds center
        private final double latitude;
        //Longitude position of map bounds center
        private final double longitude;

        public Center(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        @Override
        public String toString() {
            return String.format("Lat:%f, Long:%f", latitude, longitude);
        }
    }
}
