package com.example.yeelin.projects.betweenus.fragment;

import android.os.Bundle;

import android.support.v7.preference.PreferenceFragmentCompat;
import com.example.yeelin.projects.betweenus.R;

/**
 * Created by ninjakiki on 1/12/16.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    /**
     * Creates a new instance of the settings fragment
     * @return
     */
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    /**
     * Configure the fragment and load preferences from the xml resource.
     * Note we are using the v7 support library so overriding onCreatePreferences
     * is required.
     * @param bundle
     * @param s
     */
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        //load preferences from an xml resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
