package com.example.yeelin.projects.betweenus.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.yeelin.projects.betweenus.receiver.PlacesBroadcastReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Created by ninjakiki on 8/21/15.
 */
public class PlacesFetchServiceHandler
        extends Handler {
    //logcat
    private static final String TAG = PlacesFetchServiceHandler.class.getCanonicalName();

    //message types
    public static final int MESSAGE_PLACE_DETAILS_FETCH = 100;
    public static final int MESSAGE_PLACE_API_CONNECT = 110;

    //timeout for api connect and subsequent calls
    private static final int TIMEOUT_SECONDS = 30;

    //member variables
    private Context applicationContext;
    private GoogleApiClient googleApiClient;
    private PendingResult<PlaceBuffer> placePendingResult;

    /**
     * Instantiate a google api client object
     * @param looper
     * @param context
     */
    public PlacesFetchServiceHandler(Looper looper, Context context) {
        super(looper);
        applicationContext = context.getApplicationContext();
        googleApiClient = new GoogleApiClient
                .Builder(applicationContext)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    /**
     * Handle the dequeued message on a background thread
     * @param msg
     */
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_PLACE_API_CONNECT:
                Log.d(TAG, "handleMessage: MESSAGE_PLACE_API_CONNECT");
                //check if we are connected already for some reason, and connect if we are not
                if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                    connect();
                }
                break;

            case MESSAGE_PLACE_DETAILS_FETCH:
                Log.d(TAG, "handleMessage: MESSAGE_PLACE_DETAILS_FETCH");
                //check if we are still connected, and reconnect if needed
                if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                    connect();
                }

                //call the api to get the place details
                ArrayList<String> placeIds = (ArrayList<String>) msg.obj;
                fetch(placeIds);
                break;

            default:
                super.handleMessage(msg);
        }
    }

    /**
     * Performs a blocking connect on the google api client. Broadcasts the result back.
     */
    private void connect() {
        Log.d(TAG, "handleConnect: GoogleApiClient connecting");
        ConnectionResult connectionResult = googleApiClient.blockingConnect(TIMEOUT_SECONDS, TimeUnit.SECONDS); //blocking call

        //if successful, we are done
        if (connectionResult.isSuccess()) {
            Log.d(TAG, "handleConnect: GoogleApiClient connected");
            return;
        }

        //else try to resolve
        if (connectionResult.hasResolution()) {
            //request the user take immediate action to resolve the error
            PlacesBroadcastReceiver.broadcastConnectFailure(applicationContext, connectionResult, PlacesBroadcastReceiver.HAS_RESOLUTION);
        }
        else {
            //Show error dialog using GooglePlayServicesUtil.getErrorDialog()
            PlacesBroadcastReceiver.broadcastConnectFailure(applicationContext, connectionResult, PlacesBroadcastReceiver.NO_RESOLUTION);
        }
    }

    /**
     * Performs a blocking fetch. Fetches the place latlngs given the place ids and broadcasts it back.
     * Releases resources when done.
      * @param placeIds
     */
    private void fetch(final ArrayList<String> placeIds) {
        placePendingResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeIds.get(0), placeIds.get(1));
        final PlaceBuffer result = placePendingResult.await(TIMEOUT_SECONDS, TimeUnit.SECONDS); //blocking call

        //process result
        if (result.getStatus().isSuccess()) {
            Log.d(TAG, "handleFetch: Success contacting getPlaceById API");
            //found places
            final Place userPlace = result.get(0);
            final Place friendPlace = result.get(1);
            //broadcast lat long back
            PlacesBroadcastReceiver.broadcastPlacesSuccess(applicationContext, userPlace.getLatLng(), friendPlace.getLatLng());
        }
        else {
            Log.d(TAG, "handleFetch: Error contacting getPlaceById API:" + result.getStatus().toString());
            //broadcast error
            PlacesBroadcastReceiver.broadcastPlacesFailure(applicationContext);
        }
        //clean up
        releaseResources(result);
    }

    /**
     * Clean up the resources
     * @param result
     */
    private void releaseResources(final PlaceBuffer result) {
        result.release();
        placePendingResult.cancel();
        placePendingResult = null;
    }
}
