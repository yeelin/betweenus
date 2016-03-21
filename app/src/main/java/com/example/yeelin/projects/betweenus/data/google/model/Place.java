package com.example.yeelin.projects.betweenus.data.google.model;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalBusinessLocation;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;

/**
 * Created by ninjakiki on 3/17/16.
 */
public class Place implements LocalBusiness {
    //available from nearby search api
    private final String icon;
    private final PlaceGeometry geometry;
    private final String name;
    private final PlaceHours opening_hours;
    private final PlacePhoto[] photos;
    private final String place_id;
    private final int price_level;
    private final double rating;
    private final String vicinity;
    private final String[] types;

    //available from placedetails api
    private final String formatted_address;
    private final String formatted_phone_number;
    private final PlaceReview[] reviews;
    private final int user_ratings_total;
    private final String url;
    private final String website;

    public Place(String icon, PlaceGeometry geometry, String name, PlaceHours opening_hours, PlacePhoto[] photos, String place_id, int price_level, double rating, String vicinity, String[] types,
                 String formatted_address, String formatted_phone_number, PlaceReview[] reviews, int user_ratings_total, String url, String website) {
        this.icon = icon;
        this.geometry = geometry;
        this.name = name;
        this.opening_hours = opening_hours;
        this.photos = photos;
        this.place_id = place_id;
        this.price_level = price_level;
        this.rating = rating;
        this.vicinity = vicinity;
        this.types = types;

        this.formatted_address = formatted_address;
        this.formatted_phone_number = formatted_phone_number;
        this.reviews = reviews;
        this.user_ratings_total = user_ratings_total;
        this.url = url;
        this.website = website;
    }

    public String getIcon() {
        return icon;
    }

    public PlaceGeometry getGeometry() {
        return geometry;
    }

    @Override
    public String getId() {
        return place_id;
    }

    @Override
    public String getAbout() {
        return null;
    }

    @Override
    public String getAttire() {
        return null;
    }

    @Override
    public String getCategory() {
        return Arrays.toString(types);
    }

    @Override
    public String[] getCategoryList() {
        return types;
    }

    @Override
    public String getCulinaryTeam() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String[] getCuisine() {
        return new String[0];
    }

    @Override
    public String getGeneralInfo() {
        return null;
    }

    @Override
    public String[] getHours() {
        return opening_hours.getWeekday_text();
    }

    @Override
    public boolean isAlwaysOpen() {
        return false;
    }

    @Override
    public String getFbLink() {
        return url;
    }

    @Override
    public LocalBusinessLocation getLocalBusinessLocation() {
        return geometry;
    }

    public String getName() {
        return name;
    }

    @Override
    public String[] getParking() {
        return new String[0];
    }

    @Override
    public String[] getPaymentOptions() {
        return new String[0];
    }

    @Override
    public String getPhoneNumber() {
        return formatted_phone_number;
    }

    @Override
    public String getProfilePictureUrl() {
        return null;
    }

    @Override
    public String getPriceRangeString() {
        switch (price_level) {
            case 0: return "free";
            case 1: return "$";
            case 2: return "$$";
            case 3: return "$$$";
            case 4: return  "$$$$";
            default: return "Not available";
        }
    }

    @Override
    public int getPriceRange() {
        return price_level;
    }

    @Override
    public String getPublicTransit() {
        return null;
    }

    @Override
    public String[] getRestaurantServices() {
        return new String[0];
    }

    @Override
    public String[] getRestaurantSpecialities() {
        return new String[0];
    }

    @Override
    public String getMobileUrl() {
        return website;
    }

    public PlaceHours getOpening_hours() {
        return opening_hours;
    }

    public PlacePhoto[] getPhotos() {
        return photos;
    }

    public String getPlace_id() {
        return place_id;
    }

    public int getPrice_level() {
        return price_level;
    }

    public double getRating() {
        return rating;
    }

    @Override
    public String getRatingImageUrl() {
        return null;
    }

    @Override
    public int getReviewCount() {
        return user_ratings_total;
    }

    @Override
    public int getCheckins() {
        return 0;
    }

