package com.example.yeelin.projects.betweenus.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 8/24/15.
 */
public class PlacesBroadcastReceiver extends BroadcastReceiver {
    //logcat
    private static final String TAG = PlacesBroadcastReceiver.class.getCanonicalName();

    //actions for intent
    private static final String ACTION_PLACES_SUCCESS = PlacesBroadcastReceiver.class.getCanonicalName() + ".action.PLACES_SUCCESS";
    private static final String ACTION_PLACES_FAILURE = PlacesBroadcastReceiver.class.getCanonicalName() + ".action.PLACES_FAILURE";
    private static final String EXTRA_USER_LATLNG = PlacesBroadcastReceiver.class.getSimpleName() + ".userLatLng";
    private static final String EXTRA_FRIEND_LATLNG = PlacesBroadcastReceiver.class.getSimpleName() + ".friendLatLng";

    //member variables
    private final Context applicationContext;
    private final WeakReference<PlacesBroadcastListener> listenerWeakReference;


    /**
     * This should be implemented by the activities or fragments that are interested
     * in the broadcast
     */
    public interface PlacesBroadcastListener {
        public void onPlacesSuccess(final LatLng userLatLng, final LatLng friendLatLng);
        public void onPlacesFailure();
    }

    /**
     * Broadcast success
     * @param context
     * @param userLatLng
     * @param friendLatLng
     */
    public static void broadcastPlacesSuccess(Context context, LatLng userLatLng, LatLng friendLatLng) {
        Intent intent = new Intent(ACTION_PLACES_SUCCESS);
        intent.putExtra(EXTRA_USER_LATLNG, userLatLng);
        intent.putExtra(EXTRA_FRIEND_LATLNG, friendLatLng);

        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Broadcast failure
     * @param context
     */
    public static void broadcastPlacesFailure(Context context) {
        Intent intent = new Intent(ACTION_PLACES_FAILURE);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Creates a broadcast receiver and register yourself to receive a particular intent filter
     * @param context
     * @param listener
     */
    public PlacesBroadcastReceiver(Context context, PlacesBroadcastListener listener) {
        applicationContext = context.getApplicationContext();
        listenerWeakReference = new WeakReference<>(listener);

        //create intent filter
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PLACES_SUCCESS);
        intentFilter.addAction(ACTION_PLACES_FAILURE);

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
        PlacesBroadcastListener listener = listenerWeakReference.get();
        if (listener == null) {
            Log.d(TAG, "onReceive: Listener is null, so nothing to do");
            return;
        }

        final String action = intent.getAction();
        if (ACTION_PLACES_SUCCESS.equalsIgnoreCase(action)) {
            final LatLng userLatLng = intent.getParcelableExtra(EXTRA_USER_LATLNG);
            final LatLng friendLatLng = intent.getParcelableExtra(EXTRA_FRIEND_LATLNG);
            listener.onPlacesSuccess(userLatLng, friendLatLng);
        }
        else if (ACTION_PLACES_FAILURE.equalsIgnoreCase(action)) {
            listener.onPlacesFailure();
        }
        else {
            Log.d(TAG, "onReceive: Unknown action:" + action);
        }
    }
}
