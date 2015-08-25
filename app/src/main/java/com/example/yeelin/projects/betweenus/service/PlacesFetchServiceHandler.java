package com.example.yeelin.projects.betweenus.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.yeelin.projects.betweenus.receiver.PlacesBroadcastReceiver;
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
                if (!googleApiClient.isConnected()) {
                    Log.d(TAG, "handleMessage: GoogleApiClient connecting");
                    googleApiClient.blockingConnect(TIMEOUT_SECONDS, TimeUnit.SECONDS); //blocking call
                }
                Log.d(TAG, "handleMessage: GoogleApiClient connected");
                break;

            case MESSAGE_PLACE_DETAILS_FETCH:
                Log.d(TAG, "handleMessage: MESSAGE_PLACE_DETAILS_FETCH");
                //check if we are still connected, and reconnect if needed
                if (!googleApiClient.isConnected()) {
                    Log.d(TAG, "handleMessage: GoogleApiClient Reconnecting");
                    googleApiClient.blockingConnect(TIMEOUT_SECONDS, TimeUnit.SECONDS); //blocking call
                    Log.d(TAG, "handleMessage: GoogleApiClient connected");
                }

                //call the api to get the place details
                ArrayList<String> placeIds = (ArrayList<String>) msg.obj;
                placePendingResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeIds.get(0), placeIds.get(1));
                final PlaceBuffer result = placePendingResult.await(TIMEOUT_SECONDS, TimeUnit.SECONDS); //blocking call again

                //process result
                if (result.getStatus().isSuccess()) {
                    handleGetPlaceByIdSuccess(result);
                }
                else {
                    handleGetPlaceByIdFailure(result);
                }

                //clean up
                releaseResources(result);
                break;

            default:
                super.handleMessage(msg);
        }
    }

    /**
     * Processes the results from the placebuffer and broadcast the latlngs back to the activity
     * @param result
     */
    private void handleGetPlaceByIdSuccess(final PlaceBuffer result) {
        //found place
        final Place userPlace = result.get(0);
        Log.d(TAG, String.format("processGetPlaceByIdResult: User place. Name:%s LatLng:%f, %f PlaceTypes:%s",
                userPlace.getName().toString(), userPlace.getLatLng().latitude, userPlace.getLatLng().longitude, userPlace.getPlaceTypes().toString()));

        final Place friendPlace = result.get(1);
        Log.d(TAG, String.format("processGetPlaceByIdResult: Friend place. Name:%s LatLng:%f, %f PlaceTypes:%s",
                friendPlace.getName().toString(), friendPlace.getLatLng().latitude, friendPlace.getLatLng().longitude, friendPlace.getPlaceTypes().toString()));

        //broadcast lat long back
        PlacesBroadcastReceiver.broadcastPlacesSuccess(applicationContext, userPlace.getLatLng(), friendPlace.getLatLng());
    }

    /**
     * Handle the failure to retrieve results from getPlaceById api.
     * @param result
     */
    private void handleGetPlaceByIdFailure(final PlaceBuffer result) {
        //broadcast error
        PlacesBroadcastReceiver.broadcastPlacesFailure(applicationContext);
        //log error
        Log.d(TAG, "handleMessage: Error contacting getPlaceById API:" + result.getStatus().toString());
    }

    /**
     * Clean up the resources
     * @param result
     */
    private void releaseResources(final PlaceBuffer result) {
        result.release();
        placePendingResult = null;
    }
}
