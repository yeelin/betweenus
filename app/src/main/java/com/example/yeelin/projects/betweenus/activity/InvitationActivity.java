package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.analytics.EventConstants;
import com.example.yeelin.projects.betweenus.data.generic.model.SimplifiedBusiness;
import com.example.yeelin.projects.betweenus.fragment.InvitationFragment;
import com.example.yeelin.projects.betweenus.service.ItineraryIntentService;
import com.example.yeelin.projects.betweenus.utils.EmailUtils;
import com.example.yeelin.projects.betweenus.utils.SmsUtils;
import com.facebook.appevents.AppEventsLogger;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 7/24/15.
 */
public class InvitationActivity
        extends BaseActivity
        implements InvitationFragment.InvitationFragmentListener {
    //logcat
    private static final String TAG = InvitationActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_SELECTED_ITEMS = InvitationActivity.class.getSimpleName() + ".selectedItems";
    private static final String EXTRA_ORIGINATING_VIEW = InvitationActivity.class.getSimpleName() + ".originatingView";

    //sms and email
    private static final String URI_SMSTO = "smsto:";
    private static final String URI_MAILTO = "mailto:";
    private static final String EXTRA_SMS_BODY = "sms_body";
    //request codes
    private static final int REQUEST_CODE_COMPOSE_TEXT = 100;
    private static final int REQUEST_CODE_COMPOSE_EMAIL = 110;

    //member variables
    private ArrayList<SimplifiedBusiness> selectedItems;
    private String originatingView;

    /**
     * Builds the appropriate intent to start this activity
     * @param context
     * @param selectedItems
     * @param originatingView
     * @return
     */
    public static Intent buildIntent(Context context, ArrayList<SimplifiedBusiness> selectedItems, String originatingView) {
        Intent intent = new Intent(context, InvitationActivity.class);
        //put extras
        intent.putParcelableArrayListExtra(EXTRA_SELECTED_ITEMS, selectedItems);
        intent.putExtra(EXTRA_ORIGINATING_VIEW, originatingView);
        return intent;
    }

    /**
     * Creates the activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);

        //setup tool bar
        setupToolbar(R.id.invitation_toolbar, true);

        //read intent
        Intent intent = getIntent();
        selectedItems = intent.getParcelableArrayListExtra(EXTRA_SELECTED_ITEMS);
        originatingView = intent.getStringExtra(EXTRA_ORIGINATING_VIEW);

        //check if fragment exists and instantiate if it doesn't
        if (savedInstanceState == null) {
            Fragment invitationFragment = getSupportFragmentManager().findFragmentById(R.id.invitation_fragmentContainer);
            if (invitationFragment == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.invitation_fragmentContainer, InvitationFragment.newInstance(selectedItems))
                        .commit();
            }
        }
    }

    /**
     * Handles user selection of menu options
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected: Up button clicked");
                //we want to behave as if it were back button press here because we could have come
                //from either SuggestionsPagerActivity or SuggestionsActivity
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Log activation and initiation of invite process.
     */
    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);

        //log user initiated invite process
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        Bundle parameters = new Bundle();
        parameters.putString(EventConstants.EVENT_PARAM_INITIATED_VIEW, originatingView);
        parameters.putInt(EventConstants.EVENT_PARAM_NUM_PLACES_SELECTED, selectedItems.size());
        logger.logEvent(EventConstants.EVENT_NAME_INITIATED_INVITE, parameters);
    }

    /**
     * Log deactivation
     */
    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    /**
     * Sends an intent out to invite via SMS. Log completion of invite process.
     * @param friendName
     * @param friendPhone if null, then user will fill out the phone number after being transferred to sms app
     */
    @Override
    public void onInviteByTextMessage(@Nullable String friendName, @Nullable String friendPhone) {
        //create sms intent
        //ACTION_SENDTO and smsto: ensures that intent will be handled only by a text messaging app
//        String recipientUri = String.format("%s%s", URI_SMSTO, friendPhone);
//        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(recipientUri));

        final Intent intent = new Intent(Intent.ACTION_SENDTO);
        //set uri
        if (friendPhone == null)
            intent.setData(Uri.parse(URI_SMSTO));
        else
            intent.setData(Uri.parse(String.format("%s%s", URI_SMSTO, friendPhone)));

        //put extras
        intent.putExtra(EXTRA_SMS_BODY, SmsUtils.buildBody(this, friendName, selectedItems));

        //check that there's an app to handle the intent, and start the Activity
        if(intent.resolveActivity(getPackageManager()) == null) {
            Log.d(TAG, "onInviteByTextMessage: There are no apps that can handle the SMS intent");
            return;
        }

        Log.d(TAG, "onInviteByTextMessage: Starting SMS activity");
        startActivityForResult(intent, REQUEST_CODE_COMPOSE_TEXT);

        //save to db
        Intent saveIntent = ItineraryIntentService.buildIntent(this, selectedItems, friendName, null, friendPhone);
        startService(saveIntent);

        //log user completed invite
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        Bundle parameters = new Bundle();
        parameters.putString(EventConstants.EVENT_PARAM_INITIATED_VIEW, originatingView);
        parameters.putString(EventConstants.EVENT_PARAM_DELIVERY_METHOD, EventConstants.EVENT_PARAM_DELIVER_BY_TXT);
        parameters.putInt(EventConstants.EVENT_PARAM_NUM_PLACES_SELECTED, selectedItems.size());
        logger.logEvent(EventConstants.EVENT_NAME_COMPLETED_INVITE, parameters);

    }

    /**
     * Sends an intent out to invite via email. Log completion of invite process.
     * @param friendName
     * @param friendEmail if null, then user will fill out the email address after being transferred to email app
     */
    @Override
    public void onInviteByEmail(@Nullable String friendName, @Nullable String friendEmail) {
        //create email intent
        //ACTION_SENDTO and mailto: ensures that intent will be handled only by an email app
        final Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(URI_MAILTO));

        //put extras
        if (friendEmail != null)
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {friendEmail}); //recipient's email must be in an array
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));

        Spanned spannedHtml = Html.fromHtml(EmailUtils.buildBody(this, friendName, selectedItems));
        intent.putExtra(Intent.EXTRA_TEXT, spannedHtml);

        //check that there's an app to handle the intent, and start the Activity
        if (intent.resolveActivity(getPackageManager()) == null) {
            Log.d(TAG, "onInviteByEmail: There are no apps that can handle the email intent");
            return;
        }

        Log.d(TAG, "onInviteByEmail: Starting Email activity");
        startActivityForResult(intent, REQUEST_CODE_COMPOSE_EMAIL);

        //save to db
        Intent saveIntent = ItineraryIntentService.buildIntent(this, selectedItems, friendName, friendEmail, null);
        startService(saveIntent);

        //log user completed invite
        AppEventsLogger logger = AppEventsLogger.newLogger(this);
        Bundle parameters = new Bundle();
        parameters.putString(EventConstants.EVENT_PARAM_INITIATED_VIEW, originatingView);
        parameters.putString(EventConstants.EVENT_PARAM_DELIVERY_METHOD, EventConstants.EVENT_PARAM_DELIVER_BY_EMAIL);
        parameters.putInt(EventConstants.EVENT_PARAM_NUM_PLACES_SELECTED, selectedItems.size());
        logger.logEvent(EventConstants.EVENT_NAME_COMPLETED_INVITE, parameters);
    }

    /**
     * If we are returning from SMS/email invitation, then we are done. Start the main activity again.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_COMPOSE_TEXT || requestCode == REQUEST_CODE_COMPOSE_EMAIL) {
            Intent intent = LocationEntryActivity.buildIntent(this);
            //add intent flags
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
