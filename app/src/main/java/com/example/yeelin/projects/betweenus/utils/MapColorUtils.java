package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.graphics.Color;

import com.example.yeelin.projects.betweenus.R;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by ninjakiki on 8/18/15.
 */
public class MapColorUtils {
    //constants
    public static final int COLOR_GRAY_500_OPACITY_40 = Color.argb(102, 158, 158, 158); //40% opaque means 0.4*255=102, 102 in Hex is 66

    private static float[] hsvPrimaryDark;
    private static float[] hsvAccentDark;

    private static BitmapDescriptor unselectedMarkerBitmap;
    private static BitmapDescriptor selectedMarkerBitmap;


    /**
     * Returns the primary dark color in hue
     * @param context
     * @return
     */
    public static float getPrimaryDarkHue(Context context) {
        if (hsvPrimaryDark == null) {
            hsvPrimaryDark = new float[3];
            //read the resources to convert primary color from HEX to HSV
            Color.colorToHSV(context.getResources().getColor(R.color.colorPrimaryDark), hsvPrimaryDark);
        }
        return hsvPrimaryDark[0];
    }

    /**
     * Returns the accent dark color in hue
     * @param context
     * @return
     */
    public static float getAccentDarkHue(Context context) {
        if (hsvAccentDark == null) {
            hsvAccentDark = new float[3];
            Color.colorToHSV(context.getResources().getColor(R.color.colorAccentDark), hsvAccentDark);
        }
        return hsvAccentDark[0];
    }

    /**
     * Returns a bitmap descriptor for the unselected marker state
     * @param context
     * @return
     */
    public static BitmapDescriptor getUnselectedMarkerBitmap(Context context) {
        if (unselectedMarkerBitmap == null) {
            unselectedMarkerBitmap = BitmapDescriptorFactory.defaultMarker(getPrimaryDarkHue(context));
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
            selectedMarkerBitmap = BitmapDescriptorFactory.defaultMarker(getAccentDarkHue(context));
        }
        return selectedMarkerBitmap;
    }

    /**
     * Method that returns the correct hue based on the given toggle state
     * @param context
     * @param toggleState
     * @return
     */
    public static BitmapDescriptor determineMarkerIcon(Context context, boolean toggleState) {
        return toggleState ? getSelectedMarkerBitmap(context) : getUnselectedMarkerBitmap(context);
    }
}
