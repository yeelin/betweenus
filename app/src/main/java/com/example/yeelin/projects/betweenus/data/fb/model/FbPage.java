package com.example.yeelin.projects.betweenus.data.fb.model;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalBusinessLocation;

/**
 * Created by ninjakiki on 10/27/15.
 */
public class FbPage implements LocalBusiness {
    private final String id;
    private final String category;
    private final FbLocation location;
    private final String name;
    private final String phone;
    private final FbPagePicture picture;
    private final String website;
    private final int checkins;
    private final int likes;

    public FbPage(String id, String category, FbLocation location, String name, String phone, FbPagePicture picture, String website, int checkins, int likes) {
        this.id = id;
        this.category = category;
        this.location = location;
        this.name = name;
        this.phone = phone;
        this.picture = picture;
        this.website = website;
        this.checkins = checkins;
        this.likes = likes;
    }

    public String toString() {
        return String.format("{FbPage: [Id:%s, Name:%s, Location:%s, Checkins:%d, Likes:%d, Category:%s, Picture:%s]}" ,
                id, name, location, checkins, likes, category, picture);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getImageUrl() {
        return picture.getData().getUrl();
    }

    @Override
    public String getMobileUrl() {
        return website;
    }

    @Override
    public String getPhoneNumber() {
        return phone;
    }

    @Override
    public int getReviewCount() {
        return checkins;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public double getRating() {
        return likes;
    }

    @Override
    public String getRatingImageUrl() {
        return null;
    }

    @Override
    public LocalBusinessLocation getLocalBusinessLocation() {
        return location;
    }
}
