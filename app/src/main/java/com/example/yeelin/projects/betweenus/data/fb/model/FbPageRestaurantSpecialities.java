package com.example.yeelin.projects.betweenus.data.fb.model;

import android.support.v4.util.SimpleArrayMap;

/**
 * Created by ninjakiki on 11/2/15.
 */
public abstract class FbPageRestaurantSpecialities {
    //json keys
    static class Json {
        static final String BREAKFAST = "breakfast";
        static final String COFFEE = "coffee";
        static final String DINNER = "dinner";
        static final String DRINKS = "drinks";
        static final String LUNCH = "lunch";
    }

    //user-friendly values
    static class Values {
        static final String BREAKFAST = "Breakfast";
        static final String COFFEE = "Coffee";
        static final String DINNER = "Dinner";
        static final String DRINKS = "Drinks";
        static final String LUNCH = "Lunch";
    }

    //map
    public static final SimpleArrayMap<String,String> MAP = initSpecialities();

    //map initializer
    public static SimpleArrayMap<String,String> initSpecialities() {
        SimpleArrayMap<String,String> map = new SimpleArrayMap<>();
        map.put(Json.BREAKFAST, Values.BREAKFAST);
        map.put(Json.COFFEE, Values.COFFEE);
        map.put(Json.DINNER, Values.DINNER);
        map.put(Json.DRINKS, Values.DRINKS);
        map.put(Json.LUNCH, Values.LUNCH);
        return map;
    }
}
