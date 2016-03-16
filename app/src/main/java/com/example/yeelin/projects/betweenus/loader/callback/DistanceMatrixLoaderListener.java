package com.example.yeelin.projects.betweenus.loader.callback;

import android.support.annotation.Nullable;

import com.example.yeelin.projects.betweenus.data.google.model.DistanceMatrixResult;

/**
 * Created by ninjakiki on 2/26/16.
 */
public interface DistanceMatrixLoaderListener {
    /**
     * Listener interface.  The listener is usually the ui.
     * @param loaderId
     * @param distanceMatrixResult
     */
    void onLoadComplete(int loaderId, @Nullable DistanceMatrixResult distanceMatrixResult);
}
