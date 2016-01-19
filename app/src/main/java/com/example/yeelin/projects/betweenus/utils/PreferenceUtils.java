package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.data.LocalConstants;

/**
 * Created by ninjakiki on 1/13/16.
 */
public class PreferenceUtils {
    //logcat
    private static final String TAG = PreferenceUtils.class.getCanonicalName();

    private static final float MILE_TO_METERS = 1609.34f;
    private static final int KM_TO_METERS = 1000;

    //preference keys
    public static final String KEY_USE_METRIC = "use_metric";
    public static final String KEY_DATA_SOURCE = "data_source";
    public static final String KEY_SEARCH_TERM = "search_term";
    public static final String KEY_SEARCH_RADIUS = "search_radius";

    /**
     * Helper method that returns user's preference to use metric or imperial units
     * Read by Suggestions detail activity, Suggestions pager activity, Suggestions list fragment
     * @param context
     * @return
     */
    public static boolean useMetric(Context context) {
        boolean useMetric = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(KEY_USE_METRIC, false); //defaults to false if pref cannot be found
        Log.d(TAG, "useMetric: " + useMetric);
        return useMetric;
    }

    /**
     * Helper method that returns user's preference for data source.
     * Read by Suggestions activity, Suggestions pager activity, Suggestions detail activity
     * @param context
     * @return
     */
    @LocalConstants.DataSourceId
    public static int getPreferredDataSource(Context context) {
        String dataSource = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_DATA_SOURCE, context.getString(R.string.setting_default_data_source));
        Log.d(TAG, "getPreferredDataSource:" + dataSource);

        switch (dataSource) {
            case "Facebook": return LocalConstants.FACEBOOK;
            case "Yelp": return LocalConstants.YELP;
            case "Google": return LocalConstants.GOOGLE;
            default:
                return LocalConstants.FACEBOOK;
        }
    }

    public static String getPreferredDataSourceString(Context context) {
        String dataSource = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_DATA_SOURCE, context.getString(R.string.setting_default_data_source));
        Log.d(TAG, "getPreferredDataSource:" + dataSource);

        return dataSource;
    }

    /**
     * Helper method that returns the user's preference for types of places to search for.
     * Read by Location entry fragment
     * @param context
     * @return
     */
    public static String getPreferredSearchTerm(Context context) {
        String searchType = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_SEARCH_TERM, context.getString(R.string.setting_default_search_term));
        Log.d(TAG, "getPreferredSearchTerm:" + searchType);
        return searchType;
    }

    /**
     * Helper method that returns the user's preference for search radius in meters. Meters is used by
     * both Facebook and Yelp apis.
     * Read by Suggestions activity
     * @param context
     * @return
     */
    public static int getPreferredSearchRadiusInMeters(Context context) {
        int searchRadiusInt = getPreferredSearchRadius(context);

        //check if user is already preferring metric
        if (useMetric(context)) {
            //yes, the preference is to use metric, so convert search radius from km to meters
            return searchRadiusInt * KM_TO_METERS;
        }
        //no, the preference is not metric, so convert search radius from miles to meters
        return Math.round(searchRadiusInt * MILE_TO_METERS);
    }

    /**
     * Helper method to retrieve user's preference for search radius
     * @param context
     * @return
     */
    public static int getPreferredSearchRadius(Context context) {
        String searchRadius = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(KEY_SEARCH_RADIUS, context.getString(R.string.setting_default_search_radius));
        Log.d(TAG, "getPreferredSearchRadius:" + searchRadius);
        return Integer.valueOf(searchRadius);
    }

    /**
     * Helper method that returns the user's preference for search radius
     * Read by Suggestions activity
     * @param context
     * @return
     */
    public static int getPreferredSearchLimit(Context context) {
        return 20;
    }
}
