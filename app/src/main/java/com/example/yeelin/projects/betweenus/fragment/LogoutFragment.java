package com.example.yeelin.projects.betweenus.fragment;

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
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONObject;

/**
 * Created by ninjakiki on 10/14/15.
 */
public class LogoutFragment extends Fragment {
    //logcat
    private static final String TAG = LogoutFragment.class.getCanonicalName();

    //constants
    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String PICTURE = "picture";
    private static final String FIELDS = "fields";
    private static final String REQUEST_FIELDS = TextUtils.join(",", new String[]{ID, NAME, PICTURE});

    //member variables
    private JSONObject user;

    //facebook-related
    private AccessTokenTracker accessTokenTracker;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fb_logout, container, false);

        //setup view holder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //setup login button
        viewHolder.loginButton.setFragment(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                fetchUserInfo();
                updateUI();
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUserInfo();
        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    private void fetchUserInfo() {
        //check if we have an access token
        if (AccessToken.getCurrentAccessToken() != null) {
            //yes, user is logged in
            Log.d(TAG, "fetchUserInfo: User is logged in");

            //create the graph request
            GraphRequest request = GraphRequest.newMeRequest(
                    AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
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
        else {
            //no, user is not logged in
            Log.d(TAG, "fetchUserInfo: User is not logged in");
        }
    }

    private void updateUI() {
        if (!isAdded()) return;

        if (AccessToken.getCurrentAccessToken() != null) {
            ViewHolder viewHolder = getViewHolder();
            if (user != null) {
                //read the Graph API Json result
                String id = user.optString(ID);
                String name = user.optString(NAME);
                String picture = user.optString(PICTURE);
                Log.d(TAG, String.format("updateUI: Id:%s, Name:%s, Pic:%s", id, name, picture));

                //set the user's name
                viewHolder.userName.setText(user.optString(NAME));

                //set the user's profile picture
                Profile profile = Profile.getCurrentProfile();
                if (profile != null) {
                    Log.d(TAG, String.format("Id:%s, First:%s, Middle:%s, Last:%s, Name:%s, Link:%s",
                            profile.getId(), profile.getFirstName(), profile.getMiddleName(), profile.getLastName(), profile.getName(), profile.getLinkUri().toString()));
                    viewHolder.userProfilePic.setProfileId(profile.getId());
                }
                else {
                    viewHolder.userProfilePic.setProfileId(null);
                }
            }
            else {
                viewHolder.userName.setText("User isn't logged in");
            }
        }
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
