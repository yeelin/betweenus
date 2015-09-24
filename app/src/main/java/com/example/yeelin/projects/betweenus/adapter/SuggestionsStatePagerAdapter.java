package com.example.yeelin.projects.betweenus.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.example.yeelin.projects.betweenus.fragment.SuggestionDetailFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 9/21/15.
 */
public class SuggestionsStatePagerAdapter
        extends FragmentStatePagerAdapter {
    //logcat
    private static final String TAG = SuggestionsStatePagerAdapter.class.getCanonicalName();

    //member variables
    private ArrayList<SimplifiedBusiness> businesses;
    private ArrayMap<String, Integer> selectedIdsMap;
    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    /**
     * Default constructor
     * @param fm
     * @param businesses
     * @param selectedIdsMap
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    public SuggestionsStatePagerAdapter(FragmentManager fm,
                                        ArrayList<SimplifiedBusiness> businesses, ArrayMap<String, Integer> selectedIdsMap,
                                        LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng) {
        super(fm);
        this.businesses = businesses;
        this.selectedIdsMap = selectedIdsMap;
        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        this.midLatLng = midLatLng;

    }

    /**
     * Returns the fragment to be placed in the view pager at the given position.
     * @param position
     * @return
     */
    @Nullable
    @Override
    public Fragment getItem(int position) {
        if (businesses == null || businesses.size() == 0) {
            Log.d(TAG, "getItem: Businesses is null or size equals 0");
            return null;
        }

        final SimplifiedBusiness business = businesses.get(position);
        if (business == null) {
            Log.d(TAG, "getItem: Null item at position:" + position);
            return null;
        }

        Log.d(TAG, String.format("getItem: Position:%d, Business:%s", position, business.getName()));
        return SuggestionDetailFragment.newInstance(
                business.getId(),
                business.getName(),
                business.getLatLng(),
                position,
                selectedIdsMap.containsKey(business.getId()),
                userLatLng,
                friendLatLng,
                midLatLng);
    }

    /**
     * Returns the count of businesses.
     * @return
     */
    @Override
    public int getCount() {
        if (businesses == null) {
            return 0;
        }

        return businesses.size();
    }
}
