package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.SuggestionsFragment;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/13/15.
 */
public class SuggestionsActivity
        extends BaseActivity
        implements SuggestionsFragment.SuggestionsFragmentListener {
    //logcat
    private static final String TAG = SuggestionsActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_SEARCH_TERM = SuggestionsActivity.class.getSimpleName() + ".searchTerm";
    private static final String EXTRA_USER_LOCATION = SuggestionsActivity.class.getSimpleName() + ".userLocation";
    private static final String EXTRA_FRIEND_LOCATION = SuggestionsActivity.class.getSimpleName() + ".friendLocation";

    /**
     * Builds the appropriate intent to start this activity.
     * @param context
     * @param userLocation
     * @param friendLocation
     * @return
     */
    public static Intent buildIntent(Context context, String searchTerm,
                                     Location userLocation, Location friendLocation) {
        Intent intent = new Intent(context, SuggestionsActivity.class);

        //put extras
        intent.putExtra(EXTRA_SEARCH_TERM, searchTerm);
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
        setContentView(R.layout.activity_suggestions);
        //setup toolbar
        setupToolbar(R.id.suggestions_toolbar, true);

        //read extras from intent
        Intent intent = getIntent();
        String searchTerm = intent.getStringExtra(EXTRA_SEARCH_TERM);
        Location userLocation = intent.getParcelableExtra(EXTRA_USER_LOCATION);
        Location friendLocation = intent.getParcelableExtra(EXTRA_FRIEND_LOCATION);

        //check if the fragment exists, otherwise create it
        if (savedInstanceState == null) {
            Fragment suggestedPlacesFragment = getSupportFragmentManager().findFragmentById(R.id.suggestions_fragmentContainer);
            if (suggestedPlacesFragment == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.suggestions_fragmentContainer, SuggestionsFragment.newInstance(searchTerm, userLocation, friendLocation))
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

    /**
     * SuggestionsFragment.SuggestionsFragmentListener
     * @param selectedItemIds
     */
    @Override
    public void onSelectionComplete(ArrayList<String> selectedItemIds) {
        Log.d(TAG, "onSelectionComplete: Selected Item Ids:" + selectedItemIds);

        //TODO: Start the invite activity
    }
}
