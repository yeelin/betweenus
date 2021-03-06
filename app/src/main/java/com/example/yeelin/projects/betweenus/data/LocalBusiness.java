package com.example.yeelin.projects.betweenus.data;

/**
 * Created by ninjakiki on 10/28/15.
 */
public interface LocalBusiness {
    String getId();
    String getAbout();
    String getAttire();
    String getCategory();
    String[] getCategoryList();
    //Missing:cover
    String getCulinaryTeam();

    String getDescription();
    String[] getCuisine();
    String getGeneralInfo();
    String[] getHours();
    boolean isAlwaysOpen();
    String getFbLink();

    LocalBusinessLocation getLocalBusinessLocation();
    String getShortDisplayAddress();
    String getLongDisplayAddress();
    String getName();
    String[] getParking();
    String[] getPaymentOptions();
    String getPhoneNumber();

    String getProfilePictureUrl();
    String getProfilePictureUrl(int height, int width);
    LocalPhoto[] getPhotos();

    String getPriceRangeString();
    int getPriceRange();
    String getPublicTransit();
    String[] getRestaurantServices();
    String[] getRestaurantSpecialities();

    String getMobileUrl();
    double getRating();
    String getRatingImageUrl();
    int getReviewCount();
    int getCheckins();
    int getLikes();
    double getNormalizedLikes();
    int getDataSource();
}
