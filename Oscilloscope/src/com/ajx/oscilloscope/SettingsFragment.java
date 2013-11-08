package com.ajx.oscilloscope;

import android.os.Bundle;
import android.preference.PreferenceFragment;


/**
 * Created by ajarax on 10/15/13.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int res = getActivity().getResources()
                .getIdentifier(getArguments().getString("resource"), "xml", getActivity().getPackageName());
        addPreferencesFromResource(res);
    }
}
