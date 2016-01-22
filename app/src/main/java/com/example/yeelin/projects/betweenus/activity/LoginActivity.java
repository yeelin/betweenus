package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.LoginFragment;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by ninjakiki on 10/13/15.
 */
public class LoginActivity
        extends BaseActivity
        implements LoginFragment.LoginFragmentListener {

    //logcat
    private static final String TAG = LoginActivity.class.getCanonicalName();

    /**
     * Builds the appropriate intent for starting this activity
     * @param context
     * @return
     */
    public static Intent buildIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        return intent;
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //init view and toolbar
        setContentView(R.layout.activity_login);
        setupToolbar(R.id.login_toolbar, true);

        //init fb sdk
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    /**
     * Log activation
     */
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Login activity resuming");
        super.onResume();
        AppEventsLogger.activateApp(this);
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
     * LoginFragment.LoginFragmentListener
     * Show a snackbar to inform user about the error
     */
    @Override
    public void onLoginError() {
        View parent = findViewById(R.id.root_layout);
        if (parent == null) return;
        Snackbar.make(parent, R.string.snackbar_fb_login_error, Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * LoginFragment.LoginFragmentListener
     * Show a snackbar to inform user about the cancel
     */
    @Override
    public void onLoginCancel() {
        View parent = findViewById(R.id.root_layout);
        if (parent == null) return;
        Snackbar.make(parent, R.string.snackbar_fb_login_canceled, Snackbar.LENGTH_LONG)
                .show();
    }
}
