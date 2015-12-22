package com.example.yeelin.projects.betweenus.fragment.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/29/15.
 */
public interface OnSuggestionsLoadedCallback {
    /**
     * Latlngs are ready.  Called by SuggestionsActivity.
     * To be implemented by fragments interested in the result.
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng
     */
    void onLatLngLoad(LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng);

    /**
     * A single page of data is ready.  Called by SuggestionsActivity.
     * To be implemented by fragments interested in the result.
     * @param localResult
     * @param selectedIdsMap
     * @param hasMoreData
     */
    void onSinglePageLoad(@Nullable LocalResult localResult,
                          @NonNull ArrayMap<String, Integer> selectedIdsMap,
                          boolean hasMoreData);

    /**
     * Multiple pages of data are ready.  Called by SuggestionsActivity.
     * To be implemented by fragments interested in the result.
     * @param localResultArrayList
     * @param selectedIdsMap
     * @param hasMoreData
     */
    void onMultiPageLoad(ArrayList<LocalResult> localResultArrayList,
                         @NonNull ArrayMap<String, Integer> selectedIdsMap,
                         boolean hasMoreData);
}
