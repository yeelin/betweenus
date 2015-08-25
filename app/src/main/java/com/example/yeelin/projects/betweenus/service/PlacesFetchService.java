package com.example.yeelin.projects.betweenus.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 8/20/15.
 */
public class PlacesFetchService extends Service {
    private static final String TAG = PlacesFetchService.class.getCanonicalName();
    //intent action and extras
    private static final String ACTION_PLACE_DETAILS_FETCH = PlacesFetchService.class.getSimpleName() + ".action.placeSearchById";
    private static final String EXTRA_PLACE_IDS = PlacesFetchService.class.getSimpleName() + ".placeIds";

    //member variables
    private PlacesFetchServiceHandler serviceHandler;

    /**
     * Builds an intent to load data for a single place given a place id.  Pass this intent to context.startService.
     * @param context
     * @param placeIds
     * @return
     */
    public static Intent buildPlaceDetailsFetchIntent(Context context, ArrayList<String> placeIds) {
        Intent intent = new Intent(context, PlacesFetchService.class);
        intent.setAction(ACTION_PLACE_DETAILS_FETCH);
        intent.putStringArrayListExtra(EXTRA_PLACE_IDS, placeIds);
        return intent;
    }

    /**
     * Builds an intent to stop this service
     * @param context
     * @return
     */
    public static Intent buildPlaceDetailsStopFetchIntent(Context context) {
        return new Intent(context, PlacesFetchService.class);
    }

    /**
     * Start up the thread running the service. Note that we create a
     * separate thread because the service normally runs in the process's
     * main thread, which we don't want to block.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        //create a separate bg thread for the handler
        HandlerThread thread = new HandlerThread(PlacesFetchService.class.getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        //create a handler using the thread's looper
        serviceHandler = new PlacesFetchServiceHandler(thread.getLooper(), this);
    }

    /**
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        //read the intent
        ArrayList<String> placeIds = intent.getStringArrayListExtra(EXTRA_PLACE_IDS);
        //for each start request, send a message to start a job and deliver the start id
        Message message = serviceHandler.obtainMessage(PlacesFetchServiceHandler.MESSAGE_PLACE_DETAILS_FETCH, startId, 0, placeIds);
        serviceHandler.sendMessage(message);

        //if we get killed, after returning from here, restart with the same intent
        return START_REDELIVER_INTENT;
    }

    /**
     * Not providing binding so this method always returns null
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Terminate the handler's looper without processing any more messages in the queue.
     * Doing this in onDestroy since we created the handler in onCreate
     */
    @Override
    public void onDestroy() {
        serviceHandler.getLooper().quit();
        serviceHandler = null;

        super.onDestroy();
    }
}
