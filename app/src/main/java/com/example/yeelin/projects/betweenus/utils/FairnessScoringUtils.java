package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yeelin.projects.betweenus.R;
import com.google.android.gms.maps.model.LatLng;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.DecimalFormat;

/**
 * Created by ninjakiki on 9/24/15.
 */
public class FairnessScoringUtils {
    //logcat
    private static final String TAG = FairnessScoringUtils.class.getCanonicalName();

    //constants
    private static final double ONE_KM_IN_METERS = 1000.0;

    @IntDef({EQUIDISTANT, CLOSER_TO_USER, CLOSER_TO_FRIEND})
    @Retention(RetentionPolicy.SOURCE) //tell compiler not to store annotation in .class files
    public @interface RelativeDistanceId {} //declare the RelativeDistanceId
    public static final int EQUIDISTANT = 0; //declare actual constants
    public static final int CLOSER_TO_USER = 1;
    public static final int CLOSER_TO_FRIEND = 2;

    /**
     * Compares the distance between the user and place with the distance between the friend and place.
     * Returns constants indicating if the place is closer to user, closer to friend, or equidistant.
     * @param userLatLng
     * @param friendLatLng
     * @param businessLatLng
     * @return
     */
    public static @RelativeDistanceId int computeFairnessScore(@NonNull LatLng userLatLng, @NonNull LatLng friendLatLng, @NonNull LatLng businessLatLng) {
        //compute distance between 1) user and place, 2) friend and place
        double userDistance = LocationUtils.computeDistanceBetween(userLatLng, businessLatLng);
        double friendDistance = LocationUtils.computeDistanceBetween(friendLatLng, businessLatLng);

        //compare the distance and return fairness score
        if (userDistance < friendDistance) {
            //Log.d(TAG, String.format("computeFairnessScore: CLOSER_TO_USER. UserDistance:%.2f, FriendDistance:%.2f", userDistance, friendDistance));
            return CLOSER_TO_USER;
        }
        if (userDistance > friendDistance) {
            //Log.d(TAG, String.format("computeFairnessScore: CLOSER_TO_FRIEND. UserDistance:%.2f, FriendDistance:%.2f", userDistance, friendDistance));
            return CLOSER_TO_FRIEND;
        }
        //Log.d(TAG, String.format("computeFairnessScore: EQUIDISTANT. UserDistance:%.2f, FriendDistance:%.2f", userDistance, friendDistance));
        return EQUIDISTANT;
    }

    /**
     * Computes the distance between the place and midpoint. Returns distance in the preferred unit.
     * @param businessLatLng
     * @param midLatLng
     * @param useMetric
     * @return
     */
    public static double computeDistanceDelta(@NonNull LatLng businessLatLng, @NonNull LatLng midLatLng, boolean useMetric) {
        //compute distance between place and mid point
        double distanceInMeters = LocationUtils.computeDistanceBetween(businessLatLng, midLatLng);

        //return distance in meters or miles depending on user's unit preference
        if (useMetric) return distanceInMeters;
        return LocationUtils.convertMetersToMiles(distanceInMeters);
    }

    /**
     * Returns a formatted a string which comprises of 1) distance between the place and midpoint, and 2) fairness score.
     * @param context
     * @param distanceFromMidPoint
     * @param fairness
     * @param useMetric
     * @param verbose
     * @return
     */
    public static String formatDistanceDeltaAndFairness(Context context,
                                                        double distanceFromMidPoint, @RelativeDistanceId int fairness,
                                                        boolean useMetric, boolean verbose) {
        return String.format("%s (%s)",
                formatDistanceDelta(context, distanceFromMidPoint, useMetric, verbose),
                formatFairnessScore(context, fairness));
    }

    /**
     * Returns a formatted a string describing the distance between the place and midpoint. The verbose boolean
     * determines the verbosity of the string.
     * @param context
     * @param distanceFromMidPoint
     * @param useMetric
     * @param verbose
     * @return
     */
    public static String formatDistanceDelta(Context context,
                                             double distanceFromMidPoint,
                                             boolean useMetric, boolean verbose) {
        //get reference to a decimal formatter
        final DecimalFormat decimalFormatter = FormattingUtils.getDecimalFormatter(1);

        //build the distance between place and midpoint
        if (!useMetric) //use imperial
            return context.getString(verbose ? R.string.detail_distance_from_midPoint_miles : R.string.item_distance_from_midPoint_miles,
                    decimalFormatter.format(distanceFromMidPoint));

        //use metric
        if (distanceFromMidPoint > ONE_KM_IN_METERS) { //if using metric and distance is greater than 1000m, then show in km
            return context.getString(verbose ? R.string.detail_distance_from_midPoint_km : R.string.item_distance_from_midPoint_km,
                    decimalFormatter.format(distanceFromMidPoint / ONE_KM_IN_METERS));
        }
        else {
            return context.getString(verbose ? R.string.detail_distance_from_midPoint_meters : R.string.item_distance_from_midPoint_meters,
                    decimalFormatter.format(distanceFromMidPoint));
        }

    }

    /**
     * Returns a formatted string which tells the user if the place is closer to the user or friend or equidistant.
     * @param context
     * @param fairness
     * @return
     */
    public static String formatFairnessScore(Context context, @RelativeDistanceId int fairness) {
        switch (fairness) {
            case CLOSER_TO_USER:
                return context.getString(R.string.detail_closer_to_user);

            case CLOSER_TO_FRIEND:
                return context.getString(R.string.detail_closer_to_friend);

            default:
                return context.getString(R.string.detail_equidistant);
        }
    }
}
