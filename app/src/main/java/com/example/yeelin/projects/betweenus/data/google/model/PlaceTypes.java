package com.example.yeelin.projects.betweenus.data.google.model;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ninjakiki on 4/7/16.
 */
public abstract class PlaceTypes {
    static class RawTypes {
        static final String bakery = "bakery";
        static final String bar = "bar";
        static final String cafe = "cafe";
        static final String casino = "casino";
        static final String convenience_store = "convenience_store";
        static final String establishment = "establishment";
        static final String food = "food";
        static final String night_club = "night_club";
        static final String point_of_interest = "point_of_interest";
        static final String restaurant = "restaurant";
    }

    static class FormattedTypes {
        static final String bakery = "Bakery";
        static final String bar = "Bar";
        static final String cafe = "Cafe";
        static final String casino = "Casino";
        static final String convenience_store = "Convenience Store";
        static final String establishment = "Establishment";
        static final String food = "Food";
        static final String night_club = "Night Club";
        static final String point_of_interest = "Point of Interest";
        static final String restaurant = "Restaurant";
    }

    //initialize static hashmap
    private static final Map<String, String> typeMap = new HashMap<String, String>() {
        {
            put(RawTypes.bakery, FormattedTypes.bakery);
            put(RawTypes.bar, FormattedTypes.bar);
            put(RawTypes.cafe, FormattedTypes.cafe);
            put(RawTypes.casino, FormattedTypes.casino);
            put(RawTypes.convenience_store, FormattedTypes.convenience_store);
            put(RawTypes.establishment, FormattedTypes.establishment);
            put(RawTypes.food, FormattedTypes.food);
            put(RawTypes.night_club, FormattedTypes.night_club);
            put(RawTypes.point_of_interest, FormattedTypes.point_of_interest);
            put(RawTypes.restaurant, FormattedTypes.restaurant);
        }
    };

    public static String getFormattedType(String rawType) {
        return typeMap.get(rawType);
    }
}
