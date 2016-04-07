package com.example.yeelin.projects.betweenus.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ninjakiki on 4/6/16.
 */
public class DBHelperTest extends AndroidTestCase {

    /**
     * Helper method to delete db so that we can start each tests with a clean state
     */
    void deleteDb() { getContext().deleteDatabase(DBHelper.DB_NAME); }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteDb();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBuildDb() throws Throwable {
        //db shouldn't exist yet
        File file = getContext().getDatabasePath(DBHelper.DB_NAME);
        assertFalse("Error: Db already exists", file.exists());

        //is the db opened?
        DBHelper dbHelper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        assertTrue("Error: Db is not open", db.isOpen());

        /**
         * Table creation verification
         */
        //have we created the tables we want?
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: Db was not created correctly with tables", cursor.moveToFirst());

        //build a hashset for all the tables we want to look for
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(ItineraryContract.TABLE);
        tableNameHashSet.add(StopContract.TABLE);

        //verify that the tables were created
        do {
            tableNameHashSet.remove(cursor.getString(0));
        }
        while (cursor.moveToNext());
        cursor.close();
        assertTrue("Error: Db was created without the correct tables", tableNameHashSet.isEmpty());

        /**
         * Index creation verification
         */
        //verify that the indexes were created
        cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='index'", null);
        assertTrue("Error: Db was not created correctly with indexes", cursor.moveToFirst());

        //build a hashset of all the indexes we want to look for
        final HashSet<String> indexNameHashSet = new HashSet<>();
        indexNameHashSet.add(ItineraryContract.INDEX_NAME);
        indexNameHashSet.add(StopContract.INDEX_NAME);

        // verify that the indexes have been created
        do {
            indexNameHashSet.remove(cursor.getString(0));
        }
        while (cursor.moveToNext());
        cursor.close();
        assertTrue("Error: Db was created without the correct indexes", indexNameHashSet.isEmpty());

        /**
         * Itinerary table - column verification
         */
        //verify that the correct columns were created in the table
        cursor = db.rawQuery("PRAGMA table_info(" + ItineraryContract.TABLE + ")", null);
        assertTrue("Error: Unable to query db for table info", cursor.moveToFirst());

        //build a hashset for all the columns we want to look for
        final HashSet<String> itineraryColumnNameHashSet = new HashSet<>();
        itineraryColumnNameHashSet.add(ItineraryContract.Columns._ID);
        itineraryColumnNameHashSet.add(ItineraryContract.Columns.ITINERARY_ID);
        itineraryColumnNameHashSet.add(ItineraryContract.Columns.CLOSEST_CITY);
        itineraryColumnNameHashSet.add(ItineraryContract.Columns.CLOSEST_CITY_LATITUDE);
        itineraryColumnNameHashSet.add(ItineraryContract.Columns.CLOSEST_CITY_LONGITUDE);
        itineraryColumnNameHashSet.add(ItineraryContract.Columns.NAME);
        itineraryColumnNameHashSet.add(ItineraryContract.Columns.EMAIL);
        itineraryColumnNameHashSet.add(ItineraryContract.Columns.PHONE);
        itineraryColumnNameHashSet.add(ItineraryContract.Columns.DATA_SOURCE);
        itineraryColumnNameHashSet.add(ItineraryContract.Columns.CREATED_DATETIME);

        int columnIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnIndex);
            itineraryColumnNameHashSet.remove(columnName);
        }
        while (cursor.moveToNext());
        cursor.close();
        assertTrue("Error: Table was created without the correct columns", itineraryColumnNameHashSet.isEmpty());

        /**
         * Stop table - column verification
         */
        //verify that the correct columns were created in the table
        cursor = db.rawQuery("PRAGMA table_info(" + StopContract.TABLE + ")", null);
        assertTrue("Error: Unable to query db for table info", cursor.moveToFirst());

        //build a hashset for all the columns we want to look for
        final HashSet<String> stopColumnNameHashSet = new HashSet<>();
        stopColumnNameHashSet.add(StopContract.Columns._ID);
        stopColumnNameHashSet.add(ItineraryContract.Columns.ITINERARY_ID);
        stopColumnNameHashSet.add(StopContract.Columns.PLACE_ID);
        stopColumnNameHashSet.add(StopContract.Columns.DATA_SOURCE);

        columnIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnIndex);
            stopColumnNameHashSet.remove(columnName);
        }
        while (cursor.moveToNext());
        cursor.close();
        assertTrue("Error: Table was created without the correct columns", stopColumnNameHashSet.isEmpty());

        //cleanup
        db.close();
        dbHelper.close();
    }

    /**
     * Itinerary table
     * Test insert
     */
    public void testInsert_ItineraryTable() throws Throwable {
        DBHelper dbHelper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues insertValues = DataUtilities.Itinerary.insertValues();
        long rowId = -1;
        rowId = db.insertWithOnConflict(
                ItineraryContract.TABLE,
                null,
                insertValues,
                SQLiteDatabase.CONFLICT_REPLACE);
        assertTrue("Error: RowId should not be -1", rowId != -1);

        Cursor insertCursor = db.query(
                ItineraryContract.TABLE,
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );
        insertValues.put(ItineraryContract.Columns._ID, rowId);
        validateCursor("Insert Cursor", insertCursor, insertValues);
        insertCursor.close();
    }

    /**
     * Stop table
     * Test bulk insert
     */
    public void testBulkInsert_StopTable() throws Throwable {
        DBHelper dbHelper = DBHelper.getInstance(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<ContentValues> bulkInsertValues = DataUtilities.Stop.bulkInsertValues();
        for (int i=0; i<bulkInsertValues.size(); i++) {
            ContentValues values = bulkInsertValues.get(i);

            long rowId = -1;
            rowId = db.insertWithOnConflict(
                    StopContract.TABLE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE);
            assertTrue("Error: RowId should not be -1", rowId != -1);

            Cursor insertCursor = db.query(
                    StopContract.TABLE,
                    null, // all columns
                    StopContract.Columns.PLACE_ID+"=?", // Columns for the "where" clause
                    new String[] {(String) values.get(StopContract.Columns.PLACE_ID)}, // Values for the "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null // sort order
            );

            values.put(StopContract.Columns._ID, rowId);
            validateCursor("Insert Cursor", insertCursor, values);
            insertCursor.close();
        }
    }

    private static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Error: Null cursor returned." + error, valueCursor != null);
        assertTrue("Error: Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned in cursor. " + error, valueCursor.moveToNext());
    }

    private static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {

            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Error: Column '" + columnName + "' not found. " + error, idx == -1);

            String expectedValue = entry.getValue().toString();
            assertEquals("Error: Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }
}
