package com.example.yeelin.projects.betweenus.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by ninjakiki on 3/26/16.
 */
public class ItineraryContentProvider extends ContentProvider {
    private static final String TAG = ItineraryContentProvider.class.getCanonicalName();

    //uri matcher: converts a uri into an int value for easier comparison
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //uri matcher: possible return values for uri matcher
    private static final int ALL_ROWS = 0;
    private static final int ROW_BY_ITINERARY_ID = 1;
    private static final int ROWS_BY_CLOSEST_CITY = 2;

    //uri matcher: possible match patterns
    static {
        uriMatcher.addURI(ItineraryContract.AUTHORITY, ItineraryContract.TABLE, ALL_ROWS); //select all from itinerary table
        uriMatcher.addURI(ItineraryContract.AUTHORITY, ItineraryContract.TABLE + "/#", ROW_BY_ITINERARY_ID); //select by itinerary id
        uriMatcher.addURI(ItineraryContract.AUTHORITY, ItineraryContract.TABLE + "/*", ROWS_BY_CLOSEST_CITY); //select by closest city string
    }

    //member variables
    private DBHelper dbHelper; //sqlite open helper

    //static methods for building Uris
    public static Uri buildUri () {
        return ItineraryContract.URI; //content://authority/table
    }

    public static Uri buildUri (long itineraryId) {
        Uri uri = ItineraryContract.URI;
        return ContentUris.withAppendedId(uri, itineraryId);
    }

    public static Uri buildUri (String closestCity) {
        Uri uri = ItineraryContract.URI;
        return uri.buildUpon()
                .appendPath(closestCity)
                .build();
    }

    /**
     * Create a sqlite open helper to access the db
     * @return
     */
    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        dbHelper = DBHelper.getInstance(getContext());
        return true;
    }

    /**
     * Query operation. Supports query all, query by itinerary id, query by closest city
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //get readable db
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //validate uri
        int match = uriMatcher.match(uri);
        switch (match) {
            case ALL_ROWS: //uri ends with "table"
                break;
            case ROW_BY_ITINERARY_ID: //uri ends with "table/#"
                break;
            case ROWS_BY_CLOSEST_CITY: //uri ends with "table/*"
                break;
            default:
                throw new UnsupportedOperationException("query: Unknown uri. Uri:" + uri);
        }
        //try to query
        Cursor cursor = db.query(ItineraryContract.TABLE, projection, selection, selectionArgs, null, null, sortOrder); //null for groupby and having
        //register cursor to watch uri for changes so that caller will know if the data changes later
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns the content type (multiple items or specific item) given a uri.
     * @param uri
     * @return
     */
    @Nullable
    @Override
    public String getType(Uri uri) {
        int match = uriMatcher.match(uri);
        switch (match) {
            case ALL_ROWS:
                return ItineraryContract.CONTENT_TYPE;
            case ROW_BY_ITINERARY_ID:
                return ItineraryContract.CONTENT_ITEM_TYPE;
            case ROWS_BY_CLOSEST_CITY:
                return ItineraryContract.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("getType: Unknown Uri:" + uri);
        }
    }

    /**
     * Insert operation
     * @param uri
     * @param values
     * @return
     */
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //get a writable db
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //validate uri
        int match = uriMatcher.match(uri);
        switch (match) {
            case ALL_ROWS: //uri ends with "table"
                break;
            case ROW_BY_ITINERARY_ID: //uri ends with "table/#"
                throw new UnsupportedOperationException("insert: Unable to insert by Itinerary id. Uri:" + uri);
            case ROWS_BY_CLOSEST_CITY: //uri ends with table/*"
                throw new UnsupportedOperationException("insert: Unable to insert by Closest city. Uri:" + uri);
            default:
                throw new UnsupportedOperationException("insert: Unknown uri. Uri:" + uri);
        }
        //try to insert
        long id = -1;
        db.beginTransactionNonExclusive();
        try {
            id = db.insertWithOnConflict(
                    ItineraryContract.TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        //notify users with active cursors to reload data
        getContext().getContentResolver().notifyChange(uri, null, false);
        //return uri with appended row id
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Bulk insert operation. Supports bulk insert all.
     * @param uri
     * @param values
     * @return
     */
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        //get a writable db
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //validate uri
        int match = uriMatcher.match(uri);
        switch (match) {
            case ALL_ROWS: //uri ends with "table"
                break;
            case ROW_BY_ITINERARY_ID: //uri ends with "table/#"
                throw new UnsupportedOperationException("insert: Unable to insert by Itinerary id. Uri:" + uri);
            case ROWS_BY_CLOSEST_CITY: //uri ends with table/*"
                throw new UnsupportedOperationException("insert: Unable to insert by Closest city. Uri:" + uri);
            default:
                throw new UnsupportedOperationException("insert: Unknown uri. Uri:" + uri);
        }
        //try to bulk insert
        int rowsInserted = 0;
        db.beginTransactionNonExclusive();;
        try {
            for (ContentValues value : values) {
                long id = db.insertWithOnConflict(ItineraryContract.TABLE, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                if (id != -1)
                    ++rowsInserted;
                else
                    Log.d(TAG, "bulkInsert: Failed for row:" + value);
            }
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        //notify users with active cursors to reload data
        getContext().getContentResolver().notifyChange(uri, null, false);
        return rowsInserted;
    }

    /**
     * Delete operation. Supports delete all, delete by itinerary id
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //get writable db
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //validate uri
        int match = uriMatcher.match(uri);
        switch (match) {
            case ALL_ROWS: //uri ends with "table"
                break;
            case ROW_BY_ITINERARY_ID: //uri ends with "table/#"
                break;
            case ROWS_BY_CLOSEST_CITY: //uri ends with "table/*"
                throw new UnsupportedOperationException("delete: Unable to delete by Closest city. Uri:" + uri);
            default:
                throw new UnsupportedOperationException("delete: Unknown uri. Uri:" + uri);
        }
        //try to delete
        int rowsDeleted = 0;
        db.beginTransactionNonExclusive();
        try {
            rowsDeleted = db.delete(ItineraryContract.TABLE, selection, selectionArgs);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        //notify users with active cursors to reload data
        getContext().getContentResolver().notifyChange(uri, null, false);
        return rowsDeleted;
    }

    /**
     * Update operation. Supports update all, update by itinerary id, and update by closest city.
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //get writable db
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //validate uri
        int match = uriMatcher.match(uri);
        switch (match) {
            case ALL_ROWS: //uri ends with "table"
                break;
            case ROW_BY_ITINERARY_ID: //uri ends with "table/#"
                break;
            case ROWS_BY_CLOSEST_CITY: //uri ends with "table/*"
                break;
            default:
                throw new UnsupportedOperationException("update: Unknown uri. Uri:" + uri);
        }
        //try to update
        int rowsUpdated = 0;
        db.beginTransactionNonExclusive();
        try {
            rowsUpdated = db.update(ItineraryContract.TABLE, values, selection, selectionArgs);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        //notify users with active cursors to reload data
        getContext().getContentResolver().notifyChange(uri, null, false);
        return rowsUpdated;
    }
}
