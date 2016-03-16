package com.example.yeelin.projects.betweenus.data;

/**
 * Created by ninjakiki on 3/2/16.
 */
public interface LocalTravelElement {
    int getTravelDuration(); //seconds
    int getTravelDistance(); //meters

    String getTravelDurationText(); //not guaranteed to be in seconds
    String getTravelDistanceText(); //not guaranteed to be in meters
}
