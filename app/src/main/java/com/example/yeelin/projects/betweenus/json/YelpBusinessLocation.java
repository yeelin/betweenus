package com.example.yeelin.projects.betweenus.json;

import java.util.Arrays;

/**
 * Created by ninjakiki on 7/23/15.
 * Location data for a business
 */
public class YelpBusinessLocation {
    //Address for this business. Only includes address fields.
    private String[] address;

    //Address for this business formatted for display. Includes all address fields, cross streets and city, state_code, etc.
    private String[] display_address;

    //City for this business
    private String city;

    //Cross streets for this business
    private String cross_streets;

    //List that provides neighborhood(s) information for business
    private String[] neighborhoods;

    //Coordinates for this business
    private Coordinate coordinate;

    public String[] getAddress() {
        return address;
    }

    public String[] getDisplay_address() {
        return display_address;
    }

    public String getCity() {
        return city;
    }

    public String getCross_streets() {
        return cross_streets;
    }

    public String[] getNeighborhoods() {
        return neighborhoods;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public String toString() {
        return String.format("Address:%s, DisplayAddress:%s, City:%s, CrossStreets:%s, Neighborhoods:%s, Coordinates:%s",
                Arrays.toString(address), Arrays.toString(display_address), city, cross_streets, Arrays.toString(neighborhoods), coordinate);
    }

    /**
     * Location coordinates for a business
     */
    static class Coordinate {
        //Latitude for this business
        private double latitude;
        //Longitude for this business
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
