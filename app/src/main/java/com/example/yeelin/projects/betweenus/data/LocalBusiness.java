package com.example.yeelin.projects.betweenus.data;

/**
 * Created by ninjakiki on 10/28/15.
 */
public interface LocalBusiness {
    String getId();

    String getName();

    String getImageUrl();

    String getMobileUrl();

    String getPhoneNumber();

    int getReviewCount();

    String getCategory();

    double getRating();

    String getRatingImageUrl();

    LocalBusinessLocation getLocalBusinessLocation();
}
