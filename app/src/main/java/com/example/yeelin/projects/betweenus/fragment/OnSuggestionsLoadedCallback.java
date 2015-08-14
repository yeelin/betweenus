package com.example.yeelin.projects.betweenus.fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;

import com.example.yeelin.projects.betweenus.model.YelpResult;

/**
 * Created by ninjakiki on 7/29/15.
 */
public interface OnSuggestionsLoadedCallback {
    /**
     * The loader has finished fetching the data.  Called by SuggestionsActivity to update the view.
     * To be implemented by fragments interested in the result.
     * @param result
     * @param selectedIdsMap
     */
    public void onSuggestionsLoaded(@Nullable YelpResult result, @NonNull ArrayMap<String,String> selectedIdsMap);
}
