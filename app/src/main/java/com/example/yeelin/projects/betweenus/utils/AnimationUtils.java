package com.example.yeelin.projects.betweenus.utils;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Created by ninjakiki on 7/21/15.
 */
public final class AnimationUtils {

    /**
     * Animates one view in, and animates another view out.
     * @param context
     * @param fadeInView
     * @param fadeOutView
     */
    public static void crossFadeViews (final Context context, final View fadeInView, final View fadeOutView) {
        long shortDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

        //fadeInView should initially be transparent but visible
        fadeInView.setAlpha(0f);
        fadeInView.setVisibility(View.VISIBLE);

        //animate fadeInView from 0f to 1f
        ViewCompat.animate(fadeInView)
                .alpha(1f)
                .setDuration(shortDuration)
                .withLayer();

        //animate fadeOutView from 1f to 0f
        ViewCompat.animate(fadeOutView)
                .alpha(0f)
                .withLayer()
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        fadeOutView.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Fades in a view
     * @param context
     * @param fadeInView
     */
    public static void fadeInView (final Context context, final View fadeInView) {
        long shortDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

        //fadeInView should initially be transparent but visible
        fadeInView.setAlpha(0f);
        fadeInView.setVisibility(View.VISIBLE);

        //animate fadeInView from 0f to 1f
        ViewCompat.animate(fadeInView)
                .alpha(1f)
                .setDuration(shortDuration)
                .withLayer();
    }
}

