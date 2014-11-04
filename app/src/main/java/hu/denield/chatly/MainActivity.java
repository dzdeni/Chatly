package hu.denield.chatly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import hu.denield.chatly.adapter.MessageListAdapter;
import hu.denield.chatly.contants.Extras;
import hu.denield.chatly.contants.SharedPrefs;
import hu.denield.chatly.data.MessageData;
import hu.denield.chatly.data.MessageDataManager;
import hu.denield.chatly.utils.Anim;

public class MainActivity extends BaseActivity {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String MESSAGES = "messages";

    private SharedPreferences sp;
    private ListView messageListView;
    private MessageListAdapter messageListAdapter;

    private String username;
    private String password;
    private boolean remember;

    private TextView usernameTextView;
    private ImageButton newMessage;
    private EditText messageInput;
    private RelativeLayout messageInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = this.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // Fresh start
        if (savedInstanceState == null) {
            if (getIntent().getExtras() != null) {
                // Trying to login
                username = getIntent().getStringExtra(Extras.USERNAME);
                password = getIntent().getStringExtra(Extras.PASSWORD);
                remember = getIntent().getBooleanExtra(Extras.REMEMBER, false);
            } else {
                // Trying to get login from SharedPreferences
                username = sp.getString(SharedPrefs.USERNAME, null);
                password = sp.getString(SharedPrefs.PASSWORD, null);
            }
        } else {
            // Trying to login from savedInstanceState
            username = savedInstanceState.getString(USERNAME);
            password = savedInstanceState.getString(PASSWORD);
        }

        if (!validateUser(username, password)) return;

        // Save the login credentials for further use (next login)
        if (remember) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(SharedPrefs.USERNAME, username);
            editor.putString(SharedPrefs.PASSWORD, password);
            editor.apply();
        }

        // Load the view from the inflated layout
        messageListView = (ListView) findViewById(R.id.listview_messages);
        usernameTextView = (TextView) findViewById(R.id.chat_username);
        messageInputLayout = (RelativeLayout) findViewById(R.id.chat_input_layout);
        messageInput = (EditText) findViewById(R.id.chat_message_input);
        newMessage = (ImageButton) findViewById(R.id.chat_button_new_message);

        usernameTextView.setText(username +": ");
        //messageInputLayout.setVisibility(View.GONE);

        messageListAdapter = new MessageListAdapter(this, MessageDataManager.getInstance().getUsers());
        messageListView.setAdapter(messageListAdapter);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        messageListView.onRestoreInstanceState(savedInstanceState.getParcelable(MESSAGES));
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(USERNAME, username);
        savedInstanceState.putString(PASSWORD, password);
        savedInstanceState.putParcelable(MESSAGES, messageListView.onSaveInstanceState());
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
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
                SharedPreferences.Editor editor = sp.edit();
                editor.remove(SharedPrefs.USERNAME);
                editor.remove(SharedPrefs.PASSWORD);
                editor.apply();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void newMessageOnClick(View view) {
        Anim.showViewWithAnimation(this, messageInputLayout, R.anim.push_up_in);
        Anim.hideViewWithAnimation(this, newMessage, R.anim.abc_fade_out);
        if (messageInput.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        focusOnNewestMessage();
        messageListAdapter.notifyDataSetChanged();
    }

    public void focusOnNewestMessage() {
        messageListView.setSelection(messageListView.getCount() - 1);
    }

    public void sendButtonOnClick(View view) {
        String message = messageInput.getText().toString();
        if (!message.trim().equals("")) {
            MessageDataManager.add(new MessageData(System.currentTimeMillis(), username, message));
            messageListAdapter.notifyDataSetChanged();
            focusOnNewestMessage();
            messageInput.setText(null);
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
        if (messageInputLayout.getVisibility() == View.VISIBLE) {
            Anim.hideViewWithAnimation(this, messageInputLayout, R.anim.push_down_out);
            Anim.showViewWithAnimation(this, newMessage, R.anim.abc_fade_in);
        } else {
            finish();
            overridePendingTransition(R.anim.push_up_in, R.anim.push_down_out);
        }
    }
}
