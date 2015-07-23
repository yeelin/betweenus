package com.example.yeelin.projects.betweenus.json;

/**
 * Created by ninjakiki on 7/23/15.
 */
public class YelpSearchData {
    //Total number of business results
    private int total;

    //The list of business entries
    private YelpBusiness[] businesses;

    public int getTotal() {
        return total;
    }

    public YelpBusiness[] getBusinesses() {
        return businesses;
    }
}
