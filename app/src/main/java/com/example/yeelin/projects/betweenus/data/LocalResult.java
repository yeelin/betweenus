package com.example.yeelin.projects.betweenus.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 10/28/15.
 */
public interface LocalResult {
    ArrayList<LocalBusiness> getLocalBusinesses();

    LatLng getResultCenter();

    double getResultLatitudeDelta();

    double getResultLongitudeDelta();

    int getDataSource();
}
