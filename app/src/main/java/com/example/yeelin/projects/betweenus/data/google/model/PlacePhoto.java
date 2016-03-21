package com.example.yeelin.projects.betweenus.data.google.model;

import java.util.Arrays;

/**
 * Place Photo
 * A Place Search will return at most one photo object.
 * Performing a Place Details request on the place may return up to ten photos.
 */
public class PlacePhoto {
    //the maximum height of the image.
    private final int height;
    //the maximum width of the image.
    private final int width;
    //contains any required attributions. This field will always be present, but may be empty.
    private final String[] html_attributions;
    //a string used to identify the photo when you perform a Photo request.
    private final String photo_reference;

    public PlacePhoto(int height, int width, String[] html_attributions, String photo_reference) {
        this.height = height;
        this.width = width;
        this.html_attributions = html_attributions;
        this.photo_reference = photo_reference;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String[] getHtml_attributions() {
        return html_attributions;
    }

    public String getPhoto_reference() {
        return photo_reference;
    }

    @Override
    public String toString() {
        return String.format("Height:%d, Width:%d, Attributions:%s, Reference:%s",
                height, width, Arrays.toString(html_attributions), photo_reference);
    }
}