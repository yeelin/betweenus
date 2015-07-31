package com.example.yeelin.projects.betweenus.fragment;

import android.support.annotation.Nullable;

import com.example.yeelin.projects.betweenus.model.YelpResult;

/**
 * Created by ninjakiki on 7/29/15.
 */
public interface SuggestionsCallbacks {
    /**
     * The loader has finished fetching the data.  Called by SuggestionsActivity to update the view.
     * To be implemented by fragments interested in the result.
     * @param yelpResult
     */
    public void onLoadComplete(@Nullable YelpResult yelpResult);
}
