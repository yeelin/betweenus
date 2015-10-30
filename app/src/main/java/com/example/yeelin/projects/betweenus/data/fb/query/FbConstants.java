package com.example.yeelin.projects.betweenus.data.fb.query;

import android.text.TextUtils;

/**
 * Created by ninjakiki on 10/27/15.
 */
public class FbConstants {
    //TODO: Remove hack
    public static final boolean USE_FB = false;

    //endpoint
    public static final String SEARCH_ENDPOINT = "/search";

    /**
     * Parameter names
     */
    //query
    public static final String QUERY = "q";
    //type
    public static final String TYPE = "type";
    //center
    public static final String CENTER = "center";
    //distance
    public static final String DISTANCE = "distance";
    //fields
    public static final String FIELDS = "fields";

    /**
     * Parameter values
     */
    //query
    private static final String RESTAURANT = "restaurant";
    public static final String REQUEST_QUERY = RESTAURANT;
    //type
    private static final String PLACE = "place";
    public static final String REQUEST_TYPE = PLACE;
    //distance
    private static final String THREE_MILE_RADIUS = "4828"; //3 miles == 4848 meters
    public static final String REQUEST_DISTANCE = THREE_MILE_RADIUS;
    //fields
    private static final String ID = "id";
    private static final String ABOUT = "about";
    private static final String ATTIRE = "attire";
    private static final String CATEGORY = "category";
    private static final String CATEGORY_LIST = "category_list";
    private static final String COVER = "cover";
    private static final String CULINARY_TEAM = "culinary_team";
    private static final String DESCRIPTION = "description";
    private static final String FOOD_STYLES = "food_styles";
    private static final String GENERAL_INFO = "general_info";
    private static final String HOURS = "hours";
    private static final String IS_ALWAYS_OPEN = "is_always_open";
    private static final String LINK = "link";
    private static final String LOCATION = "location";
    private static final String NAME = "name";
    private static final String PARKING = "parking";
    private static final String PAYMENT_OPTIONS = "payment_options";
    private static final String PHONE = "phone";
    private static final String PICTURE = "picture";
    private static final String PRICE_RANGE = "price_range";
    private static final String PUBLIC_TRANSIT = "public_transit";
    private static final String RESTAURANT_SERVICES = "restaurant_services";
    private static final String RESTAURANT_SPECIALTIES = "restaurant_specialties";
    private static final String WEBSITE = "website";
    private static final String CHECKINS = "checkins";
    private static final String LIKES = "likes";
    private static final String PHOTOS = "photos";
    public static final String REQUEST_FIELDS_SIMPLE = TextUtils.join(",",
            new String[]{ID, CATEGORY, LOCATION, NAME, PHONE, PICTURE, WEBSITE, CHECKINS, LIKES});
    public static final String REQUEST_FIELDS_DETAIL = TextUtils.join(",",
            new String[]{ID, ABOUT, ATTIRE, CATEGORY, CATEGORY_LIST, COVER, CULINARY_TEAM, DESCRIPTION, FOOD_STYLES, GENERAL_INFO,
                        HOURS, IS_ALWAYS_OPEN, LINK, LOCATION, NAME, PARKING, PAYMENT_OPTIONS, PHONE, PICTURE, PRICE_RANGE,
                        PUBLIC_TRANSIT, RESTAURANT_SERVICES, RESTAURANT_SPECIALTIES, WEBSITE, CHECKINS, LIKES, PHOTOS});


}
