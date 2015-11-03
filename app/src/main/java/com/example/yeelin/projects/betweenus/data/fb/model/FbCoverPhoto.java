package com.example.yeelin.projects.betweenus.data.fb.model;

/**
 * Created by ninjakiki on 10/30/15.
 */
public class FbCoverPhoto {
    private final String cover_id;
    private final float offset_x;
    private final float offset_y;
    private final String source;
    private final String id;

    public FbCoverPhoto(String cover_id, float offset_x, float offset_y, String source, String id) {
        this.cover_id = cover_id;
        this.offset_x = offset_x;
        this.offset_y = offset_y;
        this.source = source;
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("[Id:%s]", id);
    }
}
