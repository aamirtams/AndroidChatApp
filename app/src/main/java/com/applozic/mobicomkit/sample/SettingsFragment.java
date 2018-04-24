package com.applozic.mobicomkit.sample;

/**
 * Created by Aamir on 06-Jul-17.
 */
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
public  class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.app_preference);
    }
    public static SettingsFragment newInstance() {
        Bundle args = new Bundle();
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }
}