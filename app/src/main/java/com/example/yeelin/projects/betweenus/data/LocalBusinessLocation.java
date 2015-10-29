package com.example.yeelin.projects.betweenus.data;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ninjakiki on 10/28/15.
 */
public interface LocalBusinessLocation {
    String getShortDisplayAddress();

    String getLongDisplayAddress();

    LatLng getLatLng();
}
