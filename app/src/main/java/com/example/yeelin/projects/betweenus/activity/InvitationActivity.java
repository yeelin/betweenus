package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.InvitationFragment;
import com.example.yeelin.projects.betweenus.fragment.LocationEntryFragment;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ninjakiki on 7/24/15.
 */
public class InvitationActivity
        extends BaseActivity
        implements InvitationFragment.InvitationFragmentListener {
    //logcat
    private static final String TAG = InvitationActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_SELECTED_IDS = InvitationActivity.class.getSimpleName() + ".selectedIds";

    //sms and email
    private static final String URI_SMSTO = "smsto:";
    private static final String URI_MAILTO = "mailto:";
    private static final String EXTRA_SMS_BODY = "sms_body";
    //request codes
    private static final int REQUEST_CODE_COMPOSE_TEXT = 100;
    private static final int REQUEST_CODE_COMPOSE_EMAIL = 110;

    /**
     * Builds the appropriate intent to start this activity
     * @param selectedItemIds
     * @return
     */
    public static Intent buildIntent(Context context, ArrayList<String> selectedItemIds) {
        Intent intent = new Intent(context, InvitationActivity.class);
        //put extras
        intent.putStringArrayListExtra(EXTRA_SELECTED_IDS, selectedItemIds);
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
        ArrayList<String> selectedItemIds = intent.getStringArrayListExtra(EXTRA_SELECTED_IDS);

        //check if fragment exists and instantiate if it doesn't
        if (savedInstanceState == null) {
            Fragment invitationFragment = getSupportFragmentManager().findFragmentById(R.id.invitation_fragmentContainer);
            if (invitationFragment == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.invitation_fragmentContainer, InvitationFragment.newInstance(selectedItemIds))
                        .commit();
            }
        }
        else {
            Log.d(TAG, "onCreate: Saved instance state is not null");
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
            case R.id.home:
                navigateUpToParentActivity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Sends an intent out to invite via SMS
     * @param friendPhone
     * @param selectedItemIds
     */
    @Override
    public void onInviteByTextMessage(String friendPhone, ArrayList<String> selectedItemIds) {
        //create sms intent
        //ACTION_SENDTO and smsto: ensures that intent will be handled only by a text messaging app
        String recipientUri = String.format("%s%s", URI_SMSTO, friendPhone);
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(recipientUri));

        //put extras
        intent.putExtra(EXTRA_SMS_BODY, getString(R.string.sms_body, selectedItemIds.toString()));

        //check that there's an app to handle the intent, and start the Activity
        if(intent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "onInviteByTextMessage: Starting SMS activity");
            startActivityForResult(intent, REQUEST_CODE_COMPOSE_TEXT);
        }
        else {
            Log.d(TAG, "onInviteByTextMessage: There are no apps that can handle the SMS intent");
        }
    }

    /**
     * Sends an intent out to invite via email
     * @param friendEmail
     * @param selectedItemIds
     */
    @Override
    public void onInviteByEmail(String friendEmail, ArrayList<String> selectedItemIds) {
        //create email intent
        //ACTION_SENDTO and mailto: ensures that intent will be handled only by an email app
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(URI_MAILTO));

        //put extras
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {friendEmail}); //recipient's email must be in an array
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body, selectedItemIds.toString()));

        //check that there's an app to handle the intent, and start the Activity
        if (intent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "onInviteByEmail: Starting Email activity");
            startActivityForResult(intent, REQUEST_CODE_COMPOSE_EMAIL);
        }
        else {
            Log.d(TAG, "onInviteByEmail: There are no apps that can handle the email intent");
        }
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
