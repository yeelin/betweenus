package com.example.yeelin.projects.betweenus.data.google.model;

/**
 * Created by ninjakiki on 2/26/16.
 * Length of time it takes to travel this route
 */
public class Duration {
    //localized using query's language parameter
    private final String text;
    //expressed in seconds
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