package com.example.yeelin.projects.betweenus.data.google.model;

import com.example.yeelin.projects.betweenus.data.LocalBusinessLocation;
import com.google.android.gms.maps.model.LatLng;

/**
 * Place Geometry
 */
public class PlaceGeometry implements LocalBusinessLocation {
    private final PlaceLocation location;

    public PlaceGeometry(PlaceLocation location) {
        this.location = location;
    }

    public PlaceLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("PlaceLocation:%s", location);
    }

    @Override
    public String getShortDisplayAddress() {
        return null;
    }

    @Override
    public String getLongDisplayAddress() {
        return null;
    }

    @Override
    public String getCrossStreets() {
        return null;
    }

    @Override
    public LatLng getLatLng() {
        return new LatLng(location.getLat(), location.getLng());
    }

    /**
     * Place location
     */
    public static class PlaceLocation {
        private final double lat;
        private final double lng;

        public PlaceLocation(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }
}