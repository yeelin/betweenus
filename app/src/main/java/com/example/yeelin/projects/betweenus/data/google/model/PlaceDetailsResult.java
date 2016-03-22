package com.example.yeelin.projects.betweenus.data.google.model;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalBusinessLocation;
import com.example.yeelin.projects.betweenus.data.LocalPhoto;

import java.util.Arrays;

/**
 * Created by ninjakiki on 3/17/16.
 * https://developers.google.com/places/web-service/details#PlaceDetailsRequests
 * A Place Details request returns more comprehensive information about the indicated place
 * such as its complete address, phone number, user rating and reviews.
 */
public class PlaceDetailsResult implements LocalBusiness {
    //possible values: OK, UNKNOWN_ERROR, ZERO_RESULTS, OVER_QUERY_LIMIT, REQUEST_DENIED, INVALID_REQUEST, NOT_FOUND
    private final String status;
    //contains a set of attributions about this listing which must be displayed to the user.
    private final String[] html_attributions;
    //contains the detailed information about the place requested
    private final Place result;

    public PlaceDetailsResult(String status, String[] html_attributions, Place result) {
        this.status = status;
        this.html_attributions = html_attributions;
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public String[] getHtml_attributions() {
        return html_attributions;
    }

    public Place getResult() {
        return result;
    }

    @Override
    public String toString() {
        return String.format("Status:%s, Attributions:%s, Place:%s",
                status, Arrays.toString(html_attributions), result);
    }

    @Override
    public String getId() {
        return result.getPlace_id();
    }

    @Override
    public String getAbout() {
        return result.getAbout();
    }

    @Override
    public String getAttire() {
        return result.getAttire();
    }

    @Override
    public String getCategory() {
        return result.getCategory();
    }

    @Override
    public String[] getCategoryList() {
        return result.getCategoryList();
    }

    @Override
    public String getCulinaryTeam() {
        return result.getCulinaryTeam();
    }

    @Override
    public String getDescription() {
        return result.getDescription();
    }

    @Override
    public String[] getCuisine() {
        return result.getCuisine();
    }

    @Override
    public String getGeneralInfo() {
        return result.getGeneralInfo();
    }

    @Override
    public String[] getHours() {
        return result.getHours();
    }

    @Override
    public boolean isAlwaysOpen() {
        return result.isAlwaysOpen();
    }

    @Override
    public String getFbLink() {
        return result.getFbLink();
    }

    @Override
    public LocalBusinessLocation getLocalBusinessLocation() {
        return result.getLocalBusinessLocation();
    }

    @Override
    public String getShortDisplayAddress() {
        return result.getShortDisplayAddress();
    }

    @Override
    public String getLongDisplayAddress() {
        return result.getLongDisplayAddress();
    }

    @Override
    public String getName() {
        return result.getName();
    }

    @Override
    public String[] getParking() {
        return result.getParking();
    }

    @Override
    public String[] getPaymentOptions() {
        return result.getPaymentOptions();
    }

    @Override
    public String getPhoneNumber() {
        return result.getPhoneNumber();
    }

    @Override
    public String getProfilePictureUrl() {
        return result.getProfilePictureUrl();
    }

    @Override
    public LocalPhoto[] getPhotos() {
        return result.getPhotos();
    }

    @Override
    public String getPriceRangeString() {
        return result.getPriceRangeString();
    }

    @Override
    public int getPriceRange() {
        return result.getPriceRange();
    }

    @Override
    public String getPublicTransit() {
        return result.getPublicTransit();
    }

    @Override
    public String[] getRestaurantServices() {
        return result.getRestaurantServices();
    }

    @Override
    public String[] getRestaurantSpecialities() {
        return result.getRestaurantSpecialities();
    }

    @Override
    public String getMobileUrl() {
        return result.getMobileUrl();
    }

    @Override
    public double getRating() {
        return result.getRating();
    }

    @Override
    public String getRatingImageUrl() {
        return result.getRatingImageUrl();
    }

    @Override
    public int getReviewCount() {
        return result.getReviewCount();
    }

    @Override
    public int getCheckins() {
        return result.getCheckins();
    }

    @Override
    public int getLikes() {
        return result.getLikes();
    }

    @Override
    public double getNormalizedLikes() {
        return result.getNormalizedLikes();
    }

    @Override
    public int getDataSource() {
        return result.getDataSource();
    }
}
