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

    /**
     * Distance
     */
    public static class Distance {
        private final String text;
        private final int value;

        public Distance(String text, int value) {
            this.text = text;
            this.value = value;
        }

        public int getValue() {
            return value;
        }
        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return String.format("Text:%s,Value:%d", text, value);
        }
    }

    /**
     * Duration
     */
    public static class Duration {
        private final String text;
        private final int value;

        public Duration(String text, int value) {
            this.text = text;
            this.value = value;
        }

        public int getValue() {
            return value;
        }
        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return String.format("Text:%s,Value:%d", text, value);
        }
    }
}
