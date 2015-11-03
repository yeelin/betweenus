package com.example.yeelin.projects.betweenus.data.fb.model;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalBusinessLocation;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by ninjakiki on 10/27/15.
 */
public class FbPage implements LocalBusiness {
    private final String id;
    private final String about;
    private final String attire;
    private final String category;
    private final FbPageCategory[] category_list;
    private final FbCoverPhoto cover;
    private final String culinary_team;

    private final String description;
    private final String[] food_styles;
    private final String general_info;
    private final HashMap<String, String> hours;
    private final boolean is_always_open;
    private final String link;

    private final FbLocation location;
    private final String name;
    private final FbPageParking parking;
    private final FbPagePaymentOptions payment_options;
    private final String phone;
    private final FbPagePicture picture;

    private final String price_range;
    private final String public_transit;
    private final FbPageRestaurantServices restaurant_services;
    private final FbPageRestaurantSpecialties restaurant_specialties;

    private final String website;
    private final int checkins;
    private final int likes;

    public FbPage(String id, String about, String attire, String category, FbPageCategory[] category_list, FbCoverPhoto cover, String culinary_team,
                  String description, String[] food_styles, String general_info, HashMap<String, String> hours, boolean is_always_open, String link,
                  FbLocation location, String name, FbPageParking parking, FbPagePaymentOptions payment_options, String phone, FbPagePicture picture,
                  String price_range, String public_transit, FbPageRestaurantServices restaurant_services, FbPageRestaurantSpecialties restaurant_specialties,
                  String website, int checkins, int likes) {
        this.id = id;
        this.about = about;
        this.attire = attire;
        this.category = category;
        this.category_list = category_list;
        this.cover = cover;
        this.culinary_team = culinary_team;

        this.description = description;
        this.food_styles = food_styles;
        this.general_info = general_info;
        this.hours = hours;
        this.is_always_open = is_always_open;
        this.link = link;

        this.location = location;
        this.name = name;
        this.parking = parking;
        this.payment_options = payment_options;
        this.phone = phone;
        this.picture = picture;

        this.price_range = price_range;
        this.public_transit = public_transit;
        this.restaurant_services = restaurant_services;
        this.restaurant_specialties = restaurant_specialties;

        this.website = website;
        this.checkins = checkins;
        this.likes = likes;
    }

    @Override
    public String toString() {
        return String.format("{FbPage: [Id:%s, About:%s, Attire:%s, Category:%s, CategoryList:%s, Cover:%s, CulinaryTeam:%s, " +
                        "Description:%s, FoodStyles:%s, GeneralInfo:%s, Hours:%s, IsAlwaysOpen:%s, Link:%s, " +
                        "Location:%s, Name:%s, Parking:%s, PaymentOptions:%s, Phone:%s, Picture:%s, " +
                        "PriceRange:%s, Transit:%s, Services:%s, Specialties:%s, " +
                        "Website:%s, Checkins:%d, Likes:%d]}" ,
                id, about, attire, category, Arrays.toString(category_list), cover, culinary_team,
                description, Arrays.toString(food_styles), general_info, hours != null ? String.format("[Keys:%s, Values:%s]", hours.keySet(), hours.values()) : null, is_always_open, link,
                location, name, parking, payment_options, phone, picture,
                price_range, public_transit, restaurant_services, restaurant_specialties,
                website, checkins, likes);
    }

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
        return about;
    }

    @Override
    public String getAttire() {
        return attire;
    }

    @Override
    public String getImageUrl() {
        return picture.getData().getUrl();
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
        return website;
    }

    @Override
    public String getPhoneNumber() {
        return phone;
    }

    @Override
    public int getReviewCount() {
        return checkins;
    }

    @Override
    public int getCheckins() {
        return checkins;
    }

    @Override
    public int getLikes() {
        return likes;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getCulinaryTeam() {
        return culinary_team;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String[] getFoodStyles() {
        return food_styles;
    }

    @Override
    public String getGeneralInfo() {
        return general_info;
    }

    @Override
    public HashMap<String, String> getHours() {
        return hours;
    }

    @Override
    public boolean isAlwaysOpen() {
        return is_always_open;
    }

    @Override
    public String getFbLink() {
        return link;
    }

    @Override
    public double getRating() {
        return likes;
    }

    @Override
    public String getRatingImageUrl() {
        return null;
    }

    @Override
    public LocalBusinessLocation getLocalBusinessLocation() {
        return location;
    }
}
