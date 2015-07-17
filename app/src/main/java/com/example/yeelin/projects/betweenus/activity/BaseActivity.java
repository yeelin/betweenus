package com.example.yeelin.projects.betweenus.activity;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.yeelin.projects.betweenus.R;

/**
 * Created by ninjakiki on 7/15/15.
 */
public abstract class BaseActivity
        extends AppCompatActivity {
    /**
     * Helper method to setup toolbar
     */
    protected void setupToolbar(int toolbarResId, boolean enableUpNavigation) {
        setSupportActionBar((Toolbar) findViewById(toolbarResId));

        //check if action bar is null
        if (getSupportActionBar() == null) return;

        //set toolbar elevation
        getSupportActionBar().setElevation(R.dimen.toolbar_elevation);

        if (enableUpNavigation) {
            //enable the Up arrow
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Provides Up navigation the proper way :)
     *
     * Clear top : if the activity being launched is already in the current task, then instead of launching a new instance,
     * all activities on top of it will be closed, and this intent will be delivered to the old activity as a new intent
     * and will be either finished and recreated OR restarted.
     *
     * Single top: if set, the activity will not be recreated if it is already at the top of the stack.
     */
    protected void navigateUpToParentActivity(AppCompatActivity currentActivity) {
        //get the intent that started the parent activity
        Intent intent = NavUtils.getParentActivityIntent(currentActivity);

        //add intent flags
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NavUtils.navigateUpTo(currentActivity, intent);
    }
}
