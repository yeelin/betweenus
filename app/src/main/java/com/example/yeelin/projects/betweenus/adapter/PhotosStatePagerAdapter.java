package com.example.yeelin.projects.betweenus.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalPhoto;
import com.example.yeelin.projects.betweenus.fragment.PhotoFragment;

/**
 * Created by ninjakiki on 11/12/15.
 */
public class PhotosStatePagerAdapter
        extends FragmentStatePagerAdapter {
    //logcat
    private static final String TAG = PhotosStatePagerAdapter.class.getCanonicalName();

    //member variables
    private LocalPhoto[] localPhotos;

    /**
     * Default constructor
     * @param fm
     */
    public PhotosStatePagerAdapter(FragmentManager fm, LocalPhoto[] localPhotos) {
        super(fm);
        this.localPhotos = localPhotos;
    }

    /**
     * Returns the fragment to be placed in the view pager at the given position.
     * @param position
     * @return
     */
    @Nullable
    @Override
    public Fragment getItem(int position) {
        if (localPhotos == null || localPhotos.length == 0) {
            Log.d(TAG, "getItem: LocalPhotos array is null or length equals 0");
            return null;
        }

        final LocalPhoto localPhoto = localPhotos[position];
        if (localPhoto == null) {
            Log.d(TAG, "getItem: Null item at position:" + position);
            return null;
        }

        Log.d(TAG, String.format("getItem: Position:%d, Photo url:%s", position, localPhoto.getSourceUrl()));
        return PhotoFragment.newInstance(localPhoto.getSourceUrl(), localPhoto.getCaption());
    }

    /**
     * Returns the count of photos.
     * @return
     */
    @Override
    public int getCount() {
        if (localPhotos == null) return 0;
        return localPhotos.length;
    }

    /**
     * Swaps the current data array with the new one and notifies listeners that the data
     * has changed.  Causes the current view to be refreshed.
     * @param localPhotos
     */
    public void swapData(@Nullable LocalPhoto[] localPhotos) {
        //it's the same data so do nothing
        if (this.localPhotos == localPhotos) {
            Log.d(TAG, "swapData: Same data, so do nothing");
           return;
        }

        this.localPhotos = localPhotos;

        if (this.localPhotos != null) {
            Log.d(TAG, "swapData: New data is not null.  Calling notifyDataSetChanged");
            notifyDataSetChanged();
        }
        else {
            Log.d(TAG, "swapData: New data is null.  Not calling notifyDataSetChanged");
        }
    }
}
