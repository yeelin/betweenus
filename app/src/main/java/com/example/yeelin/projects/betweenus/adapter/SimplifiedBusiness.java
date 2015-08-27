package com.example.yeelin.projects.betweenus.adapter;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.model.YelpBusiness;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ninjakiki on 8/19/15.
 */
public class SimplifiedBusiness implements Parcelable {
    private String id;
    private String name;
    private String address;
    private String categories;
    private int reviews;

    private String imageUrl;
    private String ratingUrl;

    //unused by UI at the moment
    private LatLng latLng;

    public static SimplifiedBusiness newInstance(Context context, @NonNull YelpBusiness business) {
        return new SimplifiedBusiness(context, business);
    }

    private SimplifiedBusiness(Context context, @NonNull YelpBusiness business) {
        id = business.getId();
        name = business.getName();
        address = context.getString(R.string.list_item_short_address, business.getLocation().getAddress()[0], business.getLocation().getCity());
        categories = business.getDisplayCategories();
        reviews = business.getReview_count();
        imageUrl = business.getImage_url();
        ratingUrl = business.getRating_img_url_large();
        latLng = new LatLng(business.getLocation().getCoordinate().getLatitude(), business.getLocation().getCoordinate().getLatitude());
    }

    /**
     * This will be used only by the creator to reconstruct from the Parcel
     * @param in
     */
    protected SimplifiedBusiness(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        categories = in.readString();
        reviews = in.readInt();

        imageUrl = in.readString();
        ratingUrl = in.readString();

        latLng = in.readParcelable(LatLng.class.getClassLoader());
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
        dest.writeString(address);
        dest.writeString(categories);
        dest.writeInt(reviews);

        dest.writeString(imageUrl);
        dest.writeString(ratingUrl);

        dest.writeParcelable(latLng, flags);
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRatingUrl() {
        return ratingUrl;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
