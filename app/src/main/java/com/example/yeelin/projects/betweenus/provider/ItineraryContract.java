package com.example.yeelin.projects.betweenus.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.net.URI;

/**
 * Created by ninjakiki on 3/25/16.
 */
public final class ItineraryContract {
    /**
     * Database table and columns
     */
    static final String TABLE = "itinerary";
    public interface Columns extends BaseColumns {
        //_ID provided by base columns
        String ITINERARY_ID = "itinerary_id";
        String CLOSEST_CITY = "closest_city";
        String CLOSEST_CITY_LATITUDE = "closest_city_latitude";
        String CLOSEST_CITY_LONGITUDE = "closest_city_longitude";
        String NAME = "name";
        String EMAIL = "email";
        String PHONE = "phone";
        String DATA_SOURCE = "data_source";
        String CREATED_DATETIME = "created_datetime";
    }

    /**
     * SQL statements
     */
    //create table
    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE +
                    " ( " +
                    BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                    Columns.ITINERARY_ID + " INTEGER NOT NULL, " +
                    Columns.CLOSEST_CITY + " TEXT NOT NULL, " +
                    Columns.CLOSEST_CITY_LATITUDE + " REAL NOT NULL, " +
                    Columns.CLOSEST_CITY_LONGITUDE + " REAL NOT NULL, " +
                    Columns.NAME + " TEXT, " +
                    Columns.EMAIL + " TEXT, " +
                    Columns.PHONE + " TEXT, " +
                    Columns.DATA_SOURCE + " INTEGER NOT NULL, " +
                    Columns.CREATED_DATETIME + " INTEGER NOT NULL" +
                    " )";
    //index name
    static final String INDEX_NAME = "itinerary_id_index";
    //create index
    static final String CREATE_INDEX =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_NAME +
                    " ON " + TABLE +
                    " ( " + Columns.ITINERARY_ID + " )";
    //drop table
    static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE;

    /**
     * Content Provider related
     */
    //uri definition: content://authority/table
    static final String AUTHORITY = ItineraryContentProvider.class.getCanonicalName();
    static final Uri URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .appendPath(TABLE)
            .build();

    //uri matcher and patterns - defined in ItineraryContentProvider

    //types
    //content type: multiple items returned
    static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE;
    //item type: specific item returned
    static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE;

    //methods - defined in ItineraryContentProvider
    //manifest - declared in android manifest
}