    @Override
    public int getLikes() {
        return 0;
    }

    @Override
    public double getNormalizedLikes() {
        return 0;
    }

    @Override
    public int getDataSource() {
        return LocalConstants.GOOGLE;
    }

    public String getVicinity() {
        return vicinity;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public String getFormatted_phone_number() {
        return formatted_phone_number;
    }

    public PlaceReview[] getReviews() {
        return reviews;
    }

    public String getUrl() {
        return url;
    }

    public String getWebsite() {
        return website;
    }

    public int getUser_ratings_total() {
        return user_ratings_total;
    }

    @Override
    public String toString() {
        return String.format("Name:%s, Geometry:%s, OpeningHours:%s, Price:%s, Rating:%f, Vicinity:%s, Address:%s, Phone:%s, Reviews:%s, Url:%s, Website:%s",
                name, geometry, opening_hours, price_level, rating, vicinity, formatted_address, formatted_phone_number, Arrays.toString(reviews), url, website);
    }

    public String[] getTypes() {
        return types;
    }

    /**
     * Place Geometry
     */
    public static class PlaceGeometry implements LocalBusinessLocation {
        private final PlaceLocation location;

        public PlaceGeometry(PlaceLocation location) {
            this.location = location;
        }

        public PlaceLocation getLocation() {
            return location;
        }

        @Override
        public String toString() {
            return String.format("LatLng:%s", location);
        }

        @Override
        public String getShortDisplayAddress() {
            return null;
        }

        @Override
        public String getLongDisplayAddress() {
            return null;
        }

        @Override
        public String getCrossStreets() {
            return null;
        }

        @Override
        public LatLng getLatLng() {
            return new LatLng(location.getLat(), location.getLng());
        }

    }

    /**
     * Place location
     */
    public static class PlaceLocation {
        private final double lat;
        private final double lng;

        public PlaceLocation(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }

    /**
     * Place Hours
     * Consists of open_now boolean, periods of open/close, and weekday text which is periods
     * in the form of string.
     */
    public static class PlaceHours {
        private final String open_now;
        private final String[] weekday_text;
        private final Period[] periods;

        public PlaceHours(String open_now, String[] weekday_text, Period[] periods) {
            this.open_now = open_now;
            this.weekday_text = weekday_text;
            this.periods = periods;
        }

        public String getOpen_now() {
            return open_now;
        }

        public boolean isOpenNow() { return open_now.equalsIgnoreCase("true"); }

        public String[] getWeekday_text() {
            return weekday_text;
        }

        public Period[] getPeriods() {
            return periods;
        }

        @Override
        public String toString() {
            return String.format("OpenNow:%s, WeekdayText:%s, Periods:%s",
                    open_now, Arrays.toString(weekday_text), Arrays.toString(periods));
        }
    }

    /**
     * Period class
     * Each Period consists of 2 subperiods, one for open and one for close.
     * A day can have more than 1 period if there are breaks in between when it's open and closed.
     */
    public static class Period {
        private final SubPeriod close;
        private final SubPeriod open;

        public Period(SubPeriod close, SubPeriod open) {
            this.close = close;
            this.open = open;
        }

        public SubPeriod getClose() {
            return close;
        }

        public SubPeriod getOpen() {
            return open;
        }
    }

    /**
     * SubPeriod class
     * Each subperiod consists of a day and time.  2 subperiods make 1 period
     */
    public static class SubPeriod {
        private final int day;
        private final int time;

        public SubPeriod(int day, int time)
        {
            this.day = day;
            this.time = time;
        }

        public int getDay() {
            return day;
        }

        public int getTime() {
            return time;
        }
    }

        /**
     * Place Photo
     */
    public static class PlacePhoto {
        private final int height;
        private final int width;
        private final String[] html_attributions;
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

    /**
     * Place review
     */
    public static class PlaceReview {
        private final AspectRating[] aspects;
        private final String author_name;
        private final int rating;
        private final String text;
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
    }

    /**
     * Aspect rating
     */
    public static class AspectRating {
        private final int rating;
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
