package com.example.yeelin.projects.betweenus.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 8/24/15.
 */
public class PlacesBroadcastReceiver extends BroadcastReceiver {
    //logcat
    private static final String TAG = PlacesBroadcastReceiver.class.getCanonicalName();

    //actions for intent
    private static final String ACTION_GET_PLACE_BY_ID_SUCCESS = PlacesBroadcastReceiver.class.getCanonicalName() + ".action.GET_PLACE_BY_ID_SUCCESS";
    private static final String ACTION_GET_PLACE_BY_ID_FAILURE = PlacesBroadcastReceiver.class.getCanonicalName() + ".action.GET_PLACE_BY_ID_FAILURE";
    private static final String ACTION_API_CONNECT_SUCCESS = PlacesBroadcastReceiver.class.getCanonicalName() + ".action.API_CONNECT_SUCCESS";
    private static final String ACTION_API_CONNECT_FAILURE = PlacesBroadcastReceiver.class.getCanonicalName() + ".action.API_CONNECT_FAILURE";

    //extras for intent
    private static final String EXTRA_USER_LATLNG = PlacesBroadcastReceiver.class.getSimpleName() + ".userLatLng";
    private static final String EXTRA_FRIEND_LATLNG = PlacesBroadcastReceiver.class.getSimpleName() + ".friendLatLng";
    private static final String EXTRA_STATUS_CODE = PlacesBroadcastReceiver.class.getSimpleName() + ".statusCode";
    private static final String EXTRA_STATUS_MESSAGE = PlacesBroadcastReceiver.class.getSimpleName() + ".statusMessage";
    private static final String EXTRA_CONNECTION_RESULT = PlacesBroadcastReceiver.class.getSimpleName() + ".connectionResult";
    private static final String EXTRA_RESOLUTION_TYPE = PlacesBroadcastReceiver.class.getSimpleName() + ".resolutionType";

    //constants
    public static final int HAS_RESOLUTION = 0;
    public static final int NO_RESOLUTION = 1;

    //member variables
    private final Context applicationContext;
    private WeakReference<PlacesBroadcastListener> placesBroadcastListenerWeakRef;
    private WeakReference<PlacesConnectionBroadcastListener> connectionBroadcastListenerWeakRef;


    /**
     * This interface should be implemented by the activities or fragments that are interested
     * in the places broadcast
     */
    public interface PlacesBroadcastListener {
        void onPlacesSuccess(final LatLng userLatLng, final LatLng friendLatLng);
        void onPlacesFailure(final int statusCode, final String statusMessage);
    }

    /**
     * This interface should be implemented by the activities or fragments that are interested
     * in the places connection broadcast
     */
    public interface PlacesConnectionBroadcastListener {
        void onConnectSuccess();
        void onConnectFailure(final ConnectionResult connectionResult, final int resolutionType);

    }

    /**
     * Broadcast getPlaceById success
     * @param context
     * @param userLatLng
     * @param friendLatLng
     */
    public static void broadcastPlacesSuccess(Context context, LatLng userLatLng, LatLng friendLatLng) {
        Intent intent = new Intent(ACTION_GET_PLACE_BY_ID_SUCCESS);
        intent.putExtra(EXTRA_USER_LATLNG, userLatLng);
        intent.putExtra(EXTRA_FRIEND_LATLNG, friendLatLng);

        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Broadcast getPlaceById failure
     * @param context
     */
    public static void broadcastPlacesFailure(Context context, int statusCode, String statusMessage) {
        Intent intent = new Intent(ACTION_GET_PLACE_BY_ID_FAILURE);
        intent.putExtra(EXTRA_STATUS_CODE, statusCode);
        intent.putExtra(EXTRA_STATUS_MESSAGE, statusMessage);

        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Broadcast google api client connect success
     * @param context
     */
    public static void broadcastConnectSuccess(Context context) {
        Intent intent = new Intent(ACTION_API_CONNECT_SUCCESS);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Broadcast google api client connect failure
     * @param context
     * @param connectionResult
     */
    public static void broadcastConnectFailure(Context context, ConnectionResult connectionResult, int resolutionType) {
        Intent intent = new Intent(ACTION_API_CONNECT_FAILURE);
        intent.putExtra(EXTRA_CONNECTION_RESULT, connectionResult);
        intent.putExtra(EXTRA_RESOLUTION_TYPE, resolutionType);

        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Creates a broadcast receiver and register yourself to receive a particular intent filter
     * @param context
     * @param listener
     */
    public PlacesBroadcastReceiver(Context context, PlacesBroadcastListener listener) {
        applicationContext = context.getApplicationContext();
        placesBroadcastListenerWeakRef = new WeakReference<>(listener);

        //create intent filter
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GET_PLACE_BY_ID_SUCCESS);
        intentFilter.addAction(ACTION_GET_PLACE_BY_ID_FAILURE);

        //inform the local broadcast manager that we are interested in this intent filter
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext);
        localBroadcastManager.registerReceiver(this, intentFilter);
    }

    /**
     * Creates a broadcast receiver and register yourself to receive a particular intent filter
     * @param context
     * @param listener
     */
    public PlacesBroadcastReceiver(Context context, PlacesConnectionBroadcastListener listener) {
        applicationContext = context.getApplicationContext();
        connectionBroadcastListenerWeakRef = new WeakReference<>(listener);

        //create intent filter
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_API_CONNECT_SUCCESS);
        intentFilter.addAction(ACTION_API_CONNECT_FAILURE);

        //inform the local broadcast manager that we are interested in this intent filter
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext);
        localBroadcastManager.registerReceiver(this, intentFilter);
    }


