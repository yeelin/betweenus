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
        Log.d(TAG, "handleMessage");
        if (msg.what == MESSAGE_PLACE_DETAILS_FETCH) {
            ArrayList<String> placeIds = (ArrayList<String>) msg.obj;


            if (!googleApiClient.isConnected()) {
                Log.d(TAG, "handleMessage: GoogleApiClient connecting");
                //blocking connect since we are on bg thread
                googleApiClient.blockingConnect(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }

            //call the pi to get the place detail
            placePendingResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeIds.get(0), placeIds.get(1));
            final PlaceBuffer result = placePendingResult.await(TIMEOUT_SECONDS, TimeUnit.SECONDS); //blocking call again

            if (!result.getStatus().isSuccess()) {
                //broadcast error
                PlacesBroadcastReceiver.broadcastPlacesFailure(applicationContext);
                //log error
                Log.d(TAG, "handleMessage: Error contacting getPlaceById API:" + result.getStatus().toString());

                //clean up and return
                result.release();
                placePendingResult = null;
                return;
            }

            //process the result and release the buffer
            processGetPlaceByIdResult(result);

            //clean up
            result.release();
            placePendingResult = null;
        }
        else {
            super.handleMessage(msg);
        }
    }

    /**
     * Processes the results from the placebuffer and broadcast the latlngs back to the activity
     * @param result
     */
    private void processGetPlaceByIdResult(final PlaceBuffer result) {
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
}
