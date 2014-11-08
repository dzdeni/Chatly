package hu.denield.chatly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;

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

    private SharedPreferences mSp;
    private ListView mMessageListView;
    private MessageListAdapter mMessageListAdapter;

    private String username;
    private String password;
    private boolean remember;

    private TextView mUsernameTextView;
    private ButtonFloat mNewMessage;
    private EditText mMessageInput;
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
                username = getIntent().getStringExtra(Extras.USERNAME);
                password = getIntent().getStringExtra(Extras.PASSWORD);
                remember = getIntent().getBooleanExtra(Extras.REMEMBER, false);
            } else {
                // Trying to get login from SharedPreferences
                username = mSp.getString(SharedPrefs.USERNAME, null);
                password = mSp.getString(SharedPrefs.PASSWORD, null);
            }
        } else {
            // Trying to login from savedInstanceState
            username = savedInstanceState.getString(USERNAME);
            password = savedInstanceState.getString(PASSWORD);
        }

        if (!validateUser(username, password)) return;

        // Save the login credentials for further use (next login)
        if (remember) {
            SharedPreferences.Editor editor = mSp.edit();
            editor.putString(SharedPrefs.USERNAME, username);
            editor.putString(SharedPrefs.PASSWORD, password);
            editor.apply();
        }

        // Load the view from the inflated layout
        mMessageListView = (ListView) findViewById(R.id.listview_messages);
        mUsernameTextView = (TextView) findViewById(R.id.chat_username);
        mMessageInputLayout = (RelativeLayout) findViewById(R.id.chat_input_layout);
        mMessageInput = (EditText) findViewById(R.id.chat_message_input);
        mNewMessage = (ButtonFloat) findViewById(R.id.chat_button_new_message);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Load the view from the inflated layout to local variables and do some things with them
        TextView drawerUsernameTextView = (TextView) findViewById(R.id.drawer_username);
        drawerUsernameTextView.setText(username);

        mUsernameTextView.setText(username + ": ");
        mMessageInputLayout.setVisibility(View.GONE);

        mMessageListAdapter = new MessageListAdapter(this, MessageDataManager.getInstance().getUsers());
        mMessageListView.setAdapter(mMessageListAdapter);

        mDrawerToggle = new ActionBarDrawerToggle (
                this,
                mDrawerLayout,
                getToolbar(),
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle("#default");
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
                getSupportActionBar().setTitle(R.string.app_name);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mMessageListView.onRestoreInstanceState(savedInstanceState.getParcelable(MESSAGES));
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(USERNAME, username);
        savedInstanceState.putString(PASSWORD, password);
        savedInstanceState.putParcelable(MESSAGES, mMessageListView.onSaveInstanceState());
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
        Anim.hideViewWithAnimation(this, mNewMessage, R.anim.abc_fade_out);
        if (mMessageInput.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        focusOnNewestMessage();
        mMessageListAdapter.notifyDataSetChanged();
    }

    public void focusOnNewestMessage() {
        mMessageListView.setSelection(mMessageListView.getCount() - 1);
    }

    public void sendButtonOnClick(View view) {
        String message = mMessageInput.getText().toString();
        if (!message.trim().equals("")) {
            MessageDataManager.add(new MessageData(System.currentTimeMillis(), username, message));
            mMessageListAdapter.notifyDataSetChanged();
            focusOnNewestMessage();
            mMessageInput.setText(null);
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
            Anim.showViewWithAnimation(this, mNewMessage, R.anim.abc_fade_in);
        } else {
            finish();
            overridePendingTransition(R.anim.push_up_in, R.anim.push_down_out);
        }
    }
}
