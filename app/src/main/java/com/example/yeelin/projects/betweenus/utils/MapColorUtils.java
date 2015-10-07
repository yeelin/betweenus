package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;

import com.example.yeelin.projects.betweenus.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.maps.android.ui.IconGenerator;

/**
 * Created by ninjakiki on 8/18/15.
 */
public class MapColorUtils {
    private static final String TAG = MapColorUtils.class.getCanonicalName();
    //constants
    private static final int NUM_CATEGORIES = 11;
    public static final int COLOR_GRAY_500_OPACITY_40 = Color.argb(102, 158, 158, 158); //40% opaque means 0.4*255=102, 102 in Hex is 66

    private static IconGenerator iconGenerator;

    private static BitmapDescriptor unselectedMarkerBitmap;
    private static BitmapDescriptor selectedMarkerBitmap;
    private static BitmapDescriptor userMarkerBitmap;
    private static BitmapDescriptor friendMarkerBitmap;
    private static SparseArray<BitmapDescriptor> unselectedMarkerMap;

    /**
     * Returns a bitmap descriptor for the unselected marker state
     * @param context
     * @return
     */
    public static BitmapDescriptor getUnselectedMarkerBitmap(Context context) {
        if (unselectedMarkerBitmap == null) {
            float[] hsvPrimaryDark = new float[3];
            Color.colorToHSV(ContextCompat.getColor(context, R.color.colorPrimaryDark), hsvPrimaryDark);
            unselectedMarkerBitmap = BitmapDescriptorFactory.defaultMarker(hsvPrimaryDark[0]);
        }
        return unselectedMarkerBitmap;
    }

    /**
     * Returns a bitmap descriptor for the selected marker state
     * @param context
     * @return
     */
    public static BitmapDescriptor getSelectedMarkerBitmap(Context context) {
        if (selectedMarkerBitmap == null) {
//            float[] hsvAccentDark = new float[3];
//            Color.colorToHSV(ContextCompat.getColor(context, R.color.colorAccentDark), hsvAccentDark);
//            selectedMarkerBitmap = BitmapDescriptorFactory.defaultMarker(hsvAccentDark[0]);
            selectedMarkerBitmap = BitmapDescriptorFactory.defaultMarker();
        }
        return selectedMarkerBitmap;
    }

    /**
     * Returns the correct hue based on the given toggle state
     * @param context
     * @param toggleState
     * @return
     */
    public static BitmapDescriptor determineMarkerIcon(Context context, boolean toggleState) {
        return toggleState ?
                getSelectedMarkerBitmap(context) :
                getUnselectedMarkerBitmap(context);
    }

    /**
     * Returns the correct hue based on the given toggle state and rating
     * @param context
     * @param toggleState
     * @param rating
     * @return
     */
    public static BitmapDescriptor determineMarkerIcon(Context context, boolean toggleState, double rating) {
        return toggleState ?
                getSelectedMarkerBitmap(context) :
                getUnselectedMarkerBitmap(context, rating);
    }

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
     * Given a rating, this method tries to find the corresponding bitmap descriptor in the map.
     * If one exists, it is returned. Otherwise, a new one is created, put in the map, and returned.
     *
     * @param context
     * @param rating
     * @return
     */
    public static BitmapDescriptor getUnselectedMarkerBitmap(Context context, double rating) {
        final int integerRating = (int) (Math.round(rating*10));

        //lazy initialization of unselected marker map
        if (unselectedMarkerMap == null) unselectedMarkerMap = new SparseArray<>(NUM_CATEGORIES);

        //see if the bitmap descriptor already exists for the given rating
        //if it does, return it
        //otherwise, create one, put in map, and then return it
        BitmapDescriptor bitmapDescriptor = unselectedMarkerMap.get(integerRating);
        if (bitmapDescriptor == null) {
            bitmapDescriptor = createNewMarkerViaHueChange(integerRating);
            unselectedMarkerMap.put(integerRating, bitmapDescriptor);
        }
        return bitmapDescriptor;
    }

    /**
     * Creates a new bitmap descriptor given a rating
     * @param integerRating
     * @return
     */
    private static BitmapDescriptor createNewMarkerViaHueChange(int integerRating) {
        float hue = 0;
        switch (integerRating) {
            //0 stars
            case 0:
                hue = 55; //green
                break;
            case 5:
                hue = 75;
                break;

            //1 star
            case 10:
                hue = 95;
                break;
            case 15:
                hue = 115;
                break;

            //2 stars
            case 20:
                hue = 135;
                break;
            case 25:
                hue = 155;
                break;

            //3 stars
            case 30:
                hue = 175;
                break;
            case 35:
                hue = 195;
                break;

            //4 stars
            case 40:
                hue = 215;
                break;
            case 45:
                hue = 235;
                break;

            //5 stars
            case 50:
                hue = 255; //purple
                break;
        }
        return BitmapDescriptorFactory.defaultMarker(hue);
    }

    /**
     * Creates a bitmap based on rating using the saturation change method
     * @param context
     * @param integerRating
     * @return
     */
    @Deprecated
    private static BitmapDescriptor createNewMarkerViaSaturationChange (Context context, int integerRating) {
        Log.d(TAG, String.format("createNewMarkerViaSaturationChange: Rating: %d, Saturation: %f", integerRating, integerRating/100f));

        final ColorMatrix desatMatrix = new ColorMatrix();
        desatMatrix.setSaturation(integerRating / 100f);
        final ColorFilter colorFilter = new ColorMatrixColorFilter(desatMatrix);

        final Paint paint = new Paint();
        paint.setColorFilter(colorFilter);

        Bitmap immutableBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_action_maps_place);
        Bitmap mutableBitmap = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawBitmap(immutableBitmap, 0, 0, paint);

        return BitmapDescriptorFactory.fromBitmap(mutableBitmap);
    }

    /**
     * Creates a bitmap based on rating using the different resource method.
     * @param context
     * @param integerRating
     * @return
     */
    @Deprecated
    private static BitmapDescriptor createNewMarkerFromResource(Context context, int integerRating) {
//        switch (integerRating) {
//            case 0:
//            case 5:
//                return BitmapDescriptorFactory.fromResource(R.drawable.ic_place_0_0);
//            case 10:
//            case 15:
//                return BitmapDescriptorFactory.fromResource(R.drawable.ic_place_1_0);
//            case 20:
//            case 25:
//                return BitmapDescriptorFactory.fromResource(R.drawable.ic_place_2_0);
//            case 30:
//            case 35:
//                return BitmapDescriptorFactory.fromResource(R.drawable.ic_place_3_0);
//            case 40:
//            case 45:
//                return BitmapDescriptorFactory.fromResource(R.drawable.ic_place_4_0);
//            default:
//                return BitmapDescriptorFactory.fromResource(R.drawable.ic_place_5_0);
//        }
        return null;
    }
}
