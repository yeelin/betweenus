package com.example.yeelin.projects.betweenus.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ninjakiki on 3/25/16.
 */
public final class StopContract {
    /**
     * Database table and columns
     */
    static final String TABLE = "stop";
    public interface Columns extends BaseColumns {
        String PLACE_ID = "place_id";
        String DATA_SOURCE = "data_source";
    }

    /**
     * SQL statements
     */
    //create table
    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE +
                    " ( " +
                    BaseColumns._ID + " INTEGER PRIMARY KEY, " +
                    ItineraryContract.Columns.ITINERARY_ID + " INTEGER NOT NULL, " +
                    Columns.PLACE_ID + " TEXT NOT NULL, " +
                    Columns.DATA_SOURCE + " INTEGER NOT NULL" +
                    " )";
    //index name
    static final String INDEX_NAME = "itinerary_place_id_index";
    //create index
    static final String CREATE_INDEX =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_NAME +
                    " ON " + TABLE +
                    " ( " + ItineraryContract.Columns.ITINERARY_ID + ", " + Columns.PLACE_ID + " )";
    //drop table
    static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + TABLE;

    /**
     * Content Provider Related
     */
    //uri definition: scheme://authority
    static final String AUTHORITY = StopContentProvider.class.getCanonicalName();
    public static final Uri URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .appendPath(TABLE)
            .build();

    //uri matcher and patterns - defined in StopContentProvider

    //types:
    //content type: multiple items returned
    static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE;
    //item type: specific item returned
    static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE;

    //methods - defined in StopContentProvider
    //manifest - declared in android manifest
}
