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

    //saved instance state
    private static final String STATE_PAGER_POSITION = PhotosPagerActivity.class.getSimpleName() + ".pagerPosition";

    //member variables
    private ViewPager viewPager;

    private int viewPagerPosition = 0;
    private String id;
    //private LocalPhoto[] localPhotos;

    /**
     * Builds intent to start this activity
     * @param context
     * @param id
     * @return
     */
    public static Intent buildIntent(Context context, String id) {
        Intent intent = new Intent(context, PhotosPagerActivity.class);
        intent.putExtra(EXTRA_ID, id);
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

        //set up view pager
        viewPager = (ViewPager) findViewById(R.id.picture_viewPager);
        PhotosStatePagerAdapter pagerAdapter = new PhotosStatePagerAdapter(getSupportFragmentManager(), null);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setCurrentItem(viewPagerPosition);

        //fetch photo urls for the given place id
        fetchPlacePhotos();
    }

    /**
     * Fetch photo urls for this place/detail page using a loader
     */
    private void fetchPlacePhotos() {
        Log.d(TAG, "fetchPlacePhotos");
        if (FbConstants.USE_FB) {
            if (AccessToken.getCurrentAccessToken() != null) {
                Log.d(TAG, "fetchPlacePhotos: User is logged in");
                PhotosLoaderCallbacks.initLoader(this, getSupportLoaderManager(), this, id, LocalConstants.FACEBOOK);
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
     * @param localPhotos
     */
    @Override
    public void onLoadComplete(LoaderId loaderId, @Nullable LocalPhoto[] localPhotos) {
        if (loaderId != LoaderId.PHOTOS) {
            Log.d(TAG, "onLoadComplete: Unknown loaderId:" + loaderId);
            return;
        }

        //this.localPhotos = localPhotos;

        //PhotosStatePagerAdapter pagerAdapter = new PhotosStatePagerAdapter(getSupportFragmentManager(), localPhotos);
        PhotosStatePagerAdapter pagerAdapter = (PhotosStatePagerAdapter) viewPager.getAdapter();
        pagerAdapter.swapData(localPhotos);
    }

    /**
     * Save the current pager position when a new page is selected
     * @param position
     */
    @Override
    public void onPageSelected(int position) {
        viewPagerPosition = position;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
