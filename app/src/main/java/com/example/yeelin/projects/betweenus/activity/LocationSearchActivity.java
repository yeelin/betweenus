package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.LocationSearchFragment;
import com.example.yeelin.projects.betweenus.utils.LocationUtils;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class LocationSearchActivity
        extends BaseActivity
        implements LocationSearchFragment.LocationSearchFragmentListener {
    //logcat
    private static final String TAG = LocationSearchActivity.class.getCanonicalName();
    //intent extras
    private static final String EXTRA_USER_ID = LocationSearchActivity.class.getSimpleName() + ".userId";

    //result intent extras
    public static final String EXTRA_PLACE_ID = LocationSearchActivity.class.getSimpleName() + ".placeId";
    public static final String EXTRA_PLACE_DESC = LocationSearchActivity.class.getSimpleName() + ".placeDescription";

    /**
     * Builds the intent to start this activity
     * @param context
     * @param userId
     * @return
     */
    public static Intent buildIntent(Context context, int userId) {
        Intent intent = new Intent(context, LocationSearchActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }

    /**
     * Creates the activity and sets up the toolbar
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);

        //setup toolbar
        setupToolbar(R.id.search_toolbar, true);

        //read intent extras
        Intent intent = getIntent();
        int userId = intent.getIntExtra(EXTRA_USER_ID, LocationUtils.USER_LOCATION);

        //set the title (aka label in the manifest) for this activity
        setTitle(userId == LocationUtils.USER_LOCATION ? R.string.user_search_title : R.string.friend_search_title);

        //initialize the fragment if necessary
        if (savedInstanceState == null) {
            LocationSearchFragment locationSearchFragment = (LocationSearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragmentContainer);
            if (locationSearchFragment == null) {
                Log.d(TAG, "onCreate: Creating a new location search fragment");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.search_fragmentContainer, LocationSearchFragment.newInstance(userId))
                        .commit();
            }
        }
        else {
            Log.d(TAG, "onCreate: Saved instance state is not null");
        }
    }

    /**
     * Handle Up navigation
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: Up button clicked");
                navigateUpToParentActivity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * LocationSearchFragmentListener implementation
     * This callback happens when the user selects a suggestion in the location search listview.
     * Creates an intent, set it on the result and finishes the activity.
     * @param placeId
     * @param description
     */
    @Override
    public void onLocationSelected(String placeId, String description) {
        Log.d(TAG, "onLocationSelected");

        Intent intent = new Intent();
        intent.putExtra(EXTRA_PLACE_ID, placeId);
        intent.putExtra(EXTRA_PLACE_DESC, description);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /**
     * Log activation
     */
    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    /**
     * Log deactivation
     */
    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }
}
