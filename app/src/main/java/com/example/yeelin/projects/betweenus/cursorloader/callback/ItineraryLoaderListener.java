package com.example.yeelin.projects.betweenus.cursorloader.callback;

import android.database.Cursor;
import android.support.annotation.Nullable;

/**
 * Created by ninjakiki on 3/29/16.
 */
public interface ItineraryLoaderListener {
    /**
     * Listener interface for the Itinerary loader.
     * @param loaderId
     * @param cursor
     */
    void onLoadComplete (int loaderId, @Nullable Cursor cursor);
}
