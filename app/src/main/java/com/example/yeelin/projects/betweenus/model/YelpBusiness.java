package com.example.yeelin.projects.betweenus.model;

import java.util.Arrays;

/**
 * Created by ninjakiki on 7/22/15.
 */
public class YelpBusiness {
    //Yelp ID for this business
    private String id;

    //Whether business has been (permanently) closed
    private boolean is_closed;

    //Name of this business
    private String name;

    //URL of photo for this business
    private String image_url;

    //URL for mobile business page on Yelp
    private String mobile_url;

    //Phone number for this business with international dialing code (e.g. +442079460000)
    private String phone;

    //Phone number for this business formatted for display
    private String display_phone;

    //Number of reviews for this business
    private int review_count;

    //Provides a list of category name, alias pairs that this business is associated with.
    // For example, [["Local Flavor", "localflavor"], ["Active Life", "active"], ["Mass Media", "massmedia"]]
    //The alias is provided so you can search with the category_filter.
    private String[][] categories;

    //Rating for this business (value ranges from 1, 1.5, ... 4.5, 5)
    private double rating;

    //URL to small version of rating image for this business (size = 50x10)
    private String rating_img_url_small;

    //Location data for this business
    private YelpBusinessLocation location;

    public String getId() {
        return id;
    }

    public boolean is_closed() {
        return is_closed;
    }

    public String getName() {
        return name;
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

    public double getRating() {
        return rating;
    }

    public String getRating_img_url_small() {
        return rating_img_url_small;
    }

    public YelpBusinessLocation getLocation() {
        return location;
    }

    public YelpBusiness() {}

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
