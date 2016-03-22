package com.example.yeelin.projects.betweenus.data.generic.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 8/19/15.
 */
public class SimplifiedBusiness implements Parcelable {
    //member variables
    private String id;
    private String name;
    private LatLng latLng;
    private String address;
    private String[] categoryList;
    private int reviews;
    private double rating;
    private int likes;
    private double normalizedLikes;
    private int checkins;
//    private int priceRange;
    private int dataSource;

    private String webUrl;
    private String fbUrl;
    private String profilePictureUrl;
    private String ratingImageUrl;

    /**
     * Creates a new SimplifiedBusiness given a LocalBusiness.
     * @param business
     * @return
     */
    public static SimplifiedBusiness newInstance(@NonNull LocalBusiness business) {
        return new SimplifiedBusiness(business);
    }
    /**
     * Helper method to build the simplified business items array list for marshalling across to the
     * detail pager activity.
     * @return
     */
    public static ArrayList<SimplifiedBusiness> buildSimplifiedBusinessList(ArrayList<LocalResult> localResultArrayList) {

        ArrayList<SimplifiedBusiness> simplifiedBusinesses = new ArrayList<>();

        for (int i=0; i<localResultArrayList.size(); i++) {
            final LocalResult localResult = localResultArrayList.get(i);

            for (int j=0; j<localResult.getLocalBusinesses().size(); j++) {
                final LocalBusiness localBusiness = localResult.getLocalBusinesses().get(j);
                simplifiedBusinesses.add(SimplifiedBusiness.newInstance(localBusiness));
            }
        }
        return simplifiedBusinesses;
    }

    /**
     * Helper method to build the selected items array list for marshalling across to the
     * invitation activity.
     * TODO: See if we can change selectedIdsMap to HashMap<String,LocalBusiness>
     * @return
     */
    public static ArrayList<SimplifiedBusiness> buildSelectedItemsList(ArrayList<LocalResult> localResultArrayList,
                                                                       ArrayMap<String,Integer> selectedIdsMap) {

        ArrayList<SimplifiedBusiness> selectedItems = new ArrayList<>(selectedIdsMap.size());

        for (int i=0; i<localResultArrayList.size(); i++) {
            final LocalResult localResult = localResultArrayList.get(i);

            for (int j=0; j<localResult.getLocalBusinesses().size(); j++) {
                final LocalBusiness localBusiness = localResult.getLocalBusinesses().get(j);
                if (selectedIdsMap.containsKey(localBusiness.getId())) {
                    selectedItems.add(SimplifiedBusiness.newInstance(localBusiness));
                }
            }
        }
        return selectedItems;
    }

    /**
     * Private constructor
     * @param business
     */
    private SimplifiedBusiness(@NonNull LocalBusiness business) {
        id = business.getId();
        name = business.getName();
        latLng = new LatLng(business.getLocalBusinessLocation().getLatLng().latitude, business.getLocalBusinessLocation().getLatLng().longitude);
        address = business.getLocalBusinessLocation().getShortDisplayAddress();
        categoryList = business.getCategoryList();
        reviews = business.getReviewCount();
        rating = business.getRating();
        likes = business.getLikes();
        normalizedLikes = business.getNormalizedLikes();
        checkins = business.getCheckins();
//        priceRange = business.getPriceRange();
        dataSource = business.getDataSource();

        webUrl = business.getMobileUrl();
        fbUrl = business.getFbLink();
        profilePictureUrl = business.getProfilePictureUrl();
        ratingImageUrl = business.getRatingImageUrl();
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
        categoryList = in.createStringArray();
        //in.readStringArray(categoryList);
        reviews = in.readInt();
        rating = in.readDouble();
        likes = in.readInt();
        normalizedLikes = in.readDouble();
        checkins = in.readInt();
//        priceRange = in.readInt();
        dataSource = in.readInt();

        webUrl = in.readString();
        fbUrl = in.readString();
        profilePictureUrl = in.readString();
        ratingImageUrl = in.readString();
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
        dest.writeStringArray(categoryList);
        dest.writeInt(reviews);
        dest.writeDouble(rating);
        dest.writeInt(likes);
        dest.writeDouble(normalizedLikes);
        dest.writeInt(checkins);
//        dest.writeInt(priceRange);
        dest.writeInt(dataSource);

        dest.writeString(webUrl);
        dest.writeString(fbUrl);
        dest.writeString(profilePictureUrl);
        dest.writeString(ratingImageUrl);
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

    public String[] getCategoryList() {
        return categoryList;
    }

    public int getReviews() {
        return reviews;
    }

    public double getRating() {
        return rating;
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

    public String getWebUrl() {
        return webUrl;
    }

    public String getFbUrl() {
        return fbUrl;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getRatingImageUrl() {
        return ratingImageUrl;
    }

//    public int getPriceRange() {
//        return priceRange;
//    }

    public int getDataSource() {
        return dataSource;
    }
}
