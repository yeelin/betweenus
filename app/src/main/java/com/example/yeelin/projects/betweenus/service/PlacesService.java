package com.example.yeelin.projects.betweenus.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.example.yeelin.projects.betweenus.model.LocationSearchItem;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 8/20/15.
 */
public class PlacesService extends Service {
    private static final String TAG = PlacesService.class.getCanonicalName();
    //intent action
    private static final String ACTION_GET_PLACE_BY_ID = PlacesService.class.getSimpleName() + ".action.getPlaceById";
    private static final String ACTION_API_CONNECT = PlacesService.class.getSimpleName() + ".action.apiConnect";

    //intent extras
    private static final String EXTRA_PLACE_IDS = PlacesService.class.getSimpleName() + ".placeIds";
    private static final String EXTRA_QUERY = PlacesService.class.getSimpleName() + ".query";

    //member variables
    private PlacesServiceHandler serviceHandler;
    private Binder binder; //binder given to clients of this service if they are bound

    /**
     * Builds an intent to load data for one or more places given its place id.
     * Pass this intent to context.startService.
     * @param context
     * @param placeIds
     * @return
     */
    public static Intent buildGetPlaceByIdIntent(Context context, ArrayList<String> placeIds) {
        Intent intent = new Intent(context, PlacesService.class);
        intent.setAction(ACTION_GET_PLACE_BY_ID);
        intent.putStringArrayListExtra(EXTRA_PLACE_IDS, placeIds);
        return intent;
    }

    /**
     * Builds an intent to connect to the place API.
     * @param context
     * @return
     */
    public static Intent buildApiConnectIntent(Context context) {
        Intent intent = new Intent(context, PlacesService.class);
        intent.setAction(ACTION_API_CONNECT);
        return intent;
    }

    /**
     * Builds an intent for binding to this service.
     * @param context
     * @return
     */
    public static Intent buildBindIntent(Context context) {
        return new Intent(context, PlacesService.class);
    }

    /**
     * Builds an intent to stop this service
     * @param context
     * @return
     */
    public static Intent buildStopServiceIntent(Context context) {
        return new Intent(context, PlacesService.class);
    }

    /**
     * Start up the thread running the service. Note that we create a
     * separate thread because the service normally runs in the process's
     * main thread, which we don't want to block.
     */
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        //create a separate bg thread for the handler
        HandlerThread thread = new HandlerThread(PlacesService.class.getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        //create a handler using the thread's looper
        serviceHandler = new PlacesServiceHandler(thread.getLooper(), this);
        //create a binder
        binder = new PlacesServiceBinder();
    }

    /**
     * Read the intent and for each start request, send a message to start a job and deliver the start id
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        //create the appropriate message given the intent
        final String action = intent.getAction();
        Message message = null;
        if (ACTION_API_CONNECT.equalsIgnoreCase(action)) {
            message = serviceHandler.obtainMessage(PlacesServiceHandler.MESSAGE_API_CONNECT, startId, 0);
        }
        else if (ACTION_GET_PLACE_BY_ID.equalsIgnoreCase(action)) {
            final ArrayList<String> placeIds = intent.getStringArrayListExtra(EXTRA_PLACE_IDS);
            message = serviceHandler.obtainMessage(PlacesServiceHandler.MESSAGE_GET_PLACE_BY_ID, startId, 0, placeIds);
        }

        //ask the handler to queue the message
        if (message != null) {
            serviceHandler.sendMessage(message);
        }

        //if we get killed, after returning from here, restart with the same intent
        return START_REDELIVER_INTENT;
    }

    /**
     * Multiple clients can connect to the service at once. However, the system calls your service's onBind()
     * method to retrieve the IBinder only when the first client binds. The system then delivers the same
     * IBinder to any additional clients that bind, without calling onBind() again.
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    /**
     * Terminate the handler's looper without processing any more messages in the queue.
     * Doing this in onDestroy since we created the handler in onCreate
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        //release the binder
        binder = null;
        //release the service handler
        serviceHandler.getLooper().quit();
        serviceHandler.releaseAllResources();
        serviceHandler = null;

        super.onDestroy();
    }

    /**
     * PlacesServiceBinder
     * Provides an programming interface that clicents can use to interact with the service
     * An instance of this class is returned in onBind().
     * The client receives the Binder and can use it to directly access public methods
     * available in the Binder implementation.
     */
    public class PlacesServiceBinder extends Binder {
        PlacesServiceBinder() {
            super();
        }

        /**
         * Request for autocomplete results.  The client will receive a callback on success and failure
         * @param queryCallbackPair pair consisting of the query and the client which must implement the AutocompleteCallback
         *                          interface
         */
        public void requestAutocompleteResults(Pair<String, AutocompleteCallback> queryCallbackPair) {
            Message message = serviceHandler.obtainMessage(PlacesServiceHandler.MESSAGE_AUTOCOMPLETE, queryCallbackPair);
            serviceHandler.sendMessage(message);
        }
    }

    /**
     * AutocompleteCallback interface
     * This should be implemented by activities or fragments interested in getting
     * a callback after requesting autocomplete results
     */
    public interface AutocompleteCallback {
        void onAutocompleteResult(ArrayList<LocationSearchItem> items);
        void onAutocompleteFailure(int statusCode, String statusMessage);
    }
}
