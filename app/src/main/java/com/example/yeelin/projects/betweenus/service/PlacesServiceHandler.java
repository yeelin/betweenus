package com.example.yeelin.projects.betweenus.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.example.yeelin.projects.betweenus.adapter.LocationSearchItem;
import com.example.yeelin.projects.betweenus.receiver.PlacesBroadcastReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Created by ninjakiki on 8/21/15.
 */
public class PlacesServiceHandler
        extends Handler {
    //logcat
    private static final String TAG = PlacesServiceHandler.class.getCanonicalName();

    //message types
    public static final int MESSAGE_GET_PLACE_BY_ID = 100;
    public static final int MESSAGE_API_CONNECT = 110;
    public static final int MESSAGE_AUTOCOMPLETE = 120;

    //timeout for api connect and subsequent calls
    private static final int TIMEOUT_SECONDS = 30;

    //constants for autocomplete
    private static final LatLng US_SW = new LatLng(24.498899, -124.422985);
    private static final LatLng US_NE = new LatLng(48.902104, -67.008434);
    private static final LatLngBounds US_LAT_LNG_BOUNDS = new LatLngBounds(US_SW, US_NE); //rectangle encapsulating USA

    //member variables
    private Context applicationContext;
    private GoogleApiClient googleApiClient;
    private PendingResult<PlaceBuffer> placePendingResult;
    private PendingResult<AutocompletePredictionBuffer> autocompletePendingResult;
    private Handler mainHandler;

    /**
     * Instantiate a google api client object
     * @param looper
     * @param context
     */
    public PlacesServiceHandler(Looper looper, Context context) {
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
            case MESSAGE_API_CONNECT:
                Log.d(TAG, "handleMessage: MESSAGE_API_CONNECT");
                //check if we are connected already for some reason, and connect if we are not
                if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                    connect();
                }
                break;

            case MESSAGE_GET_PLACE_BY_ID:
                Log.d(TAG, "handleMessage: MESSAGE_GET_PLACE_BY_ID");
                //check if we are still connected, and reconnect if needed
                if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                    connect();
                }

                //call the api to get the place details
                final ArrayList<String> placeIds = (ArrayList<String>) msg.obj;
                fetch(placeIds);
                break;

            case MESSAGE_AUTOCOMPLETE:
                Log.d(TAG, "handleMessage: MESSAGE_AUTOCOMPLETE");
                //check if we are still connected, and reconnect if needed
                if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                    connect();
                }

                //call the autocomplete api
                final Pair<String, PlacesService.AutocompleteCallback> queryCallbackPair = (Pair<String, PlacesService.AutocompleteCallback>) msg.obj;
                autocomplete(queryCallbackPair);
                break;

            default:
                super.handleMessage(msg);
        }
    }

    /**
     * Performs a blocking connect on the google api client. Broadcasts the result back.
     */
    private void connect() {
        Log.d(TAG, "connect: GoogleApiClient connecting");
        ConnectionResult connectionResult = googleApiClient.blockingConnect(TIMEOUT_SECONDS, TimeUnit.SECONDS); //blocking call

        //if successful, we are done
        if (connectionResult.isSuccess()) {
            Log.d(TAG, "connect: GoogleApiClient connected");
            PlacesBroadcastReceiver.broadcastConnectSuccess(applicationContext);
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
     * Performs a blocking fetch. Fetches the place latlngs given the place ids. Broadcasts results back.
     * Releases resources when done.
      * @param placeIds
     */
    private void fetch(final ArrayList<String> placeIds) {
        placePendingResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeIds.get(0), placeIds.get(1));
        final PlaceBuffer result = placePendingResult.await(TIMEOUT_SECONDS, TimeUnit.SECONDS); //blocking call

        //process result
        if (result.getStatus().isSuccess()) {
            Log.d(TAG, "fetch: Success contacting getPlaceById API");
            //found places
            final Place userPlace = result.get(0);
            final Place friendPlace = result.get(1);
            //broadcast lat long back
            PlacesBroadcastReceiver.broadcastPlacesSuccess(applicationContext, userPlace.getLatLng(), friendPlace.getLatLng());
        }
        else {
            Log.d(TAG, "fetch: Error contacting getPlaceById API:" + result.getStatus().toString());
            //broadcast error
            PlacesBroadcastReceiver.broadcastPlacesFailure(applicationContext, result.getStatus().getStatusCode(), result.getStatus().getStatusMessage());
        }
        //clean up
        releaseResources(result);
    }

    /**
     * Performs a blocking autocomplete call. Fetches the autocomplete predictions given the query string
     * and calls the listener with the result.  No broadcast.
     * @param queryCallbackPair
     */
    private void autocomplete(final Pair<String, PlacesService.AutocompleteCallback> queryCallbackPair) {
        autocompletePendingResult = Places.GeoDataApi.getAutocompletePredictions(
                googleApiClient,
                queryCallbackPair.first,
                US_LAT_LNG_BOUNDS, //restrict results to a bounding rectangle that encapsulates the US
                null); //no autocomplete filter
        final AutocompletePredictionBuffer result = autocompletePendingResult.await(TIMEOUT_SECONDS, TimeUnit.SECONDS); //blocking call

        //process result
        final ArrayList<LocationSearchItem> items = buildSearchResultItems(result);

        //check if main handler has been initialized
        if (mainHandler == null) {
            mainHandler = new Handler(applicationContext.getMainLooper());
        }

        //post results back on the main thread
        if (result.getStatus().isSuccess()) {
            Log.d(TAG, "autocomplete: Success");
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    queryCallbackPair.second.onAutocompleteResult(items);
                }
            });
        }
        else {
            Log.d(TAG, String.format("autocomplete: Error contacting autocomplete API. StatusCode:%s, Message:%s", result.getStatus().getStatusCode(), result.getStatus().getStatusMessage()));
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    queryCallbackPair.second.onAutocompleteFailure(result.getStatus().getStatusCode(), result.getStatus().getStatusMessage());
                }
            });
        }
        //clean up
        releaseResources(result);
    }

    /**
     * Helper method. Clean up resources used by Places API
     * @param result
     */
    private void releaseResources(final PlaceBuffer result) {
        if (result != null) result.release();
        if (placePendingResult != null) {
            placePendingResult.cancel();
            placePendingResult = null;
        }
    }

    /**
     * Helper method Clean up resources used by Autocomplete API
     * @param result
     */
    private void releaseResources(final AutocompletePredictionBuffer result) {
        if (result != null) result.release();
        if (autocompletePendingResult != null) {
            autocompletePendingResult.cancel();
            autocompletePendingResult = null;
        }
    }

    /**
     * Release all member variables. Called by the service in onDestroy().
     */
    public void releaseAllResources() {
        applicationContext = null;
        mainHandler = null;

        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
            googleApiClient = null;
        }

        if (placePendingResult != null) {
            placePendingResult.cancel();
            placePendingResult = null;
        }

        if (autocompletePendingResult != null) {
            autocompletePendingResult.cancel();
            autocompletePendingResult = null;
        }
    }

    /**
     * Helper method that reads the search results from the predictions buffer into an arraylist of
     * search result items.
     * @param autocompletePredictions
     * @return
     */
    @NonNull
    private ArrayList<LocationSearchItem> buildSearchResultItems (@NonNull AutocompletePredictionBuffer autocompletePredictions) {
        ArrayList<LocationSearchItem> locationSearchItems = new ArrayList<>(autocompletePredictions.getCount());
        for (int i=0; i<autocompletePredictions.getCount(); i++) {
            locationSearchItems.add(new LocationSearchItem(autocompletePredictions.get(i).getDescription(), autocompletePredictions.get(i).getPlaceId()));
        }
        return locationSearchItems;
    }
}
