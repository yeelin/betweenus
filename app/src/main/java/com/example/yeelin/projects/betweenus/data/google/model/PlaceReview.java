package com.example.yeelin.projects.betweenus.data.google.model;

import java.util.Arrays;

/**
 * Place review
 */
public class PlaceReview {
    //contains a collection of AspectRating objects, each of which provides a rating of a single attribute of the establishment.
    // The first object in the collection is considered the primary aspect.
    private final AspectRating[] aspects;
    // the name of the user who submitted the review. Anonymous reviews are attributed to "A Google user".
    private final String author_name;
    //the user's overall rating for this place. This is a whole number, ranging from 1 to 5.
    private final int rating;
    //the user's review. When reviewing a location with Google Places, text reviews are considered optional. Therefore, this field may by empty. Note that this field may include simple HTML markup.
    private final String text;
    //time that the review was submitted, measured in the number of seconds since since midnight, January 1, 1970 UTC.
    private final long time;

    public PlaceReview(AspectRating[] aspects, String author_name, int rating, String text, long time) {
        this.aspects = aspects;
        this.author_name = author_name;
        this.rating = rating;
        this.text = text;
        this.time = time;
    }

    public AspectRating[] getAspects() {
        return aspects;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public int getRating() {
        return rating;
    }

    public String getText() {
        return text;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return String.format("Aspects:%s, Author:%s, Rating:%d, Time:%d, Text:%s",
                Arrays.toString(aspects), author_name, rating, time, text);
    }

    /**
     * Aspect rating
     */
    public static class AspectRating {
        //the user's rating for this particular aspect, from 0 to 3.
        private final int rating;
        //the name of the aspect that is being rated. The following types are supported:
        // appeal, atmosphere, decor, facilities, food, overall, quality and service.
        private final String type;

        public AspectRating(int rating, String type) {
            this.rating = rating;
            this.type = type;
        }

        public int getRating() {
            return rating;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return String.format("Rating:%d, Type:%s", rating, type);
        }
    }
}
