package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.PlaceFragment;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class PlaceActivity extends BaseActivity {
    //logcat
    private static final String TAG = PlaceActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_ID = PlaceActivity.class.getSimpleName() + ".placeId";
    private static final String EXTRA_NAME = PlaceActivity.class.getSimpleName() + ".placeName";

    /**
     * Builds the appropriate intent to start this activity
     * @param context
     * @param placeId
     * @param name
     * @return
     */
    public static Intent buildIntent(Context context, long placeId, String name) {
        Intent intent = new Intent(context, PlaceActivity.class);

        //put extras
        intent.putExtra(EXTRA_ID, placeId);
        intent.putExtra(EXTRA_NAME, name);

        return intent;
    }

    /**
     * Creates the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        //setup toolbar
        setupToolbar(R.id.place_toolbar, true);

        //read intent extras
        Intent intent = getIntent();
        long placeId = intent.getLongExtra(EXTRA_ID, 0);
        String placeName = intent.getStringExtra(EXTRA_NAME);

        //check if fragment exists, otherwise create it
        if (savedInstanceState == null) {
            Fragment placeFragment = getSupportFragmentManager().findFragmentById(R.id.place_fragmentContainer);
            if (placeFragment == null) {
                Log.d(TAG, "onCreate: Creating a new Place Fragment");
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.place_fragmentContainer, PlaceFragment.newInstance(placeId, placeName))
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