    /**
     * Unregister for the broadcast
     */
    public void unregister() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext);
        localBroadcastManager.unregisterReceiver(this);
    }

    /**
     * Required override for BroadcastReceiver
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast. During this time you
     * can use the other methods on BroadcastReceiver to view/modify the current result values.
     * This method is called on the main thread.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        //check the action
        final String action = intent.getAction();
        if (ACTION_GET_PLACE_BY_ID_SUCCESS.equalsIgnoreCase(action)) {
            Log.d(TAG, "onReceive: ACTION_GET_PLACE_BY_ID_SUCCESS");
            //get the intent extras
            final LatLng userLatLng = intent.getParcelableExtra(EXTRA_USER_LATLNG);
            final LatLng friendLatLng = intent.getParcelableExtra(EXTRA_FRIEND_LATLNG);

            //broadcast
            final PlacesBroadcastListener listener = placesBroadcastListenerWeakRef.get();
            if (listener != null) {
                listener.onPlacesSuccess(userLatLng, friendLatLng);
            }
        }
        else if (ACTION_GET_PLACE_BY_ID_FAILURE.equalsIgnoreCase(action)) {
            Log.d(TAG, "onReceive: ACTION_GET_PLACE_BY_ID_FAILURE");
            //get intent extras
            final int statusCode = intent.getIntExtra(EXTRA_STATUS_CODE, 0);
            final String statusMessage = intent.getStringExtra(EXTRA_STATUS_MESSAGE);
            //broadcast
            final PlacesBroadcastListener listener = placesBroadcastListenerWeakRef.get();
            if (listener != null) {
                listener.onPlacesFailure(statusCode, statusMessage);
            }
        }
        else if (ACTION_API_CONNECT_SUCCESS.equalsIgnoreCase(action)) {
            Log.d(TAG, "onReceive: ACTION_API_CONNECT_SUCCESS");
            //broadcast
            final PlacesConnectionBroadcastListener listener = connectionBroadcastListenerWeakRef.get();
            if (listener != null) {
                listener.onConnectSuccess();
            }
        }
        else if (ACTION_API_CONNECT_FAILURE.equalsIgnoreCase(action)) {
            Log.d(TAG, "onReceive: ACTION_API_CONNECT_FAILURE");
            //get intent extras
            final ConnectionResult connectionResult = intent.getParcelableExtra(EXTRA_CONNECTION_RESULT);
            final int resolutionType = intent.getIntExtra(EXTRA_RESOLUTION_TYPE, NO_RESOLUTION);
            //broadcast
            final PlacesConnectionBroadcastListener listener = connectionBroadcastListenerWeakRef.get();
            if (listener != null) {
                listener.onConnectFailure(connectionResult, resolutionType);
            }
        }
        else {
            Log.d(TAG, "onReceive: Unknown action:" + action);
        }
    }
}
