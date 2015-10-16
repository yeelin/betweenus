package com.example.yeelin.projects.betweenus.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yeelin.projects.betweenus.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

/**
 * Created by ninjakiki on 10/13/15.
 */
public class LoginFragment extends Fragment {

    //logcat
    private static final String TAG = LoginFragment.class.getCanonicalName();
    //member variables
    private SkipLoginCallback skipLoginCallback;
    private CallbackManager callbackManager;

    /**
     * Callback from this fragment for whoever who is interested when the user skips fb login
     */
    public interface SkipLoginCallback {
        void onSkipLoginClicked();
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
            skipLoginCallback = (SkipLoginCallback) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement SkipLoginCallback");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fb_login, container, false);

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
                Log.d(TAG, "onCancel: Login success.");
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel: Login canceled.");

                //create a snackbar to inform the user
                if (viewHolder != null) {
                    final Snackbar snackbar = Snackbar.make(viewHolder.rootView, "Oops! Facebook login error.", Snackbar.LENGTH_LONG);
                }
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError: Login error. " + error.getLocalizedMessage());

                //create a snackbar to inform the user
                if (viewHolder != null) {
                    final Snackbar snackbar = Snackbar.make(viewHolder.rootView, "Oops! Facebook login error.", Snackbar.LENGTH_LONG);
                }
            }
        });

        //setup the skip login button
        viewHolder.skipLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (skipLoginCallback != null) {
                    skipLoginCallback.onSkipLoginClicked();
                }
            }
        });

        return view;
    }

    /**
     * Nullify the listener
     */
    @Override
    public void onDetach() {
        skipLoginCallback = null;
        super.onDetach();
    }

    /**
     * This is called when the FB login activity returns.  We are required to pass the result to callback manager.
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
        final View rootView;
        final LoginButton loginButton;
        final TextView skipLoginButton;

        ViewHolder(View view) {
            rootView = view.findViewById(R.id.root_layout);
            loginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
            skipLoginButton = (TextView) view.findViewById(R.id.skip_fb_login_button);
        }
    }
}
