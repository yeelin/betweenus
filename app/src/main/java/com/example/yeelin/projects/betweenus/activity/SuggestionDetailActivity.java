package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.SuggestionDetailFragment;

/**
 * Created by ninjakiki on 7/15/15.
 */
public class SuggestionDetailActivity
        extends BaseActivity
        implements SuggestionDetailFragment.SuggestionDetailFragmentListener {
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
        setupToolbar(R.id.detail_toolbar, true);

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

    /**
     * SuggestionDetailFragment.SuggestionDetailFragmentListener implementation
     * Starts an activity to open the given url
     * @param url
     */
    @Override
    public void onOpenWebsite(@Nullable String url) {
        if (url == null) return;
        Log.d(TAG, "onOpenWebsite: Url:" + url);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * SuggestionDetailFragment.SuggestionDetailFragmentListener implementation
     * Starts an activity to open the dialer to the given phone number
     * @param phone
     */
    @Override
    public void onDialPhone(@Nullable String phone) {
        if (phone == null) return;
        Log.d(TAG, "onDialPhone: Phone:" + phone);

        //ACTION_DIAL does not call directly
        //ACTION_CALL will call directly and app has to declare permission in manifest
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
