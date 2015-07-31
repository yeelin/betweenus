package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.SuggestionDetailFragment;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class SuggestionDetailActivity extends BaseActivity {
    //logcat
    private static final String TAG = SuggestionDetailActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_ID = SuggestionDetailActivity.class.getSimpleName() + ".id";

    /**
     * Builds the appropriate intent to start this activity
     * @param context
     * @param id
     * @return
     */
    public static Intent buildIntent(Context context, String id) {
        Intent intent = new Intent(context, SuggestionDetailActivity.class);
        //put extras
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }

    /**
     * Creates the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion_detail);
        //setup toolbar
        setupToolbar(R.id.place_toolbar, true);

        //read intent extras
        Intent intent = getIntent();
        String id = intent.getStringExtra(EXTRA_ID);

        //check if fragment exists, otherwise create it
        if (savedInstanceState == null) {
            Fragment suggestionDetailFragment = getSupportFragmentManager().findFragmentById(R.id.suggestionDetail_fragmentContainer);
            if (suggestionDetailFragment == null) {
                Log.d(TAG, "onCreate: Creating a new detail fragment");
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.suggestionDetail_fragmentContainer, SuggestionDetailFragment.newInstance(id))
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
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: Up button clicked");
                navigateUpToParentActivity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
