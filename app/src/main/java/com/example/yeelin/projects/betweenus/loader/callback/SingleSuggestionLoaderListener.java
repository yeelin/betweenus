package com.example.yeelin.projects.betweenus.loader.callback;

import android.support.annotation.Nullable;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;

/**
 * Created by ninjakiki on 10/30/15.
 */
public interface SingleSuggestionLoaderListener {
    /**
     * Listener interface. The loader's listener is usually the ui.
     * @param loaderId
     * @param business
     */
    void onLoadComplete(int loaderId, @Nullable LocalBusiness business);
}
