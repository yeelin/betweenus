package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;

import com.example.yeelin.projects.betweenus.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.ui.IconGenerator;

/**
 * Created by ninjakiki on 8/18/15.
 */
public class MapColorUtils {
    //constants
    public static final int NUM_RATING_BUCKETS = 11;
    private static IconGenerator iconGenerator;

    private static BitmapDescriptor selectedMarkerBitmap;
    private static BitmapDescriptor userMarkerBitmap;
    private static BitmapDescriptor friendMarkerBitmap;
    private static SparseArray<BitmapDescriptor> unselectedMarkerMap;

    /**
     * Returns the user marker
     * @param context
     * @return
     */
    public static BitmapDescriptor getUserMarkerIcon(Context context) {
        if (userMarkerBitmap == null) {
            if (iconGenerator == null) initIconGenerator(context);
            userMarkerBitmap = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(context.getString(R.string.map_marker_you)));
        }
        return userMarkerBitmap;
    }

    /**
     * Returns the friend marker
     * @param context
     * @return
     */
    public static BitmapDescriptor getFriendMarkerIcon(Context context) {
        if (friendMarkerBitmap == null) {
            if (iconGenerator == null) initIconGenerator(context);
            friendMarkerBitmap = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(context.getString(R.string.map_marker_friend)));
        }
        return friendMarkerBitmap;
    }

    /**
     * Helper method that inits the icon generator for user and friend markers
     * @param context
     */
    private static void initIconGenerator(Context context) {
        iconGenerator = new IconGenerator(context);
        iconGenerator.setStyle(IconGenerator.STYLE_GREEN);
        iconGenerator.setColor(ContextCompat.getColor(context, R.color.colorAccent));
    }

    /**
     * Returns the BitmapDescriptor with the correct hue based on the given toggle state and rating
     * @param toggleState
     * @param rating
     * @return
     */
    public static BitmapDescriptor determineMarkerIcon(boolean toggleState, double rating) {
        return toggleState ? getSelectedMarkerBitmap() : getUnselectedMarkerBitmap(rating);
    }

    /**
     * Returns the BitmapDescriptor with the correct hue for the SELECTED marker state
     * @return
     */
    private static BitmapDescriptor getSelectedMarkerBitmap() {
        if (selectedMarkerBitmap == null) {
            selectedMarkerBitmap = BitmapDescriptorFactory.defaultMarker();
        }
        return selectedMarkerBitmap;
    }

    /**
     * Given a rating, this method returns the BitmapDescriptor with the correct hue for the UNSELECTED
     * marker state.
     * First, it tries to find the corresponding bitmap descriptor in the map.
     * If one exists, it is returned. Otherwise, a new one is created, put in the map, and returned.
     *
     * @param rating a value in the range [0.0, 0.5, 1.0, ..., 4.5, 5.0] inclusive
     * @return
     */
    private static BitmapDescriptor getUnselectedMarkerBitmap(double rating) {
        //lazy initialization of unselected marker map
        if (unselectedMarkerMap == null) unselectedMarkerMap = new SparseArray<>(NUM_RATING_BUCKETS);

        //multiply rating by 10 to get values in [0, 5, 10, ..., 45, 50] so that we can switch on the result
        int integerRating = (int) (10*rating);

        //see if the bitmap descriptor already exists for the given rating
        //if it does, return it
        //otherwise, create one, put in map, and then return it
        BitmapDescriptor bitmapDescriptor = unselectedMarkerMap.get(integerRating);
        if (bitmapDescriptor == null) {
            bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(lookupHue(integerRating));
            unselectedMarkerMap.put(integerRating, bitmapDescriptor);
        }
        return bitmapDescriptor;
    }

    /**
     * Returns the correct hue given an integerRating whose value is in the range [0, 5, 10, ..., 45, 50] inclusive
     * @param integerRating
     * @return
     */
    private static float lookupHue(int integerRating) {

        switch (integerRating) {
            //0 stars
            case 0: return 55; //green
            case 5: return 75;

            //1 star
            case 10: return 95;
            case 15: return 115;

            //2 stars
            case 20: return 135;
            case 25: return 155;

            //3 stars
            case 30: return 175;
            case 35: return 195;

            //4 stars
            case 40: return  215;
            case 45: return 235;

            //5 stars
            case 50: return 255; //purple

            //shouldn't happen
            default:
                return 55; //same hue as 0 stars
        }
    }
}
