package com.example.yeelin.projects.betweenus.data;

import android.os.Parcelable;

/**
 * Created by ninjakiki on 11/13/15.
 * Local Photo extends Parcelable so that we can pass it from one activity to another via intents
 */
public interface LocalPhoto extends Parcelable {
    String getSourceUrl();
    String getCaption();
    String getAttribution();
    int getWidth();
    int getHeight();
}
