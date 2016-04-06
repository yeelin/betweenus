package com.example.yeelin.projects.betweenus.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.example.yeelin.projects.betweenus.data.generic.model.SimplifiedBusiness;
import com.example.yeelin.projects.betweenus.provider.ItineraryContract;
import com.example.yeelin.projects.betweenus.provider.StopContract;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ninjakiki on 4/5/16.
 */
public class ItineraryIntentService
        extends IntentService {
    private static final String TAG = ItineraryIntentService.class.getCanonicalName();

    //action in intent
    private static final String ACTION_SAVE_ITINERARY = ItineraryIntentService.class.getSimpleName() + ".action.saveItinerary";

    //extras in intent
    private static final String EXTRA_SELECTED_ITEMS = ItineraryIntentService.class.getSimpleName() + ".selectedItems";
    private static final String EXTRA_EMAIL = ItineraryIntentService.class.getSimpleName() + ".email";
    private static final String EXTRA_PHONE = ItineraryIntentService.class.getSimpleName() + ".phone";
    private static final String EXTRA_NAME = ItineraryIntentService.class.getSimpleName() + ".name";

    /**
     *
     * @param context
     * @return
     */
    public static Intent buildIntent(Context context, ArrayList<SimplifiedBusiness> selectedItems, String name, String email, String phone) {
        Intent intent = new Intent(context, ItineraryIntentService.class);
        //set action
        intent.setAction(ACTION_SAVE_ITINERARY);
        //set extras
        intent.putParcelableArrayListExtra(EXTRA_SELECTED_ITEMS, selectedItems);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PHONE, phone);
        return intent;
    }

    public ItineraryIntentService() {
        super(ItineraryIntentService.class.getSimpleName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ItineraryIntentService(String name) {
        super(name);
    }

    /**
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: Starting");
        if (intent == null) return;

        final String action = intent.getAction();
        if (action.equals(ACTION_SAVE_ITINERARY)) {
            Log.d(TAG, "onHandleIntent: Save itinerary action");

            //read extras
            ArrayList<SimplifiedBusiness> selectedItems = intent.getParcelableArrayListExtra(EXTRA_SELECTED_ITEMS);
            String name = intent.getStringExtra(EXTRA_NAME);
            String email = intent.getStringExtra(EXTRA_EMAIL);
            String phone = intent.getStringExtra(EXTRA_PHONE);
            if (selectedItems != null) {
                saveItinerary(selectedItems, name, email, phone);
            }

        }
        else {
            Log.d(TAG, "onHandleIntent: Unknown action: " + action);
        }
        Log.d(TAG, "onHandleIntent: Done");
    }

    /**
     * Saves the given parameters as an itinerary with stops.
     * @param selectedItems
     * @param name
     * @param email
     * @param phone
     */
    private void saveItinerary(ArrayList<SimplifiedBusiness> selectedItems, @Nullable String name, @Nullable String email, @Nullable String phone) {
        Log.d(TAG, "saveItinerary:");

        //build content values
        Pair<ContentValues, ArrayList<ContentValues>> cvPair = buildContentValues(selectedItems, name, email, phone);

        //persist data
        persistData(cvPair.first, cvPair.second);
    }

    /**
     * Builds content values for the itinerary table and the stops table.
     * @param selectedItems
     * @param name
     * @param email
     * @param phone
     * @return
     */
    private Pair<ContentValues, ArrayList<ContentValues>> buildContentValues(ArrayList<SimplifiedBusiness> selectedItems, String name, String email, String phone) {
        Log.d(TAG, "buildContentValues");

        final long currentTimeMillis = new Date().getTime();
        //TODO: create content values arraylist for Stop table
        int dataSource = -1;
        ArrayList<ContentValues> stops = new ArrayList<>(selectedItems.size());
        for (int i=0; i<selectedItems.size(); i++) {
            SimplifiedBusiness business = selectedItems.get(i);
            ContentValues stop = new ContentValues();

            stop.put(ItineraryContract.Columns.ITINERARY_ID, currentTimeMillis); //for now, current time would serve as itinerary id
            stop.put(StopContract.Columns.PLACE_ID, business.getId());
            stop.put(StopContract.Columns.DATA_SOURCE, business.getDataSource());
            dataSource = business.getDataSource();

            stops.add(stop);
        }

        //TODO: create content values for Itinerary table
        ContentValues itinerary = new ContentValues();
        itinerary.put(ItineraryContract.Columns.ITINERARY_ID, currentTimeMillis); //for now, current time would serve as itinerary id
        itinerary.put(ItineraryContract.Columns.CLOSEST_CITY, name);
        itinerary.put(ItineraryContract.Columns.CLOSEST_CITY_LATITUDE, 0.0);
        itinerary.put(ItineraryContract.Columns.CLOSEST_CITY_LONGITUDE, 0.0);
        if (name != null) itinerary.put(ItineraryContract.Columns.NAME, name);
        if (email != null) itinerary.put(ItineraryContract.Columns.EMAIL, email);
        if (phone != null) itinerary.put(ItineraryContract.Columns.PHONE, phone);
        itinerary.put(ItineraryContract.Columns.DATA_SOURCE, dataSource);
        itinerary.put(ItineraryContract.Columns.CREATED_DATETIME, currentTimeMillis);

        return new Pair<>(itinerary, stops);
    }

    /**
     * Saves the given itinerary to the itinerary table, and the stops to the stops table.
     * @param itinerary
     * @param stops
     */
    private void persistData(ContentValues itinerary, ArrayList<ContentValues> stops) {
        Log.d(TAG, "persistData");

        //insert into Itinerary table
        getContentResolver().insert(
                ItineraryContract.URI,
                itinerary);

        //insert into Stops table
        getContentResolver().bulkInsert(
                StopContract.URI,
                stops.toArray(new ContentValues[stops.size()]));
    }
}
