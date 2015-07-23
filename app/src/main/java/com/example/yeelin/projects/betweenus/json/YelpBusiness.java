package com.example.yeelin.projects.betweenus.json;

/**
 * Created by ninjakiki on 7/22/15.
 */
public class YelpBusiness {
    //Yelp ID for this business
    private String id;

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

    //Rating for this business (value ranges from 1, 1.5, ... 4.5, 5)
    private double rating;

    //URL to small version of rating image for this business (size = 50x10)
    private String rating_img_url_small;

    public String toString() {
        return String.format("Id:%s, Name:%s, ImageURL:%s, MobileURL:%s, Phone:%s, DisplayPhone:%s, ReviewCount:%d, Rating:%f, RatingImgUrlSmall:%s",
                id, name, image_url, mobile_url, phone, display_phone, review_count, rating, rating_img_url_small);
    }
}
