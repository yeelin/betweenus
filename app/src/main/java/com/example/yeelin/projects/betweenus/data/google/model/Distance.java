package com.example.yeelin.projects.betweenus.data.google.model;

/**
 * Created by ninjakiki on 2/26/16.
 * Total distance of this route
 */
public class Distance {
    //uses unit specified in original request or origin's region
    private final String text;
    //total distance expressed in meters (always)
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