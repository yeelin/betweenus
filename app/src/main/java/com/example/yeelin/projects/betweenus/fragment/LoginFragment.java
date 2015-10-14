package com.example.yeelin.projects.betweenus.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
public class LoginFragment
        extends Fragment
        implements FacebookCallback<LoginResult>, View.OnClickListener {
    private SkipLoginCallback skipLoginCallback;
    private CallbackManager callbackManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fb_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //create the callback manager
        callbackManager = CallbackManager.Factory.create();

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //setup the login button
        viewHolder.loginButton.setReadPermissions("user_friends");
        viewHolder.loginButton.setFragment(this);
        viewHolder.loginButton.registerCallback(callbackManager, this);

        //setup the skip login button
        viewHolder.skipLoginButton.setOnClickListener(this);
    }

    @Override
    public void onSuccess(LoginResult loginResult) {

    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(FacebookException error) {

    }

    @Override
    public void onClick(View v) {

    }

    public interface SkipLoginCallback {
        void onSkipLoginClicked();
    }

    private class ViewHolder {
        final LoginButton loginButton;
        final TextView skipLoginButton;

        ViewHolder(View view) {
            loginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
            skipLoginButton = (TextView) view.findViewById(R.id.skip_fb_login_button);
        }
    }
}
