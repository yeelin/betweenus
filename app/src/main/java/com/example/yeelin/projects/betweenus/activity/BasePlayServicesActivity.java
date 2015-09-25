package com.example.yeelin.projects.betweenus.activity;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.PlayServicesErrorDialogFragment;
import com.example.yeelin.projects.betweenus.service.PlacesService;

/**
 * Created by ninjakiki on 7/15/15.
 * This is the base activity for all activities that wish to use google play services.
 */
public abstract class BasePlayServicesActivity
        extends BaseActivity
        implements PlayServicesErrorDialogFragment.PlayServicesErrorDialogFragmentListener {
    //logcat
    private static final String TAG = BasePlayServicesActivity.class.getCanonicalName();

    //request code for play services resolution
    public static final int REQUEST_CODE_PLAY_SERVICES_RESOLUTION = 200;

    //tag for error dialog fragment
    private static final String TAG_GOOGLE_PLAY_ERROR_DIALOG = BasePlayServicesActivity.class.getSimpleName() + ".googlePlayServicesErrorDialog";

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
                    Log.d(TAG, "onActivityResult: Starting PlacesService again");
                    //attempt to reconnect
                    startService(PlacesService.buildApiConnectIntent(this));
                }
                else {
                    // Update failed. Do something reasonable.
                    Log.w(TAG, "onActivityResult: Google play services unavailable. Informing user via snackbar");

                    //create a snackbar and inform the user
                    final View rootView = findViewById(R.id.root_layout);
                    if (rootView != null) {
                        Snackbar.make(rootView, R.string.google_play_services_error, Snackbar.LENGTH_LONG);
                    }
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Helper method that shows the google play services error dialog.
     * @param errorCode
     */
    protected void showPlayServicesErrorDialog(int errorCode) {
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
        Log.d(TAG, "onPlayServicesErrorDialogCancelled: Informing user via snackbar");

        //create a snackbar and inform the user
        final View rootView = findViewById(R.id.root_layout);
        if (rootView != null) {
            Snackbar.make(rootView, R.string.google_play_services_error, Snackbar.LENGTH_LONG);
        }
    }
}