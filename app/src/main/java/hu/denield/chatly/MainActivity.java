package hu.denield.chatly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingRightInAnimationAdapter;

import hu.denield.chatly.adapter.MessageListAdapter;
import hu.denield.chatly.contant.Extras;
import hu.denield.chatly.contant.SharedPrefs;
import hu.denield.chatly.data.MessageData;
import hu.denield.chatly.data.MessageDataManager;
import hu.denield.chatly.util.Anim;

public class MainActivity extends BaseActivity {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String MESSAGES = "messages";
    public static final String CHAT_INPUT = "chatInput";
    public static final String CHAT_INPUT_STATE = "chatInputState";
    public static final String NAVIGATION_DRAWER_STATE = "navigationDrawerState";

    private SharedPreferences mSp;
    private ListView mMessageListView;
    private AnimationAdapter mMessageListAdapter;

    private String mUsername;
    private String mPassword;
    private boolean mRemember;

    private TextView mUsernameTextView;
    private ButtonFloat mNewMessageButton;
    private EditText mMessageInputText;
    private RelativeLayout mMessageInputLayout;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSp = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Fresh start
        if (savedInstanceState == null) {
            if (getIntent().getExtras() != null) {
                // Trying to login
                mUsername = getIntent().getStringExtra(Extras.USERNAME);
                mPassword = getIntent().getStringExtra(Extras.PASSWORD);
                mRemember = getIntent().getBooleanExtra(Extras.REMEMBER, false);
            } else {
                // Trying to get login from SharedPreferences
                mUsername = mSp.getString(SharedPrefs.USERNAME, null);
                mPassword = mSp.getString(SharedPrefs.PASSWORD, null);
            }
        } else {
            // Trying to login from savedInstanceState
            mUsername = savedInstanceState.getString(USERNAME);
            mPassword = savedInstanceState.getString(PASSWORD);
        }

        // Validate the user
        if (!validateUser(mUsername, mPassword)) return;

        // Save the login credentials for further use (next login)
        if (mRemember) {
            SharedPreferences.Editor editor = mSp.edit();
            editor.putString(SharedPrefs.USERNAME, mUsername);
            editor.putString(SharedPrefs.PASSWORD, mPassword);
            editor.apply();
        }

        // Load the view from the inflated layout
        mMessageListView = (ListView) findViewById(R.id.listview_messages);
        mUsernameTextView = (TextView) findViewById(R.id.chat_username);
        mMessageInputLayout = (RelativeLayout) findViewById(R.id.chat_input_layout);
        mMessageInputText = (EditText) findViewById(R.id.chat_message_input);
        mNewMessageButton = (ButtonFloat) findViewById(R.id.chat_button_new_message);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Load the view from the inflated layout to local variables and do some things with them
        TextView drawerUsernameTextView = (TextView) findViewById(R.id.drawer_username);
        drawerUsernameTextView.setText(mUsername);

        mUsernameTextView.setText(mUsername + ": ");
        mMessageInputLayout.setVisibility(View.GONE);

        mMessageInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                sendMessage(mMessageInputText.getText().toString());
                return true;
            }
        });

        MessageListAdapter baseAdapter = new MessageListAdapter(this, MessageDataManager.getInstance().getMessages());

        // drawer toogle (lollipop's hamburger to arrow)
        mDrawerToggle = new ActionBarDrawerToggle (
                this,
                mDrawerLayout,
                getToolbar(),
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(getString(R.string.app_name) + ": #default");
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                getSupportActionBar().setTitle(R.string.app_name);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // drawer settings
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        getSupportActionBar().setTitle(getString(R.string.app_name) + ": #default");

        mMessageListAdapter = new SwingRightInAnimationAdapter(baseAdapter);
        mMessageListAdapter.setAbsListView(mMessageListView);
        mMessageListView.setAdapter(mMessageListAdapter);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.getParcelable(MESSAGES) != null) {
            mMessageListView.onRestoreInstanceState(savedInstanceState.getParcelable(MESSAGES));
        }
        if (savedInstanceState.getString(CHAT_INPUT) != null) {
            mMessageInputText.setText(savedInstanceState.getString(CHAT_INPUT));
        }
        if (savedInstanceState.getBoolean(CHAT_INPUT_STATE)) {
            mMessageInputLayout.setVisibility(View.VISIBLE);
            mNewMessageButton.setVisibility(View.GONE);
        }
        if (savedInstanceState.getBoolean(NAVIGATION_DRAWER_STATE)) mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(USERNAME, mUsername);
        savedInstanceState.putString(PASSWORD, mPassword);
        savedInstanceState.putParcelable(MESSAGES, mMessageListView.onSaveInstanceState());
        savedInstanceState.putString(CHAT_INPUT, mMessageInputText.getText().toString());
        savedInstanceState.putBoolean(CHAT_INPUT_STATE, (mMessageInputLayout.getVisibility() == View.VISIBLE)?true:false);
        savedInstanceState.putBoolean(NAVIGATION_DRAWER_STATE, mDrawerLayout.isDrawerOpen(GravityCompat.START));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Toast.makeText(this, getString(R.string.action_settings), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_logout:
                SharedPreferences.Editor editor = mSp.edit();
                editor.remove(SharedPrefs.USERNAME);
                editor.remove(SharedPrefs.PASSWORD);
                editor.apply();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void newMessageOnClick(View view) {
        Anim.showViewWithAnimation(this, mMessageInputLayout, R.anim.push_up_in);
        Anim.hideViewWithAnimation(this, mNewMessageButton, R.anim.abc_fade_out);
        focusOnNewestMessage();
        mMessageListAdapter.notifyDataSetChanged();
    }

    public void focusOnNewestMessage() {
        mMessageListView.setSelection(mMessageListView.getCount() - 1);
    }

    public void sendButtonOnClick(View view) {
        sendMessage(mMessageInputText.getText().toString());
    }

    public void sendMessage(String message) {
        if ((message != null) && (!message.trim().equals(""))) {
            MessageDataManager.add(new MessageData(System.currentTimeMillis(), mUsername, message));
            mMessageListAdapter.notifyDataSetChanged();
            focusOnNewestMessage();
            mMessageInputText.setText(null);
        }
    }

    public boolean validateUser(String username, String password) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        if (username != null && password != null) {
            if (username.equals("admin") && password.equals("nimda")) {
                return true;
            }
            loginIntent.putExtra(Extras.ERROR, getString(R.string.login_wrong_username_or_password));
        }
        startActivity(loginIntent);
        finish();
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mMessageInputLayout.getVisibility() == View.VISIBLE) {
            Anim.hideViewWithAnimation(this, mMessageInputLayout, R.anim.push_down_out);
            Anim.showViewWithAnimation(this, mNewMessageButton, R.anim.abc_fade_in);
        } else {
            finish();
            overridePendingTransition(R.anim.push_up_in, R.anim.push_down_out);
        }
    }
}