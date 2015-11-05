package com.example.yeelin.projects.betweenus.data.fb.model;

import android.support.v4.util.SimpleArrayMap;

/**
 * Created by ninjakiki on 11/2/15.
 */
public abstract class FbPageRestaurantServices {
    //json keys
    static class Json {
        static final String CATERING = "catering";
        static final String DELIVERY = "delivery";
        static final String GROUPS = "groups";
        static final String KIDS = "kids";
        static final String OUTDOOR = "outdoor";
        static final String RESERVE = "reserve";
        static final String TAKEOUT = "takeout";
        static final String WAITER = "waiter";
        static final String WALKINS = "walkins";
    }

    //user-friendly values
    static class Values {
        static final String CATERING = "Catering service";
        static final String DELIVERY = "Delivery service";
        static final String GROUPS = "Good for groups";
        static final String KIDS = "Kid friendly";
        static final String OUTDOOR = "Outdoor seating available";
        static final String RESERVE = "Takes reservations";
        static final String TAKEOUT = "Takeout service";
        static final String WAITER = "Waiter service";
        static final String WALKINS = "Walk-ins welcome";
    }

    public static final SimpleArrayMap<String,String> MAP = initRestaurantSpecialties();

    public static SimpleArrayMap<String,String> initRestaurantSpecialties() {
        SimpleArrayMap<String,String> map = new SimpleArrayMap<>();
        map.put(Json.CATERING, Values.CATERING);
        map.put(Json.DELIVERY, Values.DELIVERY);
        map.put(Json.GROUPS, Values.GROUPS);
        map.put(Json.KIDS, Values.KIDS);
        map.put(Json.OUTDOOR, Values.OUTDOOR);
        map.put(Json.RESERVE, Values.RESERVE);
        map.put(Json.TAKEOUT, Values.TAKEOUT);
        map.put(Json.WAITER, Values.WAITER);
        map.put(Json.WALKINS, Values.WALKINS);
        return map;
    }
}
