/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.settings.simpleaosp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import com.android.settings.Utils;

import com.android.internal.logging.MetricsLogger;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class StatusBarSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String QUICK_PULLDOWN = "quick_pulldown";
    private static final String SHOW_CARRIER_LABEL = "status_bar_show_carrier";
    private static final String KEY_LOCK_CLOCK = "lock_clock";
    private static final String KEY_LOCK_CLOCK_PACKAGE_NAME = "com.cyanogenmod.lockclock";
    private static final String KEY_STATUS_BAR_CLOCK = "clock_style_pref";

    private ListPreference mQuickPulldown;
    private ListPreference mShowCarrierLabel;
    private PreferenceScreen mLockClock;
    private PreferenceScreen mClockStyle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.status_bar_settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mQuickPulldown = (ListPreference) prefSet.findPreference(QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        int quickPulldownValue = Settings.System.getIntForUser(resolver,
                Settings.System.QS_QUICK_PULLDOWN, 0, UserHandle.USER_CURRENT);
        mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
        mQuickPulldown.setSummary(mQuickPulldown.getEntry());

        mShowCarrierLabel =
                (ListPreference) findPreference(SHOW_CARRIER_LABEL);
        int showCarrierLabel = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_CARRIER, 1, UserHandle.USER_CURRENT);
        mShowCarrierLabel.setValue(String.valueOf(showCarrierLabel));
        mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntry());
        mShowCarrierLabel.setOnPreferenceChangeListener(this);

        if (!Utils.isVoiceCapable(getActivity())) {
            prefSet.removePreference(mShowCarrierLabel);
        }

	mLockClock = (PreferenceScreen) findPreference(KEY_LOCK_CLOCK);
        if (!Utils.isPackageInstalled(getActivity(), KEY_LOCK_CLOCK_PACKAGE_NAME)) {
            prefSet.removePreference(mLockClock);
        }

        mClockStyle = (PreferenceScreen) prefSet.findPreference(KEY_STATUS_BAR_CLOCK);
        updateClockStyleDescription();
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.STATUS_BAR_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateClockStyleDescription();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getContentResolver();
        if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) newValue);
            int index = mQuickPulldown.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.QS_QUICK_PULLDOWN,
                    quickPulldownValue, UserHandle.USER_CURRENT);
            mQuickPulldown.setSummary(mQuickPulldown.getEntries()[index]);
            return true;
        } else if (preference == mShowCarrierLabel) {
            int showCarrierLabel = Integer.valueOf((String) newValue);
            int index = mShowCarrierLabel.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.
                STATUS_BAR_SHOW_CARRIER, showCarrierLabel, UserHandle.USER_CURRENT);
            mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntries()[index]);
            return true;
        }
        return false;
    }

    private void updateClockStyleDescription() {
        if (mClockStyle == null) {
            return;
        }
        if (Settings.System.getInt(getContentResolver(),
               Settings.System.STATUS_BAR_CLOCK, 1) == 1) {
            mClockStyle.setSummary(getString(R.string.enabled_string));
        } else {
            mClockStyle.setSummary(getString(R.string.disabled_string));
         }
     }
}

