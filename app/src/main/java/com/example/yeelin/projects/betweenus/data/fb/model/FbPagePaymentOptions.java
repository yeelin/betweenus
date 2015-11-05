package com.example.yeelin.projects.betweenus.data.fb.model;

import android.support.v4.util.SimpleArrayMap;

/**
 * Created by ninjakiki on 11/2/15.
 */
public abstract class FbPagePaymentOptions {
    //json keys
    static class Json {
        static final String AMEX = "amex";
        static final String CASH_ONLY = "cash_only";
        static final String DISCOVER = "discover";
        static final String MASTERCARD = "mastercard";
        static final String VISA = "visa";
    }

    //user-friendly values
    static class Values {
        static final String AMEX = "American Express";
        static final String CASH_ONLY = "Cash only";
        static final String DISCOVER = "Discover";
        static final String MASTERCARD = "Mastercard";
        static final String VISA = "Visa";
    }

    //map
    public static final SimpleArrayMap<String,String> MAP = initPaymentOptions();

    //map initializer
    public static SimpleArrayMap<String,String> initPaymentOptions() {
        SimpleArrayMap<String,String> map = new SimpleArrayMap<>();
        map.put(Json.AMEX, Values.AMEX);
        map.put(Json.CASH_ONLY, Values.CASH_ONLY);
        map.put(Json.DISCOVER, Values.DISCOVER);
        map.put(Json.MASTERCARD, Values.MASTERCARD);
        map.put(Json.VISA, Values.VISA);
        return map;
    }
}
