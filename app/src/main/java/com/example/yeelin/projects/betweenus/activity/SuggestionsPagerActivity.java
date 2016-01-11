package com.example.yeelin.projects.betweenus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.analytics.EventConstants;
import com.example.yeelin.projects.betweenus.data.generic.model.SimplifiedBusiness;
import com.example.yeelin.projects.betweenus.adapter.SuggestionsStatePagerAdapter;
import com.example.yeelin.projects.betweenus.fragment.SuggestionDetailFragment;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 9/21/15.
 */
public class SuggestionsPagerActivity
        extends BaseActivity
        implements ViewPager.OnPageChangeListener, //tells you about view pager events
        SuggestionDetailFragment.SuggestionDetailFragmentListener { //tells you about events from fragment
    //logcat
    private static final String TAG = SuggestionsPagerActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_PAGER_POSITION = SuggestionsPagerActivity.class.getSimpleName() + ".pagerPosition";
    private static final String EXTRA_PAGER_LIST = SuggestionsPagerActivity.class.getSimpleName() + ".pagerList";
    public static final String EXTRA_SELECTED_IDS = SuggestionsPagerActivity.class.getSimpleName() + ".selectedIds";
    public static final String EXTRA_SELECTED_POSITIONS = SuggestionsPagerActivity.class.getSimpleName() + ".selectedPositions";
    private static final String EXTRA_USER_LATLNG = SuggestionsPagerActivity.class.getSimpleName() + ".userLatLng";
    private static final String EXTRA_FRIEND_LATLNG = SuggestionsPagerActivity.class.getSimpleName() + ".friendLatLng";
    private static final String EXTRA_MID_LATLNG = SuggestionsPagerActivity.class.getSimpleName() + ".midLatLng";

    //saved instance state
    private static final String STATE_PAGER_POSITION = SuggestionsPagerActivity.class.getSimpleName() + ".pagerPosition";

    //member variables
    private ViewPager viewPager;

    private int viewPagerPosition = 0;
    private ArrayList<SimplifiedBusiness> simplifiedBusinesses;
    private ArrayMap<String, Integer> selectedIdsMap;
    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    /**
     * Builds the appropriate intent to start this activity
     * @param context
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng midpoint between userLatLng and friendLatLng
     * @return
     */
    public static Intent buildIntent(Context context, int position,
                                     ArrayList<SimplifiedBusiness> simplifiedBusinesses,
                                     ArrayList<String> selectedIdsList, ArrayList<Integer> selectedPositionsList,
                                     LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        Intent intent = new Intent(context, SuggestionsPagerActivity.class);
        //put extras
        intent.putExtra(EXTRA_PAGER_POSITION, position);
        intent.putExtra(EXTRA_PAGER_LIST, simplifiedBusinesses);
        intent.putExtra(EXTRA_SELECTED_IDS, selectedIdsList);
        intent.putExtra(EXTRA_SELECTED_POSITIONS, selectedPositionsList);

        intent.putExtra(EXTRA_USER_LATLNG, userLatLng);
        intent.putExtra(EXTRA_FRIEND_LATLNG, friendLatLng);
        intent.putExtra(EXTRA_MID_LATLNG, midLatLng);

        return intent;
    }

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions_pager);
        //setup toolbar
        setupToolbar(R.id.pager_toolbar, true);

        //read intent extras
        Intent intent = getIntent();
        viewPagerPosition = intent.getIntExtra(EXTRA_PAGER_POSITION, viewPagerPosition);
        simplifiedBusinesses = intent.getParcelableArrayListExtra(EXTRA_PAGER_LIST);

        ArrayList<String> selectedIdsList = intent.getStringArrayListExtra(EXTRA_SELECTED_IDS);
        ArrayList<Integer> selectedPositionsList = intent.getIntegerArrayListExtra(EXTRA_SELECTED_POSITIONS);
        selectedIdsMap = new ArrayMap<>(selectedIdsList.size());
        for (int i=0; i<selectedIdsList.size(); i++) {
            selectedIdsMap.put(selectedIdsList.get(i), selectedPositionsList.get(i));
        }

        userLatLng = intent.getParcelableExtra(EXTRA_USER_LATLNG);
        friendLatLng = intent.getParcelableExtra(EXTRA_FRIEND_LATLNG);
        midLatLng = intent.getParcelableExtra(EXTRA_MID_LATLNG);

        //read saved instance state
        if (savedInstanceState != null) {
            viewPagerPosition = savedInstanceState.getInt(STATE_PAGER_POSITION, viewPagerPosition);
        }

        //set up view pager
        viewPager = (ViewPager) findViewById(R.id.suggestions_viewPager);
        SuggestionsStatePagerAdapter pagerAdapter = new SuggestionsStatePagerAdapter(getSupportFragmentManager(),
                simplifiedBusinesses, selectedIdsMap,
                userLatLng, friendLatLng, midLatLng);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(viewPagerPosition);

        //set toolbar title
        updateToolbarTitle(viewPagerPosition);
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
                Log.d(TAG, String.format("onOptionsItemSelected: Up button clicked."));

                //set the result along with the intent, and finish
                setResult(Activity.RESULT_OK, buildResultIntent());
                finish();
                return true;

            case R.id.action_select:
                Log.d(TAG, "onOptionsItemSelected: Invite button clicked");
                if (selectedIdsMap.size() > 0) {
                    //start invite activity
                    startActivity(InvitationActivity.buildIntent(this, buildSelectedItemsList(), EventConstants.EVENT_PARAM_VIEW_PAGER));
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Helper method to build the selected items array list for marshalling across to the
     * invitation activity.
     * @return
     */
    private ArrayList<SimplifiedBusiness> buildSelectedItemsList() {
        Log.d(TAG, "buildSelectedItemsList");
        ArrayList<SimplifiedBusiness> selectedItems = new ArrayList<>(selectedIdsMap.size());

        for (int i=0; i<simplifiedBusinesses.size(); i++) {
            SimplifiedBusiness business = simplifiedBusinesses.get(i);
            if (selectedIdsMap.containsKey(business.getId())) {
                selectedItems.add(business);
            }
        }
        return selectedItems;
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

    /**
     * Save the pager position in case of rotation or backgrounding.
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_PAGER_POSITION, viewPagerPosition);
    }

    /**
     * Restore pager position in case of rotation or backgrounding
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            viewPagerPosition = savedInstanceState.getInt(STATE_PAGER_POSITION, viewPagerPosition);
        }
    }

    /**
     * Remove a listener that was previously added via addOnPageChangeListener(OnPageChangeListener)
     */
    @Override
    protected void onDestroy() {
        viewPager.removeOnPageChangeListener(this);
        super.onDestroy();
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
        resultIntent.putStringArrayListExtra(EXTRA_SELECTED_IDS, new ArrayList<>(selectedIdsMap.keySet()));
        resultIntent.putIntegerArrayListExtra(EXTRA_SELECTED_POSITIONS, new ArrayList<>(selectedIdsMap.values()));
        return resultIntent;
    }

    /**
     * Save the current pager position when a new page is selected
     * @param position
     */
    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected: Position:" + position);

        //update the current view pager position
        viewPagerPosition = position;

        //set the toolbar title
        updateToolbarTitle(position);
    }

    /**
     * Updates the toolbar title using the current page number (zero-index + 1) and the count of places
     * that we have fetched.
     * There are 2 formats for titles:
     * 1) %d of %d+ : Example: 3 of 25+.  This is used if we have loaded 25 places and we know there
     *    are more to be fetched if requested. (TODO)
     * 2) %d of %d : Example: 3 of 25.  This is used if we have loaded 25 places and we know the
     *    server has no more data.
     * @param position
     */
    private void updateToolbarTitle(int position) {
        //no need to update title if the toolbar is null for some reason
        if (getSupportActionBar() == null) return;

        //figure out if we should use format 1 or 2 for the title and format the title accordingly
        final SuggestionsStatePagerAdapter pagerAdapter = (SuggestionsStatePagerAdapter) viewPager.getAdapter();
        final int count = pagerAdapter.getCount();

        //set the title
        getSupportActionBar().setTitle(
                getString(R.string.title_detail_parameterized,
                        String.valueOf(1+position), //add 1 since users are not used to seeing zero-based indexing
                        String.valueOf(count)));
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageScrollStateChanged(int state) {}

    /**
     * SuggestionDetailFragment.SuggestionDetailFragmentListener implementation
     * Starts the photo pager
     * @param position
     */
    @Override
    public void onOpenPhotos(int position) {
        Log.d(TAG, "onOpenPhotos: Position:" + position);
        final SimplifiedBusiness simplifiedBusiness = simplifiedBusinesses.get(position);
        startActivity(PhotosPagerActivity.buildIntent(this, simplifiedBusiness.getId(), simplifiedBusiness.getProfilePictureUrl()));
    }

    /**
     * SuggestionDetailFragment.SuggestionDetailFragmentListener implementation
     * Starts the interactive map
     * @param position
     * @param toggleState
     */
    @Override
    public void onOpenMap(int position, boolean toggleState) {
        final SimplifiedBusiness simplifiedBusiness = simplifiedBusinesses.get(position);
        startActivity(MapActivity.buildIntent(this, simplifiedBusiness.getId(), simplifiedBusiness.getName(), simplifiedBusiness.getLatLng(), toggleState,
                simplifiedBusiness.getRating(), simplifiedBusiness.getRatingImageUrl(), simplifiedBusiness.getReviews(),
                simplifiedBusiness.getLikes(), simplifiedBusiness.getNormalizedLikes(), simplifiedBusiness.getCheckins()));

        //log user switch to detail map view
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        Bundle parameters = new Bundle();
        parameters.putString(EventConstants.EVENT_PARAM_SOURCE_VIEW, EventConstants.EVENT_PARAM_VIEW_PAGER);
        parameters.putString(EventConstants.EVENT_PARAM_DESTINATION_VIEW, EventConstants.EVENT_PARAM_VIEW_DETAIL_MAP);
        logger.logEvent(EventConstants.EVENT_NAME_SWITCHED_VIEWS, parameters);
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
     * Flips the toggle state of the item in the selectedIdsMap.
     * If the item is in the map, it is removed.
     * If the item is not in the map, it is added.
     * @param id business id
     * @param position position of the item in the pager
     * @param toggleState resulting toggle state (true means selected, false means not selected)
     */
    @Override
    public void onToggle(String id, int position, boolean toggleState) {
        Log.d(TAG, String.format("onToggle: Id:%s, Position:%d, ToggleState:%s", id, position, toggleState));
        if (selectedIdsMap.containsKey(id) && !toggleState) {
            //if the item is in the map AND resulting toggle state is false (not selected), we remove it
            Log.d(TAG, "onToggle: Item is in the map, so removing:" + id);
            selectedIdsMap.remove(id);
        }
        else if (!selectedIdsMap.containsKey(id) && toggleState) {
            //if the item is not in the map AND resulting toggle state is true, we add it
            Log.d(TAG, "onToggle: Item is not in the map, so adding:" + id);
            selectedIdsMap.put(id, position);
        }
    }
}
