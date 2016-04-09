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
public class EmailUtils {
    //logcat
    private static final String TAG = EmailUtils.class.getCanonicalName();

    /**
     * Returns the body of the email
     * @param context
     * @param name
     * @param selectedItems
     * @return
     */
    public static String buildBody(Context context, @Nullable String name, final ArrayList<SimplifiedBusiness> selectedItems) {
        String salutation = name == null ? context.getString(R.string.email_hello_no_name) : context.getString(R.string.sms_hello_with_name, name);
        String bodyMiddle = buildSelectedItemsString(selectedItems);
        Log.d(TAG, "buildBody: " + bodyMiddle);
        return salutation
                + "<p>" +
                context.getResources().getQuantityString(R.plurals.email_body, selectedItems.size())
                + "<p>" +
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
            builder.append("<br>");
        }
        return builder.toString();
    }

    /**
     * Helper method that builds a html string for a selected item
     * @param selectedItem
     * @return
     */
    private static String buildSelectedItemString(final SimplifiedBusiness selectedItem) {
        final StringBuilder builder = new StringBuilder();

        switch (selectedItem.getDataSource()) {
            case LocalConstants.FACEBOOK:
                //name with link
                builder.append(String.format("<p><a href=\"%s\" style=\"text-decoration:none\"><b>%s</b></a>",
                        selectedItem.getFbUrl(),
                        selectedItem.getName()));
                //likes and count of checkins
                builder.append(String.format("<br>%d likes, %d checkins",
                        selectedItem.getLikes(),
                        selectedItem.getCheckins()));
                break;

            case LocalConstants.YELP:
                //name with link
                builder.append(String.format("<p><a href=\"%s\" style=\"text-decoration:none\"><b>%s</b></a>",
                        selectedItem.getWebUrl(),
                        selectedItem.getName()));
                //rating and count of reviews
                builder.append(String.format("<br>%s stars, %d reviews",
                        FormattingUtils.getDecimalFormatterNoRounding(1).format(selectedItem.getRating()),
                        selectedItem.getReviews()));
                break;

            case LocalConstants.GOOGLE:
                //name with link
                builder.append(String.format("<p><a href=\"%s\" style=\"text-decoration:none\"><b>%s</b></a>",
                        selectedItem.getWebUrl(),
                        selectedItem.getName()));
                //rating
                builder.append(String.format("<br>%s stars",
                        FormattingUtils.getDecimalFormatterNoRounding(1).format(selectedItem.getRating())));
                break;

            default:
                break;
        }


        //address
        builder.append(String.format("<br>%s", selectedItem.getAddress()));

        return builder.toString();
    }

}
