package org.republica.fragments;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;

import org.republica.R;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String KEY_PREF_NOTIFICATIONS_VIBRATE = "notifications_vibrate";
    public static final String KEY_PREF_NOTIFICATIONS_LED = "notifications_led";
    public static final String KEY_PREF_NOTIFICATIONS_DELAY = "notifications_delay";

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
        updateNotificationsEnabled();
        updateNotificationsDelaySummary();
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (KEY_PREF_NOTIFICATIONS_ENABLED.equals(key)) {
            updateNotificationsEnabled();
        } else if (KEY_PREF_NOTIFICATIONS_DELAY.equals(key)) {
            updateNotificationsDelaySummary();
        }
    }

    private void updateNotificationsEnabled() {
        boolean notificationsEnabled = ((CheckBoxPreference) findPreference(KEY_PREF_NOTIFICATIONS_ENABLED)).isChecked();
        findPreference(KEY_PREF_NOTIFICATIONS_VIBRATE).setEnabled(notificationsEnabled);
        findPreference(KEY_PREF_NOTIFICATIONS_LED).setEnabled(notificationsEnabled);
        findPreference(KEY_PREF_NOTIFICATIONS_DELAY).setEnabled(notificationsEnabled);
    }

    private void updateNotificationsDelaySummary() {
        ListPreference notificationsDelayPreference = (ListPreference) findPreference(KEY_PREF_NOTIFICATIONS_DELAY);
        notificationsDelayPreference.setSummary(notificationsDelayPreference.getEntry());
    }
}
