package com.example.yeelin.projects.betweenus.data.fb.model;

/**
 * Created by ninjakiki on 10/27/15.
 */
public class FbPictureData {
    private final boolean is_silhouette;
    private final String url;

    public FbPictureData(boolean is_silhouette, String url) {
        this.is_silhouette = is_silhouette;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public boolean is_silhouette() {
        return is_silhouette;
    }

    public String toString() {
        return String.format("[is_silhouette:%s, Url:%s]", is_silhouette, url);
    }
}
