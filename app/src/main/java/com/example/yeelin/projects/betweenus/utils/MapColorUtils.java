package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.graphics.Color;

import com.example.yeelin.projects.betweenus.R;

/**
 * Created by ninjakiki on 8/18/15.
 */
public class MapColorUtils {
    private float[] hsvPrimaryDark = new float[3];
    private float[] hsvAccentDark = new float[3];
    private static MapColorUtils instance;

    public static MapColorUtils getInstance(Context context) {
        if (instance == null) {
            instance = new MapColorUtils(context);
        }
        return instance;
    }

    private MapColorUtils(Context context) {
        //read the resources to convert primary and accent colors from HEX to HSV
        Color.colorToHSV(context.getResources().getColor(R.color.colorPrimaryDark), hsvPrimaryDark);
        Color.colorToHSV(context.getResources().getColor(R.color.colorAccentDark), hsvAccentDark);
    }

    public float getPrimaryDarkHue() {
        return hsvPrimaryDark[0];
    }

    public float getAccentDarkHue() {
        return hsvAccentDark[0];
    }
}
