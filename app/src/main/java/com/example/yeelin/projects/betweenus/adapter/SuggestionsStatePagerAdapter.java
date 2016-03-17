package com.example.yeelin.projects.betweenus.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalTravelElement;
import com.example.yeelin.projects.betweenus.fragment.SuggestionDetailFragment;
import com.example.yeelin.projects.betweenus.data.generic.model.SimplifiedBusiness;
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
    private ArrayList<LocalTravelElement> userTravelArrayList;
    private LatLng userLatLng;
    private LatLng friendLatLng;
    private LatLng midLatLng;

    private int preferredDataSource;
    private boolean useMetric;

    /**
     * Default constructor
     * @param fm
     * @param businesses
     * @param selectedIdsMap
     * @param userTravelArrayList
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     * @param dataSource
     * @param useMetric
     */
    public SuggestionsStatePagerAdapter(FragmentManager fm,
                                        ArrayList<SimplifiedBusiness> businesses, ArrayMap<String, Integer> selectedIdsMap,
                                        ArrayList<LocalTravelElement> userTravelArrayList,
                                        LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng,
                                        int dataSource, boolean useMetric) {
        super(fm);
        this.businesses = businesses;
        this.selectedIdsMap = selectedIdsMap;
        this.userTravelArrayList = userTravelArrayList;

        this.userLatLng = userLatLng;
        this.friendLatLng = friendLatLng;
        this.midLatLng = midLatLng;

        this.preferredDataSource = dataSource;
        this.useMetric = useMetric;
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

        Log.d(TAG, String.format("getItem: Position:%d, Business:%s, Fblink:%s", position, business.getName(), business.getFbUrl()));
        return SuggestionDetailFragment.newInstance(
                business.getId(),
                business.getName(),
                business.getLatLng(),
                position,
                selectedIdsMap.containsKey(business.getId()),
                business.getRating(),
                business.getLikes(),
                business.getNormalizedLikes(),
                userTravelArrayList.get(position),
                userLatLng,
                friendLatLng,
                midLatLng,
                preferredDataSource, useMetric);
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
