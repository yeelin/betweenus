package com.example.yeelin.projects.betweenus.utils;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

/**
 * Created by ninjakiki on 7/16/15.
 */
public class LocationUtils {
    //constants used by various activities and fragments to differentiate between user and friend locations
    public static final int USER_LOCATION = 0;
    public static final int FRIEND_LOCATION = 1;

    /**
     * Returns the midpoint between two latlngs.
     * @param userLatLng
     * @param friendLatLng
     * @return
     */
    public static LatLng computeMidPoint(@NonNull LatLng userLatLng, @NonNull LatLng friendLatLng) {
        return SphericalUtil.interpolate(userLatLng, friendLatLng, 0.5);
    }
}

