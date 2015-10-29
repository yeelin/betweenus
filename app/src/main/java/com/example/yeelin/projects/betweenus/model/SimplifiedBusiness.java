package com.example.yeelin.projects.betweenus.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.yelp.model.YelpBusiness;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ninjakiki on 8/19/15.
 */
public class SimplifiedBusiness implements Parcelable {
    private String id;
    private String name;
    private LatLng latLng;
    private String address;
    private String categories;
    private int reviews;
    private double rating;

    private String webUrl;
    private String imageUrl;
    private String ratingUrl;

    public static SimplifiedBusiness newInstance(@NonNull YelpBusiness business) {
        return new SimplifiedBusiness(business);
    }

    public static SimplifiedBusiness newInstance(@NonNull LocalBusiness business) {
        return new SimplifiedBusiness(business);
    }

    private SimplifiedBusiness(@NonNull YelpBusiness business) {
        id = business.getId();
        name = business.getName();
        latLng = new LatLng(business.getLocation().getCoordinate().getLatitude(), business.getLocation().getCoordinate().getLongitude());
        address = business.getLocation().getShortDisplayAddress();
        categories = business.getDisplayCategories();
        reviews = business.getReview_count();
        rating = business.getRating();

        webUrl = business.getMobile_url();
        imageUrl = business.getImage_url();
        ratingUrl = business.getRating_img_url_large();
    }

    private SimplifiedBusiness(@NonNull LocalBusiness business) {
        id = business.getId();
        name = business.getName();
        latLng = new LatLng(business.getLocalBusinessLocation().getLatLng().latitude, business.getLocalBusinessLocation().getLatLng().longitude);
        address = business.getLocalBusinessLocation().getShortDisplayAddress();
        categories = business.getCategory();
        reviews = business.getReviewCount();
        rating = business.getRating();

        webUrl = business.getMobileUrl();
        imageUrl = business.getImageUrl();
        ratingUrl = business.getRatingImageUrl();
    }

    /**
     * This will be used only by the creator to reconstruct from the Parcel
     * @param in
     */
    protected SimplifiedBusiness(Parcel in) {
        id = in.readString();
        name = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        address = in.readString();
        categories = in.readString();
        reviews = in.readInt();
        rating = in.readDouble();

        webUrl = in.readString();
        imageUrl = in.readString();
        ratingUrl = in.readString();
    }

    /**
     * This is required for deserializing data stored in Parcel
     */
    public static final Creator<SimplifiedBusiness> CREATOR = new Creator<SimplifiedBusiness>() {
        @Override
        public SimplifiedBusiness createFromParcel(Parcel in) {
            return new SimplifiedBusiness(in);
        }

        @Override
        public SimplifiedBusiness[] newArray(int size) {
            return new SimplifiedBusiness[size];
        }
    };

    /**
     * Describes the contents of the object being parcelled.
     * @return
     */
    @Override
    public int describeContents() {
        return hashCode();
    }

    /**
     * Actual object serialization happens here. Each element of the object is
     * individually parcelled.
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeParcelable(latLng, flags);
        dest.writeString(address);
        dest.writeString(categories);
        dest.writeInt(reviews);
        dest.writeDouble(rating);

        dest.writeString(webUrl);
        dest.writeString(imageUrl);
        dest.writeString(ratingUrl);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getAddress() {
        return address;
    }

    public String getCategories() {
        return categories;
    }

    public int getReviews() {
        return reviews;
    }

    public double getRating() {
        return rating;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRatingUrl() {
        return ratingUrl;
    }
}
