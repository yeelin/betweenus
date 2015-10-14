package com.example.yeelin.projects.betweenus.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.yeelin.projects.betweenus.R;

/**
 * Created by ninjakiki on 10/13/15.
 */
public class LoginActivity
        extends BaseActivity {

    private static final String TAG = LoginActivity.class.getCanonicalName();

    public static Intent buildIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupToolbar(R.id.login_toolbar, false);


    }
}
