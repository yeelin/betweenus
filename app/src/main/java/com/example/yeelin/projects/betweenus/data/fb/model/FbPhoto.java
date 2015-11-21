package com.example.yeelin.projects.betweenus.data.fb.model;

import com.example.yeelin.projects.betweenus.data.LocalPhoto;

/**
 * Created by ninjakiki on 11/13/15.
 */
public class FbPhoto implements LocalPhoto {
    private final String id;
    private final String name;
    private final int height;
    private final int width;
    private final FbPhotoImages[] images;

    //derived member variable
    private String sourceUrl;
    private String highResSourceUrl;

    public FbPhoto(String id, String name, int height, int width, FbPhotoImages[] images) {
        this.id = id;
        this.name = name;
        this.height = height;
        this.width = width;
        this.images = images;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public FbPhotoImages[] getImages() {
        return images;
    }

    @Override
    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getHighResSourceUrl() {
        return highResSourceUrl;
    }

    @Override
    public String getCaption() {
        return name;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setHighResSourceUrl(String highResSourceUrl) {
        this.highResSourceUrl = highResSourceUrl;
    }

    public static class FbPhotoImages {
        private final int height;
        private final int width;
        private final String source;

        public FbPhotoImages(int height, int width, String source) {
            this.height = height;
            this.width = width;
            this.source = source;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public String getSource() {
            return source;
        }
    }
}
