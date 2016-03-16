package com.example.yeelin.projects.betweenus.data.google.model;

import com.example.yeelin.projects.betweenus.data.LocalTravelElement;

/**
 * Created by ninjakiki on 2/26/16.
 * Contains information about each origin-destination pairing
 */
public class Element implements LocalTravelElement {
    private final String status;
    private final Duration duration;
    private final Distance distance;
    //TODO: Add duration in traffic later
    //TODO: Add fare later

    public Element(String status, Duration duration, Distance distance) {
        this.status = status;
        this.duration = duration;
        this.distance = distance;
    }

    public String getStatus() {
        return status;
    }

    public Duration getDuration() {
        return duration;
    }

    public Distance getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return String.format("Status:%s, Duration:%s, Distance:%s", status, duration, distance);
    }

    @Override
    public int getTravelDuration() {
        return duration.getValue(); // in seconds
    }

    @Override
    public int getTravelDistance() {
        return distance.getValue(); // in meters
    }

    @Override
    public String getTravelDurationText() {
        return duration.getText(); //localized using query's language parameter, do not recommend using
    }

    @Override
    public String getTravelDistanceText() {
        return distance.getText(); //uses unit specified in original request or origin's region, do not recommend using
    }
}
