package com.example.yeelin.projects.betweenus.data.fb.model;

import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalBusinessLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by ninjakiki on 10/27/15.
 */
public class FbPage implements LocalBusiness {
    private static final String TAG = FbPage.class.getCanonicalName();
    private static final String[] dayOfWeek = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
    private static final String[] DAY_OF_WEEK = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

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
                description, Arrays.toString(food_styles), general_info, getHours(), is_always_open, link,
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
        return price_range;
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
        return -1;
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
    public String[] getCategoryList() {
        if (category_list == null || category_list.length == 0) return null;
        String[] categoryArray = new String[category_list.length];
        for (int i=0; i<categoryArray.length; i++) {
            categoryArray[i] = category_list[i].getName();
        }
        return categoryArray;
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
    public String[] getCuisine() {
        return food_styles;
    }

    @Override
    public String getGeneralInfo() {
        return general_info;
    }

    @Override
    public String[] getHours() {
        if (hours != null) return buildHoursArray();
        return null;
    }

    private String[] buildHoursArray() {
        String[] hoursArray = new String[dayOfWeek.length];
        String key;
        String openValue;
        String closeValue;
        String status;
        boolean isOpenToday;

        int n=0;
        //iterate over the days of the week
        for (int i=0; i<dayOfWeek.length; i++) {
            //always reset this to false so that we know if the place is not open that day
            isOpenToday = false;

            //each day can have up to 2 openings
            for (int j=1; j<=2; j++) {

                //open or close
                status = "open";
                key = String.format("%s_%d_%s", dayOfWeek[i], j, status);
                openValue = hours.get(key);

                if (openValue != null) {
                    status = "close";
                    key = String.format("%s_%d_%s", dayOfWeek[i], j, status);
                    closeValue = hours.get(key);

                    if (closeValue != null) {
                        if (j == 1)
                            hoursArray[n] = String.format("%s\t:\t%s - %s", DAY_OF_WEEK[i], openValue, closeValue);
                        if (j == 2)
                            hoursArray[n] = String.format("%s, %s - %s", hoursArray[n], openValue, closeValue);
                        isOpenToday = true;
                    }
                }
            }

            if (!isOpenToday) {
                hoursArray[n] = String.format("%s\t:\t%s", DAY_OF_WEEK[i], "Closed");
            }
            ++n;
        }

        Log.d(TAG, "buildHoursArray:" + Arrays.toString(hoursArray));
        return hoursArray;
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
        return -1;
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
