package com.ajx.oscilloscope;

import android.preference.PreferenceActivity;

import java.util.List;

/**
 * Created by ajarax on 10/15/13.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }
}
