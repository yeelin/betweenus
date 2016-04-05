package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.analytics.EventConstants;
import com.example.yeelin.projects.betweenus.fragment.ItineraryFragment;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by ninjakiki on 3/30/16.
 */
public class ItineraryActivity
        extends BaseActivity
        implements ItineraryFragment.ItineraryFragmentListener {
    private static final String TAG = ItineraryActivity.class.getCanonicalName();

    public static Intent buildIntent(Context context) {
        Intent intent = new Intent(context, ItineraryActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setup view and toolbar
        setContentView(R.layout.activity_itinerary);
        setupToolbar(R.id.itinerary_toolbar, true);

        //TODO: read intent if any
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);

        //log user initiated invite process
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        Bundle parameters = new Bundle();
//        parameters.putString(EventConstants.EVENT_PARAM_INITIATED_VIEW, originatingView);
//        parameters.putInt(EventConstants.EVENT_PARAM_NUM_PLACES_SELECTED, selectedItems.size());
//        logger.logEvent(EventConstants.EVENT_NAME_INITIATED_INVITE, parameters);

    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onItineraryView(int itineraryId) {
        Log.d(TAG, "onItineraryView");
    }

    @Override
    public void onItineraryReuse(int itineraryId) {
        Log.d(TAG, "onItineraryReuse");
    }
}
