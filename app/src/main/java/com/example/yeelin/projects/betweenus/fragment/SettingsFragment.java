package com.example.yeelin.projects.betweenus.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.example.yeelin.projects.betweenus.R;
import com.example.yeelin.projects.betweenus.utils.PreferenceUtils;

/**
 * Created by ninjakiki on 1/12/16.
 * http://stackoverflow.com/questions/32070186/how-to-use-the-v7-v14-preference-support-library
 * https://plus.google.com/+AndroidDevelopers/posts/9kZ3SsXdT2T
 */
public class SettingsFragment
        extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

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

        //read and set preferred data source in summary
        setPreferredDataSourceInSummary();

        //set search term in summary
        setPreferredSearchTermInSummary();

        //set search radius in summary
        setPreferredSearchRadiusInSummary();
    }

    /**
     * Registers a callback to be invoked when a change happens to a preference
     */
    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Unregisters the callback to be invoked when a change happens to a preference
     */
    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * SharedPreferences.OnSharedPreferenceChangeListener Callback
     * This method is called when a change happens to a preference
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtils.KEY_DATA_SOURCE:
                setPreferredDataSourceInSummary();
                break;

            case PreferenceUtils.KEY_SEARCH_TERM:
                setPreferredSearchTermInSummary();
                break;

            case PreferenceUtils.KEY_SEARCH_RADIUS:
                setPreferredSearchRadiusInSummary();
                break;
        }
    }

    /**
     * Sets the preferred data source in the summary field of the setting
     */
    private void setPreferredDataSourceInSummary() {
        String preferredDataSource = PreferenceUtils.getPreferredDataSourceString(getContext());
        findPreference(PreferenceUtils.KEY_DATA_SOURCE).setSummary(preferredDataSource);
    }

    /**
     * Sets the preferred search term in the summary field of the setting
     */
    private void setPreferredSearchTermInSummary() {
        String preferredSearchTerm = PreferenceUtils.getPreferredSearchTerm(getContext());
        findPreference(PreferenceUtils.KEY_SEARCH_TERM).setSummary(preferredSearchTerm);
    }

    /**
     * Sets the preferred search radius in the summary field of the setting
     */
    private void setPreferredSearchRadiusInSummary() {
        int preferredSearchRadius = PreferenceUtils.getPreferredSearchRadius(getContext());
        boolean useMetric = PreferenceUtils.useMetric(getContext());

        String searchRadiusString;
        if (useMetric)
            searchRadiusString = getResources().getQuantityString(R.plurals.search_radius_in_km, preferredSearchRadius, preferredSearchRadius);
        else
            searchRadiusString = getResources().getQuantityString(R.plurals.search_radius_in_miles, preferredSearchRadius, preferredSearchRadius);
        findPreference(PreferenceUtils.KEY_SEARCH_RADIUS).setSummary(searchRadiusString);
    }
}
