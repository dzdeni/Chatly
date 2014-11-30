package hu.denield.chatly.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

import hu.denield.chatly.Chatly;
import hu.denield.chatly.R;

/**
 * A PreferenceFragment which loads the settings.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences.Editor mEditor;
    private CheckBoxPreference mAutoLoginPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences sp = getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE);

        // bug?!
        mAutoLoginPreference = (CheckBoxPreference) findPreference(getString(R.string.pref_autologin));
        mAutoLoginPreference.setChecked(sp.getBoolean(getString(R.string.pref_autologin), false));
        mEditor = sp.edit();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_autologin))) {
            if (!mAutoLoginPreference.isChecked()) {
                mEditor.remove(getString(R.string.pref_autologin));
                mEditor.remove(getString(R.string.pref_username));
                mEditor.remove(getString(R.string.pref_password));
            } else {
                String username = ((Chatly) getActivity().getApplication()).getUsername();
                String password = ((Chatly) getActivity().getApplication()).getPassword();
                mEditor.putBoolean(getString(R.string.pref_autologin), true);
                mEditor.putString(getString(R.string.pref_username), username);
                mEditor.putString(getString(R.string.pref_password), password);
            }
            mEditor.apply();
        }
    }
}