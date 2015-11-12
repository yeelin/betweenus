package com.example.yeelin.projects.betweenus.data.generic.model;

import android.content.Context;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by ninjakiki on 9/29/15.
 */
public class PlaceClusterItem implements ClusterItem {
    private final LatLng position;
    private final String id;
    private final String title;

    private final String ratingUrl;
    private final double rating;
    private final int reviews;
    private final int likes;
    private final double normalizedLikes;
    private final int checkins;

    private String reviewsSnippet;
    private String likesSnippet;
    private String checkinsSnippet;

    private final int resultPosition;
    private final Context context;

    /**
     * Constructor
     * @param position
     * @param id
     * @param title
     * @param ratingUrl
     * @param rating
     * @param reviews
     * @param likes
     * @param normalizedLikes
     * @param checkins
     * @param resultPosition
     * @param context
     */
    public PlaceClusterItem(LatLng position, String id, String title,
                            String ratingUrl, double rating, int reviews, int likes, double normalizedLikes, int checkins,
                            int resultPosition, Context context) {
        this.position = position;
        this.id = id;
        this.title = title;

        this.ratingUrl = ratingUrl;
        this.rating = rating;
        this.reviews = reviews;
        this.likes = likes;
        this.normalizedLikes = normalizedLikes;
        this.checkins = checkins;

        this.resultPosition = resultPosition;
        this.context = context;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getRatingUrl() {
        return ratingUrl;
    }

    public double getRating() {
        return rating;
    }

    public int getReviews() {
        return reviews;
    }

    public int getLikes() {
        return likes;
    }

    public double getNormalizedLikes() {
        return normalizedLikes;
    }

    public int getCheckins() {
        return checkins;
    }

    public int getResultPosition() {
        return resultPosition;
    }

    public String getReviewsSnippet() {
        if (reviews != LocalConstants.NO_DATA_INTEGER && reviewsSnippet == null) {
            reviewsSnippet = context.getString(R.string.review_count, reviews);
        }
        return reviewsSnippet;
    }

    public String getLikesSnippet() {
        if (likes != LocalConstants.NO_DATA_INTEGER && likesSnippet == null) {
            likesSnippet = context.getResources().getQuantityString(R.plurals.short_like_count, likes, likes);
        }
        return likesSnippet;
    }

    public String getCheckinsSnippet() {
        if (checkins != LocalConstants.NO_DATA_INTEGER && checkinsSnippet == null) {
            checkinsSnippet = context.getResources().getQuantityString(R.plurals.short_checkin_count, checkins, checkins);
        }
        return checkinsSnippet;
    }
}
