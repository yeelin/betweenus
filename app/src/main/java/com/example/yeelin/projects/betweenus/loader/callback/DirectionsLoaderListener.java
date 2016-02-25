package com.example.yeelin.projects.betweenus.loader.callback;

import android.support.annotation.Nullable;

import com.example.yeelin.projects.betweenus.data.google.model.DirectionsResult;

/**
 * Created by ninjakiki on 2/19/16.
 */
public interface DirectionsLoaderListener {
    /**
     * Listener interface.  The listener is usually the ui.
     * @param loaderId
     * @param directionsResult
     */
    void onLoadComplete(int loaderId, @Nullable DirectionsResult directionsResult);
}
