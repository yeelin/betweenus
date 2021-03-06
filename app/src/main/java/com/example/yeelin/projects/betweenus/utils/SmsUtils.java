package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.generic.model.SimplifiedBusiness;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 10/8/15.
 */
public class SmsUtils {
    //logcat
    private static final String TAG = SmsUtils.class.getCanonicalName();

    /**
     * Returns the body of the SMS
     * @param context
     * @param name
     * @param selectedItems
     * @return
     */
    public static String buildBody(Context context, @Nullable String name, final ArrayList<SimplifiedBusiness> selectedItems) {
        String salutation = name == null ? context.getString(R.string.sms_hello_no_name) : context.getString(R.string.sms_hello_with_name, name);
        String bodyMiddle = buildSelectedItemsString(selectedItems);
        Log.d(TAG, "buildBody: " + bodyMiddle);
        return salutation
                + "\n" +
                context.getResources().getQuantityString(R.plurals.sms_body, selectedItems.size())
                + "\n" +
                bodyMiddle;
    }

    /**
     * Helper method that builds a string for the arraylist of selected items
     * @param selectedItems
     * @return
     */
    private static String buildSelectedItemsString(final ArrayList<SimplifiedBusiness> selectedItems) {
        final StringBuilder builder = new StringBuilder();

        for (int i=0; i<selectedItems.size(); i++) {
            builder.append(buildSelectedItemString(selectedItems.get(i)));
            if (i < selectedItems.size()-1) builder.append(", ");
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * Helper method that builds a string for a selected item
     * FB format: Name (X likes, Y checkins, Z price, URL)
     * Yelp format: Name (X stars, Y reviews, URL)
     *
     * @param selectedItem
     * @return
     */
    private static String buildSelectedItemString(final SimplifiedBusiness selectedItem) {
        switch (selectedItem.getDataSource()) {
            case LocalConstants.FACEBOOK:
                return String.format("%s (%s likes, %d checkins, %s)",
                        selectedItem.getName(),
                        selectedItem.getLikes(),
                        selectedItem.getCheckins(),
                        selectedItem.getFbUrl());

            case LocalConstants.YELP:
                return String.format("%s (%s stars, %d reviews, %s)",
                        selectedItem.getName(),
                        FormattingUtils.getDecimalFormatterNoRounding(1).format(selectedItem.getRating()),
                        selectedItem.getReviews(),
                        selectedItem.getWebUrl());

            case LocalConstants.GOOGLE:
                return String.format("%s (%s stars, %s)",
                        selectedItem.getName(),
                        FormattingUtils.getDecimalFormatterNoRounding(1).format(selectedItem.getRating()),
                        selectedItem.getAddress());

            default:
                return "No data due to unkown data source";
        }
    }
}
