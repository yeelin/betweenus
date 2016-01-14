package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by ninjakiki on 1/13/16.
 */
public class PreferenceUtils {
    private static final String TAG = PreferenceUtils.class.getCanonicalName();
    private static final String USE_METRIC = "use_metric";

    /**
     * Helper method that checks user's preference to use metric
     * @param context
     * @return
     */
    public static boolean useMetric(Context context) {
        boolean useMetric = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(USE_METRIC, false);
        Log.d(TAG, "useMetric: " + useMetric);
        return useMetric;
    }
}
