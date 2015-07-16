package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.SuggestedPlacesFragment;

/**
 * Created by ninjakiki on 7/13/15.
 */
public class SuggestedPlacesActivity
        extends BaseActivity {
    //logcat
    private static final String TAG = SuggestedPlacesActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_USER_LOCATION = SuggestedPlacesActivity.class.getSimpleName() + ".userLocation";
    private static final String EXTRA_FRIEND_LOCATION = SuggestedPlacesActivity.class.getSimpleName() + ".friendLocation";

    /**
     * Builds the appropriate intent to start this activity.
     * @param context
     * @param userLocation
     * @param friendLocation
     * @return
     */
    public static Intent buildIntent(Context context, String userLocation, String friendLocation) {
        Intent intent = new Intent(context, SuggestedPlacesActivity.class);

        //put extras
        intent.putExtra(EXTRA_USER_LOCATION, userLocation);
        intent.putExtra(EXTRA_FRIEND_LOCATION, friendLocation);

        return intent;
    }

    /**
     * Creates the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_places);
        //setup toolbar
        setupToolbar(R.id.suggestedPlaces_toolbar, true);

        //read extras from intent
        Intent intent = getIntent();
        String userLocation = intent.getStringExtra(EXTRA_USER_LOCATION);
        String friendLocation = intent.getStringExtra(EXTRA_FRIEND_LOCATION);

        //check if the fragment exists, otherwise create it
        if (savedInstanceState == null) {
            Fragment suggestedPlacesFragment = getSupportFragmentManager().findFragmentById(R.id.suggestedPlaces_fragmentContainer);
            if (suggestedPlacesFragment == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.suggestedPlaces_fragmentContainer, SuggestedPlacesFragment.newInstance())
                        .commit();
            }
        }
        else {
            Log.d(TAG, "onCreate: Saved instance state is not null");
        }
    }

    /**
     * Handles user selection of menu options
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Log.d(TAG, "onOptionsItemSelected: Up button clicked");
                navigateUpToParentActivity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
