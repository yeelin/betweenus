package com.example.yeelin.projects.betweenus.model;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/23/15.
 */
public class YelpResult {
    //Suggested bounds in a map to display results in
    private YelpResultRegion region;

    //Total number of business results
    private int total;

    //The list of business entries
    //private YelpBusiness[] businesses;
    private ArrayList<YelpBusiness> businesses;

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
//        return String.format("Region:%s, Total:%d, Businesses:%s", region, total, Arrays.toString(businesses));
        return String.format("Region:%s, Total:%d, Businesses:%s", region, total, businesses);

    }
}
