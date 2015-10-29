package com.example.yeelin.projects.betweenus.data.fb.model;

/**
 * Created by ninjakiki on 10/27/15.
 */
public class FbPagePicture {
    private final FbPictureData data;

    public FbPagePicture(FbPictureData data) {
        this.data = data;
    }
    
    public FbPictureData getData() {
        return data;
    }

    public String toString() {
        return data.toString();
    }
}
