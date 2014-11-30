package hu.denield.chatly;

import android.app.Fragment;
import android.os.Bundle;
import android.widget.Toast;

import hu.denield.chatly.constant.Extras;
import hu.denield.chatly.fragment.AboutFragment;
import hu.denield.chatly.fragment.SettingsFragment;

import static hu.denield.chatly.constant.Fragments.*;

/**
 * The activity that handles the fragments.
 * There is no need to use this yet, it will
 * make sense in a future version.
 */
public class FragmentActivity extends BaseActivity {
    private final static String FRAGMENT = "fragment";

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            // restores the fragment from the savedInstanceState
            mFragment = getFragmentManager().getFragment(savedInstanceState, FRAGMENT);
        }
        // initialize the settings fragment if it was not before
        if (mFragment == null) {
            if (getIntent().getExtras() != null && getIntent().hasExtra(Extras.FRAGMENT)) {
                FragmentName actFragment = fromInteger(getIntent().getIntExtra(Extras.FRAGMENT, 0));
                switch (actFragment) {
                    case SETTINGS:
                        mFragment = new SettingsFragment();
                        getSupportActionBar().setTitle(R.string.title_activity_settings);
                        break;
                    default:
                    case ABOUT:
                        mFragment = new AboutFragment();
                        getSupportActionBar().setTitle(R.string.title_activity_about);
                        break;
                }

                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.fragment_container, mFragment)
                        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                        .commit();
            } else {
                Toast.makeText(this, R.string.fragment_error, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getFragmentManager().putFragment(outState, FRAGMENT, mFragment);
    }
}