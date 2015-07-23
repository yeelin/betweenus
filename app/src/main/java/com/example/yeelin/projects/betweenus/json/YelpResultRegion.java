package com.example.yeelin.projects.betweenus.json;

/**
 * Created by ninjakiki on 7/23/15.
 */
public class YelpResultRegion {
    //Span of suggested map bounds
    private Span span;
    //Center position of map bounds
    private Center center;

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
    static class Span {
        //Latitude width of map bounds
        private double latitude_delta;
        //Longitude height of map bounds
        private double longitude_delta;

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
    static class Center {
        //Latitude position of map bounds center
        private double latitude;
        //Longitude position of map bounds center
        private double longitude;

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
