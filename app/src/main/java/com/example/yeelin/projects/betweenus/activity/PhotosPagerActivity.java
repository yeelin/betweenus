package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.adapter.PhotosStatePagerAdapter;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalPhoto;
import com.example.yeelin.projects.betweenus.data.LocalPhotosResult;
import com.example.yeelin.projects.betweenus.data.fb.query.FbConstants;
import com.example.yeelin.projects.betweenus.loader.LoaderId;
import com.example.yeelin.projects.betweenus.loader.PhotosLoaderCallbacks;
import com.example.yeelin.projects.betweenus.loader.callback.PhotosLoaderListener;
import com.facebook.AccessToken;


/**
 * Created by ninjakiki on 11/12/15.
 */
public class PhotosPagerActivity
        extends BaseActivity
        implements ViewPager.OnPageChangeListener,
        PhotosLoaderListener {
    //logcat
    private static final String TAG = PhotosPagerActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_ID = PhotosPagerActivity.class.getSimpleName() + ".id";
    private static final String EXTRA_PROFILE_PIC_URL = PhotosPagerActivity.class.getSimpleName() + ".profilePictureUrl";

    //saved instance state
    private static final String STATE_PAGER_POSITION = PhotosPagerActivity.class.getSimpleName() + ".pagerPosition";

    //member variables
    private ViewPager viewPager;
    private int viewPagerPosition = 0;
    private String id;
    private String profilePictureUrl;

    private LocalPhotosResult localPhotosResult;
    private boolean hasMoreData;

    /**
     * Builds intent to start this activity
     * @param context
     * @param id
     * @param profilePictureUrl
     * @return
     */
    public static Intent buildIntent(Context context, String id, String profilePictureUrl) {
        Intent intent = new Intent(context, PhotosPagerActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_PROFILE_PIC_URL, profilePictureUrl);
        return intent;
    }

    /**
     * Set up the pager activity by doing the following:
     * 1. Set up the view pager
     * 2. Fetch the photo urls for the fragments in the pager
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate:");
        super.onCreate(savedInstanceState);

        //setup view and toolbar
        setContentView(R.layout.activity_photos_pager);
        setupToolbar(R.id.picture_pager_toolbar, true);

        //read intent extras
        Intent intent = getIntent();
        id = intent.getStringExtra(EXTRA_ID);
        profilePictureUrl = intent.getStringExtra(EXTRA_PROFILE_PIC_URL);

        //set up pager adapter
        final LocalPhoto localPhoto = new LocalPhoto() {
            @Override
            public String getSourceUrl() {
                return profilePictureUrl;
            }

            @Override
            public String getCaption() {
                return null;
            }
        };

        PhotosStatePagerAdapter pagerAdapter = new PhotosStatePagerAdapter(
                getSupportFragmentManager(),
                new LocalPhoto[] {localPhoto}); //the current profile picture is the first photo in the view pager

        //set up view pager
        viewPager = (ViewPager) findViewById(R.id.picture_viewPager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(viewPagerPosition);

        //fetch photo urls for the given place id.
        fetchPlacePhotos(null); //since this is the initial call, afterId is null
    }

    /**
     * Fetch photo urls for this place/detail page using a loader
     * @param afterId the end id of the page of data that has been returned, if any
     */
    private void fetchPlacePhotos(@Nullable String afterId) {
        Log.d(TAG, "fetchPlacePhotos");
        if (FbConstants.USE_FB) {
            if (AccessToken.getCurrentAccessToken() != null) {
                Log.d(TAG, "fetchPlacePhotos: User is logged in");
                if (afterId == null) {
                    PhotosLoaderCallbacks.initLoader(this, getSupportLoaderManager(), this, id, afterId, LocalConstants.FACEBOOK);
                }
                else {
                    PhotosLoaderCallbacks.restartLoader(this, getSupportLoaderManager(), this, id, afterId, LocalConstants.FACEBOOK);
                }
            }
            else {
                Log.d(TAG, "fetchPlacePhotos: User is not logged in");
            }
        }
        else {
            Log.d(TAG, "fetchPlacePhotos: No photo paging since we are not using FB");
        }
    }

    /**
     * Handle user clicks on toolbar
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //up button was clicked
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: Up button clicked");
                navigateUpToParentActivity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Save paget position in case of rotation or backgrounding
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
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
     * Remove self as listener. Previously added via addOnPageChangeListener(OnPageChangeListener)
     */
    @Override
    protected void onDestroy() {
        viewPager.removeOnPageChangeListener(this);
        super.onDestroy();
    }

    /**
     *
     * @param loaderId
     * @param localPhotosResult
     */
    @Override
    public void onLoadComplete(LoaderId loaderId, @Nullable LocalPhotosResult localPhotosResult) {
        if (loaderId != LoaderId.PHOTOS) {
            Log.d(TAG, "onLoadComplete: Unknown loaderId:" + loaderId);
            return;
        }

        //keep a reference to the result
        this.localPhotosResult = localPhotosResult;
        //check if there's more data to fetch
        hasMoreData = isThereMoreData();

        //update the view pager's adapter
        final PhotosStatePagerAdapter pagerAdapter = (PhotosStatePagerAdapter) viewPager.getAdapter();
        pagerAdapter.updateItems(localPhotosResult != null ? localPhotosResult.getLocalPhotos() : null);

        //update the toolbar with the new count
        updateToolbarTitle(viewPagerPosition);
    }

    /**
     * Save the current pager position when a new page is selected
     * @param position
     */
    @Override
    public void onPageSelected(int position) {
        //save the view pager position so that we can restore later on configuration change
        viewPagerPosition = position;

        //set the toolbar title
        updateToolbarTitle(position);

        //figure out if:
        // 1) we have more data to fetch, i.e. afterId != null (hasMoreData == true)
        // 2) we have hit the halfway point
        Log.d(TAG, String.format("onPageSelected: HasMoreData:%s, AfterId:%s", hasMoreData, localPhotosResult.getAfterId()));
        if (!hasMoreData) return;

        final PhotosStatePagerAdapter pagerAdapter = (PhotosStatePagerAdapter) viewPager.getAdapter();
        final int count = pagerAdapter.getCount();
        final int halfwayPoint = count/2; //don't worry too much of integer math
        Log.d(TAG, String.format("onPageSelected: Total:%d, Halfway:%d, Current Position:%d", count, halfwayPoint, position));

        if (position == halfwayPoint) {
            //we are equal to the halfway point of data, so try to get more data
            Log.d(TAG, "onPageSelected: Fetching new data with afterId:" + localPhotosResult.getAfterId());
            fetchPlacePhotos(localPhotosResult.getAfterId());
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * According to fb documentation, we should stop paging when 'next' is no longer available.
     * Note: AfterId should not be used because it is the cursor that points to the end of the
     * page of data that has been returned.
     * @return
     */
    private boolean isThereMoreData() {
        return localPhotosResult != null && localPhotosResult.getNextUrl() != null;
    }

    /**
     * Updates the toolbar title using the current photo number (zero-index + 1) and the count of photos
     * that we have fetched.
     * There are 2 formats for titles:
     * 1) %d of %d+ : Example: 3 of 25+.  This is used if we have loaded 25 photos and we know there
     *    are more to be fetched if requested.
     * 2) %d of %d : Example: 3 of 25.  This is used if we have loaded 25 photos and we know the
     *    server has no more data.
     * @param position
     */
    private void updateToolbarTitle(int position) {
        //no need to update title if the toolbar is null for some reason
        if (getSupportActionBar() == null) return;

        //figure out if we should use format 1 or 2 for the title and format the title accordingly
        final PhotosStatePagerAdapter pagerAdapter = (PhotosStatePagerAdapter) viewPager.getAdapter();
        final int count = pagerAdapter.getCount();

        //set the title
        getSupportActionBar().setTitle(
                getString(hasMoreData ? R.string.title_detail_photos_parameterized_more : R.string.title_detail_photos_parameterized,
                        String.valueOf(1+position), //add 1 since users are not used to seeing zero-based indexing
                        String.valueOf(count)));
    }
}
