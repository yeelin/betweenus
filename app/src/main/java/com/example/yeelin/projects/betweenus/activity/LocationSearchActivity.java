package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.LocationSearchFragment;

import java.util.List;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class LocationSearchActivity
        extends BasePlayServicesActivity
        implements LocationSearchFragment.LocationSearchFragmentListener {
    //logcat
    private static final String TAG = LocationSearchActivity.class.getCanonicalName();
    //intent extras
    private static final String EXTRA_USER_ID = LocationSearchActivity.class.getSimpleName() + ".userId";

    //result intent extras
    public static final String EXTRA_LOCATION = LocationSearchActivity.class.getSimpleName() + ".location";

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
     * BasePlayServicesActivity required override
     * Helper method. Used when no play services are available. Shows a toast and then navigate back to parent activity
     * since search isn't going to work.
     */
    @Override
    protected void noPlayServicesAvailable() {
        Log.w(TAG, "noPlayServicesAvailable");

        //notify the user
        Toast.makeText(this, R.string.google_play_services_error, Toast.LENGTH_LONG).show();

        //not much else to do here since search won't work, so navigate back to parent seems logical
        navigateUpToParentActivity(this);
    }

    /**
     * BasePlayServicesActivity override
     * Helper method. Used when play services become available. Notify search fragment that play services are available
     * and to retry connection.
     */
    @Override
    protected void onPlayServicesAvailable() {
        Log.d(TAG, "onPlayServicesAvailable");

        LocationSearchFragment locationSearchFragment = (LocationSearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);
        if (locationSearchFragment != null) {
            locationSearchFragment.onPlayServicesAvailable();
        }
    }

    /**
     * LocationSearchFragmentListener implementation
     * This callback happens when the user selects a suggestion in the location search listview
     * @param location
     */
    @Override
    public void onLocationSelected(Location location) {
        Log.d(TAG, "onLocationSelected");

        Intent intent = new Intent();
        intent.putExtra(EXTRA_LOCATION, location);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
