package com.example.yeelin.projects.betweenus.data.yelp.model;

import android.support.annotation.Nullable;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/23/15.
 */
public class YelpResult implements LocalResult {
    //Suggested bounds in a map to display results in
    private final YelpResultRegion region;

    //Total number of business results
    private final int total;

    //The list of business entries
    //private YelpBusiness[] businesses;
    private final ArrayList<YelpBusiness> businesses;

    public YelpResult(YelpResultRegion region, int total, ArrayList<YelpBusiness> businesses) {
        this.region = region;
        this.total = total;
        this.businesses = businesses;
    }

    public YelpResultRegion getRegion() {
        return region;
    }

    public int getTotal() {
        return total;
    }

//    public YelpBusiness[] getBusinesses() {
//        return businesses;
//    }
//
//    public ArrayList<YelpBusiness> getBusinessesAsArrayList() {
//        return new ArrayList<YelpBusiness>(Arrays.asList(businesses));
//    }

    public ArrayList<YelpBusiness> getBusinesses() {
        return businesses;
    }

    @Override
    public String toString() {
        return String.format("Region:%s, Total:%d, Businesses:%s", region, total, businesses);
    }

    @Override
    public ArrayList<LocalBusiness> getLocalBusinesses() {
        ArrayList<LocalBusiness> localBusinesses = new ArrayList<>(businesses.size());
        for (int i=0; i<businesses.size(); i++) {
            localBusinesses.add(businesses.get(i));
        }
        return localBusinesses;
    }

    @Nullable
    @Override
    public String getAfterId() {
        return null;
    }

    @Nullable
    @Override
    public String getNextUrl() {
        return null;
    }

    @Override
    public LatLng getResultCenter() {
        YelpResultRegion.Center center = region.getCenter();
        return center != null ? new LatLng(center.getLatitude(), center.getLongitude()) : null;
    }

    @Override
    public double getResultLatitudeDelta() {
        YelpResultRegion.Span span = region.getSpan();
        return span != null ? span.getLatitude_delta() : 0.0;
    }

    @Override
    public double getResultLongitudeDelta() {
        YelpResultRegion.Span span = region.getSpan();
        return span != null ? span.getLongitude_delta() : 0.0;
    }

    @Override
    public int getDataSource() {
        return LocalConstants.YELP;
    }
}
