package com.example.yeelin.projects.betweenus.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

/**
 * Created by ninjakiki on 3/28/16.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = DBHelper.class.getCanonicalName();
    private static final int DB_VERSION = 1;
    static final String DB_NAME = "itinerary.db";

    //singleton
    private static DBHelper dbHelper;

    /**
     * Us this instead of constructor so that we have only 1 sqliteopenhelper per db.
     * @param context
     * @return
     */
    public static synchronized DBHelper getInstance(Context context) {
        Log.d(TAG, "getInstance:");
        if (dbHelper == null)
            dbHelper = new DBHelper(context);
        return dbHelper;
    }

    /**
     * Private constructor since we should be using getInstance
     * @param context
     */
    private DBHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION); //null means we want to use the default cursor factory
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            setWriteAheadLoggingEnabled(true);
    }

    /**
     * Create all the tables and indexes in the db
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Creating a new db");
        db.execSQL(ItineraryContract.CREATE_TABLE);
        db.execSQL(ItineraryContract.CREATE_INDEX);

        db.execSQL(StopContract.CREATE_TABLE);
        db.execSQL(StopContract.CREATE_INDEX);
    }

    /**
     * Activate write ahead logging on OSes older than Jelly bean. Reduce checkpoint size and force a
     * checkpoint now
     * @param db
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

        // If supporting OS versions before JellyBean, write ahead logging methods do not exist.
        // This is how you activate for older releases.  You can only do this on a writable database.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && !db.isReadOnly())
            db.enableWriteAheadLogging();

        if (!db.isReadOnly()) {
            //reduce auto checkpoint size
            Cursor cursor = db.rawQuery("PRAGMA wal_autocheckpoint=100", null);
            if (cursor != null) cursor.close();

            //force a checkpoint now
            cursor = db.rawQuery("PRAGMA wal_checkpoint", null);
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Rebuilds the db if the version changes.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        cleanupDb(db);
        onCreate(db);
    }

    /**
     * Drops all the tables in the db
     * @param db
     */
    private void cleanupDb(SQLiteDatabase db) {
        db.execSQL(ItineraryContract.DROP_TABLE);
        db.execSQL(StopContract.DROP_TABLE);
    }
}
