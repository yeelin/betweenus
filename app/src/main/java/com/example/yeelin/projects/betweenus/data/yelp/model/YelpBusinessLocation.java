package com.example.yeelin.projects.betweenus.data.yelp.model;

import com.example.yeelin.projects.betweenus.data.LocalBusinessLocation;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;

/**
 * Created by ninjakiki on 7/23/15.
 * Location data for a business
 */
public class YelpBusinessLocation implements LocalBusinessLocation {
    //Address for this business. Only includes address fields.
    private final String[] address;

    //Address for this business formatted for display. Includes all address fields, cross streets and city, state_code, etc.
    private final String[] display_address;

    //City for this business
    private final String city;

    //Cross streets for this business
    private final String cross_streets;

    //List that provides neighborhood(s) information for business
    private final String[] neighborhoods;

    //Coordinates for this business
    private final Coordinate coordinate;

    public YelpBusinessLocation(String[] address, String[] display_address, String city, String cross_streets, String[] neighborhoods, Coordinate coordinate) {
        this.address = address;
        this.display_address = display_address;
        this.city = city;
        this.cross_streets = cross_streets;
        this.neighborhoods = neighborhoods;
        this.coordinate = coordinate;
    }

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
    public String getShortDisplayAddress() {
        if (display_address == null || display_address.length == 0) return null;
        if (city != null)
            return display_address[0] + ", " + city;
        else
            return display_address[0];
    }

    @Override
    public String getLongDisplayAddress() {
        return getFullDisplayAddress();
    }

    @Override
    public String getCrossStreets() { return cross_streets; }

    @Override
    public LatLng getLatLng() {
        return new LatLng(coordinate.latitude, coordinate.longitude);
    }

    /**
     * Returns the full address by displaying each element of the display address on a new line.
     * @return
     */
    public String getFullDisplayAddress() {
        if (display_address == null || display_address.length == 0) return null;

        final StringBuilder addressBuilder = new StringBuilder();
        for (int i=0; i<display_address.length; i++) {
            addressBuilder.append(display_address[i]);
            if (i < display_address.length-1) {
                addressBuilder.append("\n");
            }
        }
        return addressBuilder.toString();
    }

    @Override
    public String toString() {
        return String.format("Address:%s, DisplayAddress:%s, City:%s, CrossStreets:%s, Neighborhoods:%s, Coordinates:%s",
                Arrays.toString(address), Arrays.toString(display_address), city, cross_streets, Arrays.toString(neighborhoods), coordinate);
    }

    /**
     * Location coordinates for a business
     */
    public static class Coordinate {
        //Latitude for this business
        private final double latitude;
        //Longitude for this business
        private final double longitude;

        public Coordinate(double latitude, double longitude) {
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
