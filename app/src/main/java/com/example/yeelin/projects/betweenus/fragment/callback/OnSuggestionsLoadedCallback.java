package com.example.yeelin.projects.betweenus.fragment.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ninjakiki on 7/29/15.
 */
public interface OnSuggestionsLoadedCallback {
    /**
     * The loader has finished fetching the data.  Called by SuggestionsActivity to update the view.
     * To be implemented by fragments interested in the result.
     * @param result
     * @param selectedIdsMap
     * @param userLatLng
     * @param friendLatLng
     * @param midLatLng 
     */
    public void onSuggestionsLoaded(@Nullable LocalResult result, @NonNull ArrayMap<String,Integer> selectedIdsMap, LatLng userLatLng, LatLng friendLatLng, LatLng midLatLng);
}
