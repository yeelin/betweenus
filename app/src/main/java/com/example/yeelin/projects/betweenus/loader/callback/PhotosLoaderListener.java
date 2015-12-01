package com.example.yeelin.projects.betweenus.loader.callback;

import android.support.annotation.Nullable;

import com.example.yeelin.projects.betweenus.data.LocalPhotosResult;
import com.example.yeelin.projects.betweenus.loader.LoaderId;

/**
 * Created by ninjakiki on 11/13/15.
 */
public interface PhotosLoaderListener {
    /**
     * Listener interface. The listener is usually the ui.
     * @param loaderId
     * @param localPhotosResult
     */
    void onLoadComplete(LoaderId loaderId, @Nullable LocalPhotosResult localPhotosResult);
}
