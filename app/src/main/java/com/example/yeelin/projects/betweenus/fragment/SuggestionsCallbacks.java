package com.example.yeelin.projects.betweenus.fragment;

import android.support.annotation.Nullable;

import com.example.yeelin.projects.betweenus.model.YelpBusiness;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/29/15.
 */
public interface SuggestionsCallbacks {
    /**
     * The loader has finished fetching the data.  Called by SuggestionsActivity to update the view.
     * @param suggestedItems
     */
    public void onLoadComplete(@Nullable ArrayList<YelpBusiness> suggestedItems);
}
