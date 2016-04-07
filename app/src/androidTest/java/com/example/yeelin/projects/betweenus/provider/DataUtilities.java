package com.example.yeelin.projects.betweenus.provider;

import android.content.ContentValues;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 4/6/16.
 */
public class DataUtilities {
    static class Itinerary {
        static ContentValues insertValues() {
            ContentValues values = new ContentValues();

            //itinerary id
            values.put(ItineraryContract.Columns.ITINERARY_ID, 123);
            //closest city, lat, long
            values.put(ItineraryContract.Columns.CLOSEST_CITY, "Seattle");
            values.put(ItineraryContract.Columns.CLOSEST_CITY_LATITUDE, 47.61);
            values.put(ItineraryContract.Columns.CLOSEST_CITY_LONGITUDE, -122.33);
            //name, email, phone
            values.put(ItineraryContract.Columns.NAME, "John");
            values.put(ItineraryContract.Columns.EMAIL, "john@test.com");
            values.put(ItineraryContract.Columns.PHONE, "206-111-2222");
            //datasource
            values.put(ItineraryContract.Columns.DATA_SOURCE, 2);
            //created dt
            values.put(ItineraryContract.Columns.CREATED_DATETIME, 1428625213);
            return values;
        }
    }

    static class Stop {
        static ArrayList<ContentValues> bulkInsertValues() {
            ArrayList<ContentValues> valuesArrayList = new ArrayList<>();

            ContentValues values1 = new ContentValues();
            //itinerary id
            values1.put(ItineraryContract.Columns.ITINERARY_ID, 123);
            //place id
            values1.put(StopContract.Columns.PLACE_ID, "Stop_1");
            //datasource
            values1.put(StopContract.Columns.DATA_SOURCE, 2);

            ContentValues values2 = new ContentValues();
            //itinerary id
            values2.put(ItineraryContract.Columns.ITINERARY_ID, 123);
            //place id
            values2.put(StopContract.Columns.PLACE_ID, "Stop_2");
            //datasource
            values2.put(StopContract.Columns.DATA_SOURCE, 2);

            valuesArrayList.add(values1);
            valuesArrayList.add(values2);

            return valuesArrayList;
        }
    }
}
