package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yeelin.projects.betweenus.R;
import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;

/**
 * Created by ninjakiki on 9/24/15.
 */
public class FairnessScoringUtils {
    //logcat
    private static final String TAG = FairnessScoringUtils.class.getCanonicalName();

    //constants
    public static final int EQUIDISTANT = 0;
    public static final int CLOSER_TO_USER = 1;
    public static final int CLOSER_TO_FRIEND = 2;

    public static final int IMPERIAL = 0;
    public static final int METRIC = 1;

    /**
     * Compares the distance between the user and place with the distance between the friend and place.
     * Returns constants indicating if the place is closer to user, closer to friend, or equidistant.
     * @param userLatLng
     * @param friendLatLng
     * @param businessLatLng
     * @return
     */
    public static int computeFairnessScore(@NonNull LatLng userLatLng, @NonNull LatLng friendLatLng, @NonNull LatLng businessLatLng) {
        //compute distance between 1) user and place, 2) friend and place
        double userDistance = LocationUtils.computeDistanceBetween(userLatLng, businessLatLng);
        double friendDistance = LocationUtils.computeDistanceBetween(friendLatLng, businessLatLng);

        //compare the distance and return fairness score
        if (userDistance < friendDistance) {
            Log.d(TAG, String.format("computeFairnessScore: CLOSER_TO_USER. UserDistance:%.2f, FriendDistance:%.2f", userDistance, friendDistance));
            return CLOSER_TO_USER;
        }
        if (userDistance > friendDistance) {
            Log.d(TAG, String.format("computeFairnessScore: CLOSER_TO_FRIEND. UserDistance:%.2f, FriendDistance:%.2f", userDistance, friendDistance));
            return CLOSER_TO_FRIEND;
        }
        Log.d(TAG, String.format("computeFairnessScore: EQUIDISTANT. UserDistance:%.2f, FriendDistance:%.2f", userDistance, friendDistance));
        return EQUIDISTANT;
    }

    /**
     * Computes the distance between the place and midpoint. Returns distance in the preferred unit.
     * @param businessLatLng
     * @param midLatLng
     * @param unitPreference
     * @return
     */
    public static double computeDistanceFromMidPoint(@NonNull LatLng businessLatLng, @NonNull LatLng midLatLng, int unitPreference) {
        //compute distance between place and mid point
        double distanceInMeters = LocationUtils.computeDistanceBetween(businessLatLng, midLatLng);

        //return distance in meters or miles
        if (unitPreference == IMPERIAL) {
            double distanceInMiles = LocationUtils.convertMetersToMiles(distanceInMeters);
            Log.d(TAG, String.format("computeDistanceFromMidPoint: Distance(m):%.2f, Distance(mi):%.2f", distanceInMeters, distanceInMiles));
            return distanceInMiles;
        }
        Log.d(TAG, "computeDistanceFromMidPoint: Distance(m):" + distanceInMeters);
        return distanceInMeters;
    }

    /**
     * Formats a string which comprises of 1) distance between the place and midpoint, and 2) fairness score.
     * @param context
     * @param distanceFromMidPoint
     * @param fairness
     * @param unitPreference
     * @return
     */
    public static String formatDistanceFromMidPointString(Context context, double distanceFromMidPoint, int fairness, int unitPreference) {
        //get reference to a decimal formatter
        final DecimalFormat decimalFormatter = FormattingUtils.getDecimalFormatter();

        //build string
        StringBuilder stringBuilder = new StringBuilder();

        //build the distance between place and midpoint
        if (unitPreference == IMPERIAL) {
            stringBuilder.append(context.getString(R.string.detail_distance_from_midPoint_miles,
                    decimalFormatter.format(distanceFromMidPoint)));
        }
        else if (unitPreference == METRIC) {
            if (distanceFromMidPoint > 1000) { //if using metric and distance is greater than 1000m, then show in km
                stringBuilder.append(context.getString(R.string.detail_distance_from_midPoint_km,
                        decimalFormatter.format(distanceFromMidPoint / 1000.0)));
            }
            else {
                stringBuilder.append(context.getString(R.string.detail_distance_from_midPoint_meters,
                        decimalFormatter.format(distanceFromMidPoint)));
            }
        }

        //append fairness score i.e. text that says whether the place is closer to user or friend
        stringBuilder.append(" ");
        switch (fairness) {
            case CLOSER_TO_USER:
                stringBuilder.append(context.getString(R.string.detail_closer_to_user));
                break;
            case CLOSER_TO_FRIEND:
                stringBuilder.append(context.getString(R.string.detail_closer_to_friend));
                break;
            default:
                stringBuilder.append(context.getString(R.string.detail_equidistant));
                break;
        }

        //done
        return stringBuilder.toString();
    }
}
