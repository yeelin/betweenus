package com.example.yeelin.projects.betweenus.loader.callback;

import android.support.annotation.Nullable;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.loader.LoaderId;

/**
 * Created by ninjakiki on 10/30/15.
 */
public interface SingleSuggestionLoaderListener {
    void onLoadComplete(LoaderId loaderId, @Nullable LocalBusiness business);
}
