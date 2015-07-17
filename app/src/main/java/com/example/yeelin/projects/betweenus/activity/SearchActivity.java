package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.SearchFragment;

import java.util.List;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class SearchActivity
        extends BasePlayServicesActivity
        implements SearchFragment.SearchFragmentListener {
    //logcat
    private static final String TAG = SearchActivity.class.getCanonicalName();
    //intent extras
    private static final String EXTRA_USER_ID = SearchActivity.class.getSimpleName() + ".userId";
    public static final int USER = 0;
    public static final int FRIEND = 1;

    //result intent extras
    public static final String EXTRA_LOCATION_NAME = SearchActivity.class.getSimpleName() + ".locationName";
    public static final String EXTRA_LOCATION_LATITUDE = SearchActivity.class.getSimpleName() + ".locationLatitude";
    public static final String EXTRA_LOCATION_LONGITUDE = SearchActivity.class.getSimpleName() + ".locationLongitude";

    /**
     * Builds the intent to start this activity
     * @param context
     * @param userId
     * @return
     */
    public static Intent buildIntent(Context context, int userId) {
        Intent intent = new Intent(context, SearchActivity.class);
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
        setContentView(R.layout.activity_search);

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

        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);
        if (searchFragment != null) {
            searchFragment.onPlayServicesAvailable();
        }
    }

    /**
     * SearchFragmentListener implementation
     * This callback happens when the user selects a place in the places search listview
     * @param name
     * @param latitude
     * @param longitude
     * @param placeTypes
     */
    @Override
    public void onPlaceSelected(String name, double latitude, double longitude, List<Integer> placeTypes) {
        Log.d(TAG, "onPlaceSelected:");
        //navigateUpToParentActivity(this);

        Intent intent = new Intent();
        intent.putExtra(EXTRA_LOCATION_NAME, name);
        intent.putExtra(EXTRA_LOCATION_LATITUDE, latitude);
        intent.putExtra(EXTRA_LOCATION_LONGITUDE, longitude);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
