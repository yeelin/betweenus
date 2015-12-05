package com.example.yeelin.projects.betweenus.loader.callback;

import android.support.annotation.Nullable;

import com.example.yeelin.projects.betweenus.data.LocalResult;

/**
 * Created by ninjakiki on 10/30/15.
 */
public interface SuggestionsLoaderListener {
    /**
     * Listener interface. The loader's listener is usually the ui.
     * @param loaderId
     * @param localResult
     */
    void onLoadComplete(int loaderId, @Nullable LocalResult localResult);

}
