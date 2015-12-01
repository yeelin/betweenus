package com.example.yeelin.projects.betweenus.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalPhoto;
import com.example.yeelin.projects.betweenus.fragment.PhotoFragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ninjakiki on 11/12/15.
 */
public class PhotosStatePagerAdapter
        extends FragmentStatePagerAdapter {
    //logcat
    private static final String TAG = PhotosStatePagerAdapter.class.getCanonicalName();

    //member variables
    //private LocalPhoto[] localPhotos;
    private ArrayList<LocalPhoto> localPhotoArrayList;

    /**
     * Default constructor
     * @param fm
     * @param localPhotos
     */
    public PhotosStatePagerAdapter(FragmentManager fm, @Nullable LocalPhoto[] localPhotos) {
        super(fm);
        //this.localPhotos = localPhotos;

        localPhotoArrayList = new ArrayList<>();
        if (localPhotos != null && localPhotos.length > 0) {
            Log.d(TAG, "Constructor: Local photos is not null and has at least 1 item");
            localPhotoArrayList.addAll(Arrays.asList(localPhotos));
        }
    }

    /**
     * Returns the fragment to be placed in the view pager at the given position.
     * @param position
     * @return
     */
    @Nullable
    @Override
    public Fragment getItem(int position) {
        //if (localPhotos == null || localPhotos.length == 0) {
        if (localPhotoArrayList == null || localPhotoArrayList.size() == 0) {
            Log.d(TAG, "getItem: LocalPhotos array is null or length equals 0");
            return null;
        }

        //final LocalPhoto localPhoto = localPhotos[position];
        final LocalPhoto localPhoto = localPhotoArrayList.get(position);
        if (localPhoto == null) {
            Log.d(TAG, "getItem: Null item at position:" + position);
            return null;
        }

        Log.d(TAG, String.format("getItem: Position:%d", position));
        return PhotoFragment.newInstance(localPhoto.getSourceUrl(), localPhoto.getCaption());
    }

    /**
     * Returns the count of photos in the array
     * @return
     */
    @Override
    public int getCount() {
//        if (localPhotos == null) return 0;
//        return localPhotos.length;
        if (localPhotoArrayList == null) return 0;
        return localPhotoArrayList.size();
    }

//    /**
//     * Swaps the current data array with the new one and notifies listeners that the data
//     * has changed.  Causes the current view to be refreshed.
//     * @param localPhotos
//     */
//    public void swapData(@Nullable LocalPhoto[] localPhotos) {
//        //it's the same data so do nothing
//        if (this.localPhotos == localPhotos) {
//            Log.d(TAG, "swapData: Same data, so do nothing");
//           return;
//        }
//
//        this.localPhotos = localPhotos;
//
//        if (this.localPhotos != null) {
//            Log.d(TAG, "swapData: New data is not null.  Calling notifyDataSetChanged");
//            if (this.localPhotos.length == 0)
//                Log.d(TAG, "swapData: New data has length == 0");
//            notifyDataSetChanged();
//        }
//        else {
//            Log.d(TAG, "swapData: New data is null.  Not calling notifyDataSetChanged");
//        }
//    }

    /**
     * Adds the new array of photos to the end of the arraylist member variable.
     * Notifies the listeners that the data has changed which causes the current view to be refreshed.
     * @param newPhotos
     */
    public void updateItems(@Nullable ArrayList<LocalPhoto> newPhotos) {
        if (newPhotos == null) {
            Log.d(TAG, "updateItems: New data is null, so do nothing. Not calling notifyDataSetChanged");
            return;
        }

        //Add the new photos to the end of the array
        Log.d(TAG, String.format("updateItems: Current size:%d, New size:%d", localPhotoArrayList.size(), newPhotos.size()));

        int startIndexOfNewPhotos = localPhotoArrayList.size();
        localPhotoArrayList.ensureCapacity(localPhotoArrayList.size() + newPhotos.size());
        localPhotoArrayList.addAll(startIndexOfNewPhotos, newPhotos);

        Log.d(TAG, "updateItems: After update size:" + localPhotoArrayList.size());
        notifyDataSetChanged();
    }
}
