package hu.denield.chatly;

import android.app.Fragment;
import android.os.Bundle;

import hu.denield.chatly.fragment.SettingsFragment;

public class SettingsActivity extends BaseActivity {
    private final static String SETTINGS_FRAGMENT = "settingsFragment";

    private Fragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            // restores the fragment from the savedInstanceState
            mSettingsFragment = getFragmentManager().getFragment(savedInstanceState, SETTINGS_FRAGMENT);
        }
        // initialize the settings fragment if it was not before
        if (mSettingsFragment == null) {
            mSettingsFragment = new SettingsFragment();
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_settings, mSettingsFragment)
                    .commit();
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_settings;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getFragmentManager().putFragment(outState, SETTINGS_FRAGMENT, mSettingsFragment);
    }
}