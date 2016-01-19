package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.fragment.SuggestionDetailFragment;
import com.example.yeelin.projects.betweenus.utils.PreferenceUtils;
import com.google.android.gms.maps.model.LatLng;

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
    private static final String EXTRA_LATLNG = SuggestionDetailActivity.class.getSimpleName() + ".latLng";
    private static final String EXTRA_USER_LATLNG = SuggestionDetailActivity.class.getSimpleName() + ".userLatLng";
    private static final String EXTRA_FRIEND_LATLNG = SuggestionDetailActivity.class.getSimpleName() + ".friendLatLng";
    private static final String EXTRA_MID_LATLNG = SuggestionDetailActivity.class.getSimpleName() + ".midLatLng";
    public static final String EXTRA_POSITION = SuggestionDetailActivity.class.getSimpleName() + ".position";
    public static final String EXTRA_TOGGLE_STATE = SuggestionDetailActivity.class.getSimpleName() + ".toggleState";
    private static final String EXTRA_RATING = SuggestionDetailActivity.class.getSimpleName() + ".rating";
    private static final String EXTRA_LIKES = SuggestionDetailActivity.class.getSimpleName() + ".likes";
    private static final String EXTRA_NORMALIZED_LIKES = SuggestionDetailActivity.class.getSimpleName() + ".normalizedLikes";

    //member variables
    private String id;
    private int position = 0;
    private boolean toggleState;

    /**
     * Builds the appropriate intent to start this activity
     * @param context
     * @param id
     * @param name
     * @param latLng
     * @param position
     * @param toggleState
     * @param rating
     * @param likes
     * @param normalizedLikes
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng midpoint between userLatLng and friendLatLng
     * @return
     */
    public static Intent buildIntent(Context context, String id, String name, LatLng latLng,
                                     int position, boolean toggleState, double rating, int likes, double normalizedLikes,
                                     LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        Intent intent = new Intent(context, SuggestionDetailActivity.class);
        //put extras
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_LATLNG, latLng);

        intent.putExtra(EXTRA_POSITION, position);
        intent.putExtra(EXTRA_TOGGLE_STATE, toggleState);
        intent.putExtra(EXTRA_RATING, rating);
        intent.putExtra(EXTRA_LIKES, likes);
        intent.putExtra(EXTRA_NORMALIZED_LIKES, normalizedLikes);

        intent.putExtra(EXTRA_USER_LATLNG, userLatLng);
        intent.putExtra(EXTRA_FRIEND_LATLNG, friendLatLng);
        intent.putExtra(EXTRA_MID_LATLNG, midLatLng);

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
        position = intent.getIntExtra(EXTRA_POSITION, position);
        toggleState = intent.getBooleanExtra(EXTRA_TOGGLE_STATE, false);

        String name = intent.getStringExtra(EXTRA_NAME);
        LatLng latLng = intent.getParcelableExtra(EXTRA_LATLNG);
        double rating = intent.getDoubleExtra(EXTRA_RATING, LocalConstants.NO_DATA_DOUBLE);
        int likes = intent.getIntExtra(EXTRA_LIKES, LocalConstants.NO_DATA_INTEGER);
        double normalizedLikes = intent.getDoubleExtra(EXTRA_NORMALIZED_LIKES, LocalConstants.NO_DATA_DOUBLE);

        LatLng userLatLng = intent.getParcelableExtra(EXTRA_USER_LATLNG);
        LatLng friendLatLng = intent.getParcelableExtra(EXTRA_FRIEND_LATLNG);
        LatLng midLatLng = intent.getParcelableExtra(EXTRA_MID_LATLNG);

        //check if fragment exists, otherwise create it
        if (savedInstanceState == null) {
            Fragment suggestionDetailFragment = getSupportFragmentManager().findFragmentById(R.id.suggestionDetail_fragmentContainer);
            if (suggestionDetailFragment == null) {
                Log.d(TAG, "onCreate: Creating a new detail fragment");
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.suggestionDetail_fragmentContainer, SuggestionDetailFragment.newInstance(
                                id, name, latLng,
                                position, toggleState, rating, likes, normalizedLikes,
                                userLatLng, friendLatLng, midLatLng,
                                PreferenceUtils.getPreferredDataSource(this), PreferenceUtils.useMetric(this)))
                        .commit();
            }
        }
        else {
            Log.d(TAG, "onCreate: Saved instance state is not null");
        }
    }

    /**
     * Inflate the menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_suggestion_detail, menu);
        return super.onCreateOptionsMenu(menu);
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
                Log.d(TAG, String.format("onOptionsItemSelected: Up button clicked. Id:%s, Selected:%s", id, toggleState));

                //set the result along with the intent, and finish
                setResult(Activity.RESULT_OK, buildResultIntent());
                finish();
                return true;

            case R.id.action_select:
                Log.d(TAG, "onOptionsItemSelected: Invite button clicked");
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
        resultIntent.putExtra(EXTRA_POSITION, position);
        resultIntent.putExtra(EXTRA_TOGGLE_STATE, toggleState);
        return resultIntent;
    }

    @Override
    public void onOpenPhotos(int position) {
        Log.d(TAG, "onOpenPhotos: Not implemented");
    }

    @Override
    public void onOpenMap(int position, boolean toggleState) {
        Log.d(TAG, "onOpenMap: Not implemented");
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
    public void onToggle(String id, int position, boolean toggleState) {
        this.toggleState = toggleState;
    }
}
