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
    //available from nearby search or text search api

    //contains the URL of a recommended icon which may be displayed to the user when indicating this result
    private final String icon;
    //contains geometry information about the result, generally including the location (geocode) of the place and (optionally) the viewport identifying its general area of coverage.
    private final PlaceGeometry geometry;
    //contains the human-readable name for the returned result. For establishment results, this is usually the business name.
    private final String name;
    //see PlaceHours class for info
    private final PlaceHours opening_hours;
    //an array of photo objects, each containing a reference to an image. A Place Search will return at most one photo object. Performing a Place Details request on the place may return up to ten photos.
    private final PlacePhoto[] photos;
    //a textual identifier that uniquely identifies a place.
    private final String place_id;
    //The price level of the place, on a scale of 0 to 4. The exact amount indicated by a specific value will vary from region to region.
    private final int price_level;
    //contains the place's rating, from 1.0 to 5.0, based on aggregated user reviews.
    private final double rating;
    //contains a feature name of a nearby location. Often this feature refers to a street or neighborhood within the given results. The vicinity property is only returned for a Nearby Search.
    private final String vicinity;
    //contains an array of feature types describing the given result.
    private final String[] types;

    //available from placedetails api
    //an array of separate address components used to compose a given address.
    private final AddressComponent[] address_components;
    //string containing the human-readable address of this place. Often this address is equivalent to the "postal address". The formatted_address property is only returned for a Text Search.
    private final String formatted_address;
    // contains the place's phone number in its local format.
    private final String formatted_phone_number;
    //an array of up to five reviews
    private final PlaceReview[] reviews;
    private final int user_ratings_total;
    //contains the URL of the official Google page for this place. This will be the Google-owned page that contains the best available information about the place. Applications must link to or embed this page on any screen that shows detailed results about the place to the user.
    private final String url;
    //the authoritative website for this place, such as a business' homepage.
    private final String website;

    public Place(String icon, PlaceGeometry geometry, String name, PlaceHours opening_hours, PlacePhoto[] photos, String place_id, int price_level, double rating, String vicinity, String[] types,
                 AddressComponent[] address_components, String formatted_address, String formatted_phone_number, PlaceReview[] reviews, int user_ratings_total, String url, String website) {
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
        this.address_components = address_components;

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
        return null;
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

    @Override
    public String getShortDisplayAddress() {
        return vicinity != null ? vicinity : formatted_address;
    }

    @Override
    public String getLongDisplayAddress() {
        return formatted_address;
    }

    public String getName() {
        return name;
    }

    @Override
    public String[] getParking() {
        return null;
    }

    @Override
    public String[] getPaymentOptions() {
        return null;
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
            case 0: return "Free";
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
        return null;
    }

    @Override
    public String[] getRestaurantSpecialities() {
        return null;
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

    public String[] getTypes() {
        return types;
    }

    @Override
    public String toString() {
        return String.format("Name:%s, Geometry:%s, OpeningHours:%s, Price:%s, Rating:%f, Vicinity:%s, Address:%s, Phone:%s, Reviews:%s, Url:%s, Website:%s",
                name, geometry, opening_hours, price_level, rating, vicinity, formatted_address, formatted_phone_number, Arrays.toString(reviews), url, website);
    }

    /**
     * Address Component class
     */
    public static class AddressComponent {
        //full text description or name of the address component.
        private final String long_name;
        //abbreviated textual name for the address component, if available.
        private final String short_name;
        //array indicating the type of the address component.
        private final String[] types;

        public AddressComponent(String long_name, String short_name, String[] types) {
            this.long_name = long_name;
            this.short_name = short_name;
            this.types = types;
        }

        public String getLong_name() {
            return long_name;
        }

        public String getShort_name() {
            return short_name;
        }

        public String[] getTypes() {
            return types;
        }
    }
}
