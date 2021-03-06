package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

/**
 * Created by ninjakiki on 7/20/15.
 */
public class ConnectivityUtils {
    /**
     * Retrieve the current connected network
     * @param context The context to use for retrieving the connected network
     * @return The current connected network (isConnected is true) or null if no network is active and connected
     */
    @Nullable
    public static NetworkInfo getConnectedNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) { //isConnected means really connected
            return activeNetwork;
        }

        return null;
    }

    /**
     * Tells you if the device is connected
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        return getConnectedNetwork(context) != null;
    }

    /**
     * Tells you if the device is connected to Wifi
     * @param context
     * @return
     */
    public static boolean isConnectedWifi(Context context) {
        NetworkInfo activeNetwork = getConnectedNetwork(context);
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Tells you if the connection is free or paid.
     * @param context
     * @return
     */
    public static boolean isConnectionMetered(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.isActiveNetworkMetered();
    }

    /**
     * Tells you if the device is not connected
     * @param context
     * @return
     */
    public static boolean isNotConnected(Context context) {
        return getConnectedNetwork(context) == null;
    }
}