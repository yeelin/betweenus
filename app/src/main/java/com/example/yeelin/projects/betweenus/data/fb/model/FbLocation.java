package com.example.yeelin.projects.betweenus.data.fb.model;

import com.example.yeelin.projects.betweenus.data.LocalBusinessLocation;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ninjakiki on 10/28/15.
 */
public class FbLocation implements LocalBusinessLocation {
    private final String city;
    private final String country;
    private final double latitude;
    //private String located_in;
    private final double longitude;
    //private String name;
    private final String state;
    private final String street;
    private final String zip;

    public FbLocation(double latitude, double longitude, String street, String city, String state, String zip, String country) {
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.street = street;
        this.zip = zip;
    }

    public String toString() {
        return String.format("Lat/Long:%.2f,%.2f, Street:%s, City:%s, State:%s, Zip:%s, Country:%s",
                latitude, longitude, street, city, state, zip, country);
    }

    @Override
    public String getShortDisplayAddress() {
        if (street != null && street.length() > 0 && city != null && city.length() > 0) return String.format("%s, %s", street, city);
        if (street != null && street.length() > 0) return street;
        if (city != null && city.length() > 0) return city;
        return null;
    }

    @Override
    public String getLongDisplayAddress() {
        final String startAddress = getShortDisplayAddress();
        final String endAddress;
        if (state != null && state.length() > 0 && zip != null && zip.length() > 0) endAddress = state + " " + zip;
        else if (state != null && state.length() > 0) endAddress = state;
        else if (zip != null && zip.length() > 0) endAddress = zip;
        else endAddress = null;

        if (startAddress != null && endAddress != null) return startAddress + "\n" + endAddress;
        if (startAddress != null) return startAddress;
        return endAddress;
    }

    @Override
    public String getCrossStreets() {
        return null;
    }

    @Override
    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }
}
