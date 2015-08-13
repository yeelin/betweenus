package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
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
    public static final String EXTRA_ID = SuggestionDetailActivity.class.getSimpleName() + ".id";
    private static final String EXTRA_NAME = SuggestionDetailActivity.class.getSimpleName() + ".name";
    public static final String EXTRA_IS_SELECTED = SuggestionDetailActivity.class.getSimpleName() + ".isSelected";

    //member variables
    private String id;
    private boolean isSelected;

    /**
     * Builds the appropriate intent to start this activity
     * @param context
     * @param id
     * @return
     */
    public static Intent buildIntent(Context context, String id, String name, boolean isSelected) {
        Intent intent = new Intent(context, SuggestionDetailActivity.class);
        //put extras
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_IS_SELECTED, isSelected);

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
        id = intent.getStringExtra(EXTRA_ID);
        isSelected = intent.getBooleanExtra(EXTRA_IS_SELECTED, false);
        String name = intent.getStringExtra(EXTRA_NAME);

        //check if fragment exists, otherwise create it
        if (savedInstanceState == null) {
            Fragment suggestionDetailFragment = getSupportFragmentManager().findFragmentById(R.id.suggestionDetail_fragmentContainer);
            if (suggestionDetailFragment == null) {
                Log.d(TAG, "onCreate: Creating a new detail fragment");
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.suggestionDetail_fragmentContainer, SuggestionDetailFragment.newInstance(id, name, isSelected))
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
                //handle up navigation by setting a result intent so that onActivityResult in SuggestionsActivity
                //receives a non-null intent
                Log.d(TAG, String.format("onOptionsItemSelected: Up button clicked. Id:%s, Selected:%s", id, isSelected));

                //set the result along with the intent, and finish
                setResult(Activity.RESULT_OK, buildResultIntent());
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Handles back button press by setting a result intent so that onActivityResult in SuggestionsActivity
     * receives a non-null intent
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");

        //set the result along with the intent
        setResult(Activity.RESULT_OK, buildResultIntent());
        super.onBackPressed(); //this will call finish
    }

    /**
     * Helper method that builds a result intent with the resulting state of the selection
     * so that it can be returned to the parent activity that started us.
     * @return
     */
    private Intent buildResultIntent() {
        //create result intent and put extras
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_ID, id);
        resultIntent.putExtra(EXTRA_IS_SELECTED, isSelected);
        return resultIntent;
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

    /**
     * SuggestionDetailFragment.SuggestionDetailFragmentListener implementation
     * Callback from the detail fragment to toggle the selection
     */
    @Override
    public void onSelectionToggle() {
        isSelected = !isSelected;
    }
}
