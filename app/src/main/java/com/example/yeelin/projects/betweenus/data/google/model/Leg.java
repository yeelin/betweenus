package com.example.yeelin.projects.betweenus.data.google.model;

/**
 * Created by ninjakiki on 2/22/16.
 */
public class Leg {
    private final Distance distance;
    private final Duration duration;

    public Leg(Distance distance, Duration duration) {
        this.distance = distance;
        this.duration = duration;
    }

    public Distance getDistance() {
        return distance;
    }
    public Duration getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return String.format("Distance:%s, Duration:%s", distance, duration);
    }
}
