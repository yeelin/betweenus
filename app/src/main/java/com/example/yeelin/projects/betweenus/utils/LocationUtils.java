package com.example.yeelin.projects.betweenus.utils;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

/**
 * Created by ninjakiki on 7/16/15.
 */
public class LocationUtils {

    public static final int USER_LOCATION = 0;
    public static final int FRIEND_LOCATION = 1;

    //bundle extras in returned location
    public static final String EXTRA_NAME = LocationUtils.class.getSimpleName() + ".locationName";
    public static final String EXTRA_ADDRESS = LocationUtils.class.getSimpleName() + ".locationAddress";

    /**
     * Returns the midpoint between two lat/longs.
     * @param userLocation
     * @param friendLocation
     * @return
     */
    public static Location computeMidPoint(@NonNull Location userLocation, @NonNull Location friendLocation) {
        LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        LatLng friendLatLng = new LatLng(friendLocation.getLatitude(), friendLocation.getLongitude());
        LatLng middleLatLng = SphericalUtil.interpolate(userLatLng, friendLatLng, 0.5);

        Location location = new Location("Midpoint");
        location.setLatitude(middleLatLng.latitude);
        location.setLongitude(middleLatLng.longitude);
        return location;
    }
}

