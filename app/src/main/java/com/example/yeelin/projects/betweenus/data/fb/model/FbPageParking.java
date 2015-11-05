package com.example.yeelin.projects.betweenus.data.fb.model;

import android.support.v4.util.SimpleArrayMap;

/**
 * Created by ninjakiki on 11/2/15.
 */
public abstract class FbPageParking {
    //json keys
    static class Json {
        static final String LOT = "lot";
        static final String STREET = "street";
        static final String VALET = "valet";
    }

    //user-friendly values
    static class Values {
        static final String LOT = "Parking lot";
        static final String STREET = "Street";
        static final String VALET = "Valet";
    }

    //map
    public static final SimpleArrayMap<String,String> MAP = initParking();

    //map initializer
    public static SimpleArrayMap<String,String> initParking() {
        SimpleArrayMap<String,String> map = new SimpleArrayMap<>();
        map.put(Json.LOT, Values.LOT);
        map.put(Json.STREET, Values.STREET);
        map.put(Json.VALET, Values.VALET);
        return map;
    }
}
