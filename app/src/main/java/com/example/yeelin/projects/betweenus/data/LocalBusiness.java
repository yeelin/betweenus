package com.example.yeelin.projects.betweenus.data;

import java.util.HashMap;

/**
 * Created by ninjakiki on 10/28/15.
 */
public interface LocalBusiness {
    String getId();
    String getAbout();
    String getAttire();
    String getCategory();
    //Missing:category_list
    //Missing:cover
    String getCulinaryTeam();

    String getDescription();
    String[] getFoodStyles();
    String getGeneralInfo();
    HashMap<String,String> getHours();
    boolean isAlwaysOpen();
    String getFbLink();

    LocalBusinessLocation getLocalBusinessLocation();
    String getName();
    //Missing:Parking
    //Missing:Payment options
    String getPhoneNumber();
    String getImageUrl();

    String getPriceRange();
    String getPublicTransit();
    //Missing:restaurant_services
    //Missing:restaurant_specialties

    String getMobileUrl();
    double getRating();
    String getRatingImageUrl();
    int getReviewCount();
    int getCheckins();
    int getLikes();
}
