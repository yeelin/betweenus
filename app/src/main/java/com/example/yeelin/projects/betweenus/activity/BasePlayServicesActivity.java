package com.example.yeelin.projects.betweenus.activity;

import android.content.Intent;
import android.content.IntentSender;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.BasePlayServicesFragment;
import com.example.yeelin.projects.betweenus.fragment.PlayServicesErrorDialogFragment;
import com.example.yeelin.projects.betweenus.receiver.PlacesBroadcastReceiver;
import com.example.yeelin.projects.betweenus.service.PlacesFetchService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ninjakiki on 7/15/15.
 * This is the base activity for all activities that wish to use google play services.
 */
public abstract class BasePlayServicesActivity
        extends BaseActivity
        implements BasePlayServicesFragment.BasePlayServicesFragmentListener,
        PlayServicesErrorDialogFragment.PlayServicesErrorDialogFragmentListener,
        PlacesBroadcastReceiver.PlacesBroadcastListener {
    //logcat
    private static final String TAG = BasePlayServicesActivity.class.getCanonicalName();

    //request code for play services resolution
    public static final int REQUEST_CODE_PLAY_SERVICES_RESOLUTION = 200;

    //tag for error dialog fragment
    private static final String TAG_GOOGLE_PLAY_ERROR_DIALOG = BasePlayServicesActivity.class.getSimpleName() + ".googlePlayServicesErrorDialog";

    //member variables
    private PlacesBroadcastReceiver placesBroadcastReceiver;

    /**
     * Start the PlacesFetchService. More specifically, we want to call connect on the google api client
     * to reduce latency later
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Starting PlacesFetchService");
        startService(PlacesFetchService.buildPlaceApiConnectIntent(this));
    }

    /** Create a broadcast receiver and register for broadcasts about place ids (success and failures)
    */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Registering for broadcasts");
        placesBroadcastReceiver = new PlacesBroadcastReceiver(this, this);
    }

    /**
     * Unregister for broadcasts about place ids (success and failures)
     */
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: Unregistering for broadcasts");
        placesBroadcastReceiver.unregister();
        super.onPause();
    }

    /**
     * Callback from Play Services dialogs.  Callback can originate from:
     * 1. connectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_PLAY_SERVICES_RESOLUTION) in BasePlayServicesFragment
     * 2. GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), requestCode) in PlayServicesErrorDialogFragment
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        // Handle the play services dialog result. This code is used whether the play
        // services dialog fragment was started by the fragment or the activity. Google
        // play services starts the activity such that only the activity is able to handle
        // the request code in onActivityResult.
        switch (requestCode) {
            case REQUEST_CODE_PLAY_SERVICES_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    // Situation resolved. Can now activate Play Services
                    Log.d(TAG, "onActivityResult: Google play services available");

                    // Notify all fragments that we may have a connection to google play services.
                    // try to limit it to one fragment per activity to keep things simple.
                    // notify the child activity (and in turn, the fragment) that play services are available and to retry the connection.
                    onPlayServicesAvailable();
                }
                else {
                    // Update failed. Do something reasonable.
                    Log.w(TAG, "onActivityResult: Result not ok");
                    noPlayServicesAvailable();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onPlacesSuccess(LatLng userLatLng, LatLng friendLatLng) {}

    @Override
    public void onPlacesFailure() {}

    @Override
    public void onConnectFailure(ConnectionResult connectionResult, int resolutionType) {
        Log.d(TAG, "onConnectFailure");
        if (connectionResult.hasResolution()) {
            // request the user take immediate action to resolve the error
            try {
                Log.d(TAG, "onConnectFailure: startResolutionForResult");
                //check for the result in onActivityResult() in the activity class
                connectionResult.startResolutionForResult(this, REQUEST_CODE_PLAY_SERVICES_RESOLUTION);

            }
            catch (IntentSender.SendIntentException e) {
                Log.d(TAG, "onConnectFailure: Exception. Starting service again");
                //there was an error with the resolution. try to reconnect.
                startService(PlacesFetchService.buildPlaceApiConnectIntent(this));
            }
        }
        else {
            Log.d(TAG, "onConnectFailure: No resolution so showing error dialog");
            // Show error dialog using GooglePlayServicesUtil.getErrorDialog()
            showPlayServicesErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Helper method. Used when play services become available.  Attempt to reconnect
     */
    protected void onPlayServicesAvailable() {
        Log.d(TAG, "onPlayServicesAvailable. Starting service again");
        //attempt to reconnect
        startService(PlacesFetchService.buildPlaceApiConnectIntent(this));
    }

    /**
     * Helper method. Used when no play services are available.
     */
    protected void noPlayServicesAvailable() {
        Log.w(TAG, "noPlayServicesAvailable");
        //notify the user
        Toast.makeText(this, R.string.google_play_services_error, Toast.LENGTH_LONG).show();
    }

    /**
     * Callback from BasePlayServicesFragmentListener
     * @param errorCode
     */
    @Override
    public void showPlayServicesErrorDialog(int errorCode) {
        Log.d(TAG, "showPlayServicesErrorDialog: Error code:" + errorCode);
        DialogFragment errorDialogFragment = PlayServicesErrorDialogFragment.newInstance(errorCode, REQUEST_CODE_PLAY_SERVICES_RESOLUTION);
        errorDialogFragment.show(getSupportFragmentManager(), TAG_GOOGLE_PLAY_ERROR_DIALOG);
    }

    /**
     * Callback from PlayServicesErrorDialogFragment.PlayServicesErrorDialogFragmentListener
     *
     * This callback happens when the user cancels the PlayServicesErrorDialogFragment without
     * resolving the error.
     */
    @Override
    public void onPlayServicesErrorDialogCancelled() {
        Log.d(TAG, "onPlayServicesErrorDialogCancelled");
        noPlayServicesAvailable();
    }
}