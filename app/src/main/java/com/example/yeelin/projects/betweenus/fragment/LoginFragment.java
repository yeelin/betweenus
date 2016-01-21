package com.example.yeelin.projects.betweenus.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.analytics.EventConstants;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONObject;

/**
 * Created by ninjakiki on 10/13/15.
 */
public class LoginFragment extends Fragment {

    //logcat
    private static final String TAG = LoginFragment.class.getCanonicalName();
    //constants
    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String PICTURE = "picture";
    private static final String FIELDS = "fields";
    private static final String REQUEST_FIELDS = TextUtils.join(",", new String[]{ID, NAME, PICTURE});

    //member variables
    private LoginFragmentListener listener;

    //facebook-related
    private AccessTokenTracker accessTokenTracker;
    private CallbackManager callbackManager;
    private JSONObject user;

    /**
     * Callback from this fragment for whoever who is interested in login callbacks
     */
    public interface LoginFragmentListener {
        void onLoginError();
        void onLoginCancel();
    }

    /**
     * Make sure the activity or parent fragment implements the callback.
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Object objectToCast = getParentFragment() != null ? getParentFragment() : getActivity();
        try {
            listener = (LoginFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement SkipLoginCallback");
        }
    }

    /**
     * Configure the fragment views and callbacks
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_content, container, false);

        //create the callback manager
        callbackManager = CallbackManager.Factory.create();

        //setup view holder
        final ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //setup the login button
        viewHolder.loginButton.setReadPermissions("user_friends");
        viewHolder.loginButton.setFragment(this);
        viewHolder.loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess: Login success.");

                //log user logging in
                AppEventsLogger logger = AppEventsLogger.newLogger(getContext());
                logger.logEvent(EventConstants.EVENT_NAME_LOGIN);
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel: Login canceled.");
                listener.onLoginCancel();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError: Login error. " + error.getLocalizedMessage());
                listener.onLoginError();
            }
        });

        return view;
    }

    /**
     * Init the token tracker so that if the token ever changes, we will refetch the user info
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.d(TAG, "onCurrentAccessTokenChanged");
                fetchUserInfo();
            }
        };
    }

    /**
     * Fetches the user info (profile pic and name) from Facebook
     */
    @Override
    public void onResume() {
        super.onResume();
        fetchUserInfo();
    }

    /**
     * Nullify the listener
     */
    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    /**
     * Stop token tracking
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    /**
     * This is called when the FB login activity returns.  We are required to pass the result to callback manager
     * which will result in either onSuccess, onCancel, or onError being called.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, String.format("onActivityResult: Request code:%s, Result code:%d: ", requestCode, resultCode));
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Fetches the user info (profile pic and name) from  Facebook
     */
    private void fetchUserInfo() {
        //check if we have an access token
        if (AccessToken.getCurrentAccessToken() == null) {
            //no, user is not logged in
            Log.d(TAG, "fetchUserInfo: User is not logged in");
            return;
        }

        //yes, user is logged in
        //Log.d(TAG, "fetchUserInfo: User is logged in");

        //create the graph request
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(TAG, "onCompleted: Calling updateUI");
                        user = object;
                        updateUI();
                    }
                });

        //create the parameters for the request
        Bundle parameters = new Bundle();
        parameters.putString(FIELDS, REQUEST_FIELDS);
        request.setParameters(parameters);
        GraphRequest.executeBatchAsync(request);
    }

    /**
     * Updates the UI with the profile pic and name.
     */
    private void updateUI() {
        ViewHolder viewHolder = getViewHolder();
        if (AccessToken.getCurrentAccessToken() == null || user == null) {
            //no user is not logged in
            viewHolder.userProfilePic.setProfileId(null);
            viewHolder.userName.setText("User isn't logged in");
            return;
        }

        //yes, user is logged in
        //Log.d(TAG, "updateUI: User is logged in");

        //read the Graph API Json result
        String id = user.optString(ID);
        String name = user.optString(NAME);
        String picture = user.optString(PICTURE);
        Log.d(TAG, String.format("updateUI: Id:%s, Name:%s, Pic:%s", id, name, picture));

        //set the user's name
        viewHolder.userName.setText(user.optString(NAME));

        //set the user's profile picture
        final Profile profile = Profile.getCurrentProfile();
        if (profile == null) {
            viewHolder.userProfilePic.setProfileId(null);
            return;
        }

        Log.d(TAG, String.format("updateUI: Id:%s, First:%s, Middle:%s, Last:%s, Name:%s, Link:%s",
                profile.getId(), profile.getFirstName(), profile.getMiddleName(), profile.getLastName(), profile.getName(), profile.getLinkUri().toString()));
        viewHolder.userProfilePic.setProfileId(profile.getId());
    }

    /**
     * Return the view's view holder if it exists
     * @return
     */
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /**
     * View holder class
     */
    private class ViewHolder {
        final ProfilePictureView userProfilePic;
        final TextView userName;
        final LoginButton loginButton;

        ViewHolder(View view) {
            userProfilePic = (ProfilePictureView) view.findViewById(R.id.fb_user_profile_pic);
            userName = (TextView) view.findViewById(R.id.fb_user_name);
            loginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
        }
    }
}
