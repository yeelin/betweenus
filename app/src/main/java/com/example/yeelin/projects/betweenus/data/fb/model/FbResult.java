package com.example.yeelin.projects.betweenus.data.fb.model;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 10/27/15.
 */
public class FbResult implements LocalResult {
    private final ArrayList<FbPage> data;

    public FbResult(ArrayList<FbPage> pages) {
        data = pages;
    }

    public ArrayList<FbPage> getPages() {
        return data;
    }

    public String toString() {
        return String.format("Size:%d, Data:%s", data.size(), data);
    }

    @Override
    public ArrayList<LocalBusiness> getLocalBusinesses() {
        ArrayList<LocalBusiness> localBusinesses = new ArrayList<>(data.size());
        for (int i=0; i<data.size(); i++) {
            localBusinesses.add(data.get(i));
        }
        return localBusinesses;
    }

    @Override
    public LatLng getResultCenter() {
        return null;
    }

    @Override
    public double getResultLatitudeDelta() {
        return 0;
    }

    @Override
    public double getResultLongitudeDelta() {
        return 0;
    }

    @Override
    public int getDataSource() {
        return LocalConstants.FACEBOOK;
    }
}
