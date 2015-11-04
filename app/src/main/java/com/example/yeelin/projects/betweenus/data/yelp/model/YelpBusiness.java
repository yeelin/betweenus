package com.example.yeelin.projects.betweenus.data.yelp.model;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalBusinessLocation;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by ninjakiki on 7/22/15.
 */
public class YelpBusiness implements LocalBusiness {
    //Yelp ID for this business
    private final String id;

    //Whether business has been (permanently) closed
    private final boolean is_closed;

    //Name of this business
    private final String name;

    //URL of photo for this business
    private final String image_url;

    //URL for mobile business page on Yelp
    private final String mobile_url;

    //Phone number for this business with international dialing code (e.g. +442079460000)
    private final String phone;

    //Phone number for this business formatted for display
    private final String display_phone;

    //Number of reviews for this business
    private final int review_count;

    //Provides a list of category name, alias pairs that this business is associated with.
    // For example, [["Local Flavor", "localflavor"], ["Active Life", "active"], ["Mass Media", "massmedia"]]
    //The alias is provided so you can search with the category_filter.
    private final String[][] categories;

    //Distance that business is from search location in meters, if a latitude/longitude is specified.
    private final double distance;

    //Rating for this business (value ranges from 1, 1.5, ... 4.5, 5)
    private final double rating;

    //URL to star rating image for this business (size = 84x17)
    private final String rating_img_url;

    //URL to small version of rating image for this business (size = 50x10)
    private final String rating_img_url_small;

    //URL to large version of rating image for this business (size = 166x30)
    private final String rating_img_url_large;

    //Location data for this business
    private final YelpBusinessLocation location;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAbout() {
        return null;
    }

    @Override
    public String getAttire() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return image_url;
    }

    @Override
    public String getPriceRange() {
        return null;
    }

    @Override
    public String getPublicTransit() {
        return null;
    }

    @Override
    public String getMobileUrl() {
        return mobile_url;
    }

    @Override
    public String getPhoneNumber() {
        return phone;
    }

    @Override
    public int getReviewCount() {
        return review_count;
    }

    @Override
    public int getCheckins() {
        return 0;
    }

    @Override
    public int getLikes() {
        return 0;
    }

    @Override
    public String getCategory() {
        return getDisplayCategories();
    }

    @Override
    public String[] getCategoryList() {
        if (categories == null || categories.length == 0) return null;

        String[] categoryArray = new String[categories.length];
        for (int i=0; i<categoryArray.length; i++) {
            categoryArray[i] = categories[i][0];
        }
        return categoryArray;
    }

    @Override
    public String getCulinaryTeam() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String[] getFoodStyles() {
        return null;
    }

    @Override
    public String getGeneralInfo() {
        return null;
    }

    @Override
    public String[] getHours() {
        return null;
    }

    @Override
    public boolean isAlwaysOpen() {
        return false;
    }

    @Override
    public String getFbLink() {
        return null;
    }

    @Override
    public double getRating() {
        return rating;
    }

    @Override
    public String getRatingImageUrl() {
        return rating_img_url_large;
    }

    @Override
    public LocalBusinessLocation getLocalBusinessLocation() {
        return location;
    }

    public boolean is_closed() {
        return is_closed;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getMobile_url() {
        return mobile_url;
    }

    public String getPhone() {
        return phone;
    }

    public String getDisplay_phone() {
        return display_phone;
    }

    public int getReview_count() {
        return review_count;
    }

    public String[][] getCategories() {
        return categories;
    }

    public String getDisplayCategories() {
        if (categories == null || categories.length == 0) return null;

        final StringBuilder categoriesBuilder = new StringBuilder();
        for (int i=0; i<categories.length; i++) {
            categoriesBuilder.append(categories[i][0]);
            if (i < categories.length-1) {
                categoriesBuilder.append(", ");
            }
        }
        return categoriesBuilder.toString();
    }

    public double getDistance() {
        return distance;
    }

    public String getRating_img_url() {
        return rating_img_url;
    }

    public String getRating_img_url_small() {
        return rating_img_url_small;
    }

    public String getRating_img_url_large() {
        return rating_img_url_large;
    }

    public YelpBusinessLocation getLocation() {
        return location;
    }

    public YelpBusiness(String id, boolean is_closed, String name, String image_url, String mobile_url, String phone, String display_phone, int review_count, String[][] categories, double distance, double rating, String rating_img_url, String rating_img_url_small, String rating_img_url_large, YelpBusinessLocation location) {
        this.id = id;
        this.is_closed = is_closed;
        this.name = name;
        this.image_url = image_url;
        this.mobile_url = mobile_url;
        this.phone = phone;
        this.display_phone = display_phone;
        this.review_count = review_count;
        this.categories = categories;
        this.distance = distance;
        this.rating = rating;
        this.rating_img_url = rating_img_url;
        this.rating_img_url_small = rating_img_url_small;
        this.rating_img_url_large = rating_img_url_large;
        this.location = location;
    }

    /**
     * For debugging
     * @return
     */
    @Override
    public String toString() {
        return String.format("Id:%s, IsClosed:%s, Name:%s, ImageURL:%s, MobileURL:%s, Phone:%s, DisplayPhone:%s, ReviewCount:%d, Categories:%s, Rating:%f, RatingImgUrlSmall:%s, Location:%s",
                id, is_closed, name, image_url, mobile_url, phone, display_phone, review_count, Arrays.deepToString(categories), rating, rating_img_url_small, location);
    }
}
