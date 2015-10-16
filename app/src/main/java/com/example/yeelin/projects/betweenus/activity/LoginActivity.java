package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.fragment.LoginFragment;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;

/**
 * Created by ninjakiki on 10/13/15.
 */
public class LoginActivity
        extends BaseActivity
        implements LoginFragment.SkipLoginCallback {

    //logcat
    private static final String TAG = LoginActivity.class.getCanonicalName();
    //constants
    private static final int LOGIN = 0;
    private static final int LOGOUT = 1;
    private static final int FRAGMENT_COUNT = LOGOUT + 1;

    //member variables
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private boolean userSkippedLogin = false;
    private boolean isResumed = false;

    //facebook-related
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;

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
        callbackManager = CallbackManager.Factory.create();

        //init access token tracker
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (isResumed) {
                    FragmentManager fm = getSupportFragmentManager();
                    int backStackSize = fm.getBackStackEntryCount();
                    for (int i=0; i<backStackSize; i++) {
                        fm.popBackStack();
                    }

                    //check if new state means user is logged in
                    if (currentAccessToken != null) {
                        //yes, user is logged in
                        Log.d(TAG, "onCurrentAccessTokenChanged: User is now logged in");
                        showFragment(LOGOUT, false);
                    }
                    else {
                        //no, user is not logged in
                        Log.d(TAG, "onCurrentAccessTokenChanged: User is no longer logged in");
                        showFragment(LOGIN, false);
                    }
                }
            }
        };

        //store references to both fragments
        FragmentManager fm = getSupportFragmentManager();
        fragments[LOGIN] = fm.findFragmentById(R.id.login_fragment);
        fragments[LOGOUT] = fm.findFragmentById(R.id.logout_fragment);

        //hide both fragments
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.hide(fragments[LOGIN])
                .hide(fragments[LOGOUT])
                .commit();
    }

    @Override
    public void onSkipLoginClicked() {
        userSkippedLogin = true;
        navigateUpToParentActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    private void showLoginFragment() {
        showFragment(LOGIN, true);
    }

    private void showLogoutFragment() {
        showFragment(LOGOUT, true);
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i=0; i<fragments.length; i++) {
            if (i == fragmentIndex) transaction.show(fragments[i]);
            else transaction.hide(fragments[i]);
        }
        if (addToBackStack) {
            transaction.addToBackStack(null); //no name for the transaction
        }
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        //check if user is already logged in
        if (AccessToken.getCurrentAccessToken() != null) {
            //yes, user is logged in
            Log.d(TAG, "onResumeFragments: User is logged in");
            showFragment(LOGOUT, false);
        }
        else {
            //no, user is not logged in
            Log.d(TAG, "onResumeFragments: User is not logged in");
            showFragment(LOGIN, false);
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.d(TAG, String.format("onActivityResult: Request code:%s, Result code:%d: ", requestCode, resultCode));
//        super.onActivityResult(requestCode, resultCode, data);
//
//        callbackManager.onActivityResult(requestCode, resultCode, data);
//    }
}
