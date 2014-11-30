package hu.denield.chatly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFloat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingRightInAnimationAdapter;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import hu.denield.chatly.adapter.MessageListAdapter;
import hu.denield.chatly.constant.Extras;
import hu.denield.chatly.constant.Fragments;
import hu.denield.chatly.constant.LogTag;
import hu.denield.chatly.constant.Mqtt;
import hu.denield.chatly.data.MessageData;
import hu.denield.chatly.data.MessageDataManager;
import hu.denield.chatly.data.MessageProto;
import hu.denield.chatly.mqtt.AndroidClient;
import hu.denield.chatly.mqtt.MqttCallbackHandler;
import hu.denield.chatly.util.Anim;
import hu.denield.chatly.util.StringHelper;

/**
 * The entry point of the application.
 */
public class MainActivity extends BaseActivity implements LocationListener,
                                                          GooglePlayServicesClient.ConnectionCallbacks,
                                                          GooglePlayServicesClient.OnConnectionFailedListener {

    public static final int TRAFFIC_UPDATE_INTERVAL = 2000;

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String MESSAGES = "messages";
    public static final String MESSAGE_ADAPTER = "messageAdapter";

    public static final String CHAT_INPUT = "chatInput";
    public static final String CHAT_INPUT_STATE = "chatInputState";
    public static final String NAVIGATION_DRAWER_STATE = "navigationDrawerState";

    private Chatly app;

    private TrafficStats mStats;

    private Menu mMenu;

    private SharedPreferences mSp;

    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;

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
    private TextView mDownloadedTextView;
    private TextView mUploadedTextView;

    private Runnable mTrafficUpdateRunnable;
    private Handler mTrafficUpdateHandler;

    private MqttAndroidClient mClient;

    private MessageReceiver mReceiver;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (Chatly) getApplication();

        // set the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // initialize shared preferences
        mSp = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // initialize traffic data
        mStats = new TrafficStats();

        // fresh start
        if (savedInstanceState == null) {
            if (getIntent().getExtras() != null) {
                // trying to login from Login form
                mUsername = getIntent().getStringExtra(Extras.USERNAME);
                mPassword = getIntent().getStringExtra(Extras.PASSWORD);
                mRemember = getIntent().getBooleanExtra(Extras.REMEMBER, false);

                SharedPreferences.Editor editor = mSp.edit();
                // save the login credentials for further use (next login)
                if (mRemember) {
                    editor.putBoolean(getString(R.string.pref_autologin), true);
                    editor.putString(getString(R.string.pref_username), mUsername);
                    editor.putString(getString(R.string.pref_password), mPassword);
                } else {
                    editor.putBoolean(getString(R.string.pref_autologin), false);
                    editor.remove(getString(R.string.pref_username));
                    editor.remove(getString(R.string.pref_password));
                }
                editor.apply();
            } else {
                // trying to login from SharedPreferences
                if (mSp.getBoolean(getString(R.string.pref_autologin), false)) {
                    mUsername = mSp.getString(getString(R.string.pref_username), null);
                    mPassword = mSp.getString(getString(R.string.pref_password), null);
                }
            }
        } else {
            // trying to login from savedInstanceState
            mUsername = savedInstanceState.getString(USERNAME);
            mPassword = savedInstanceState.getString(PASSWORD);
        }

        // validate the user
        if (!validateUser(mUsername, mPassword)) return;
        else {
            app.setUsername(mUsername);
            app.setPassword(mPassword);
        }

        // load the view from the inflated layout
        mMessageListView = (ListView) findViewById(R.id.listview_messages);
        mUsernameTextView = (TextView) findViewById(R.id.chat_username);
        mMessageInputLayout = (RelativeLayout) findViewById(R.id.chat_input_layout);
        mMessageInputText = (EditText) findViewById(R.id.chat_message_input);
        mNewMessageButton = (ButtonFloat) findViewById(R.id.chat_button_new_message);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // load the view from the inflated layout to local variables and do some things with them
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

        // list adapter
        MessageListAdapter baseAdapter = new MessageListAdapter(this, MessageDataManager.getInstance().getMessages());
        mMessageListAdapter = new SwingRightInAnimationAdapter(baseAdapter);
        mMessageListView.setAdapter(mMessageListAdapter);
        mMessageListAdapter.setAbsListView(mMessageListView);

        mMessageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                MessageData message = MessageDataManager.getInstance().getMessages().get(position);
                if (message != null && message.getLocation() != null && message.getLocation().getLongitude() != 0.0) {
                    Toast.makeText(MainActivity.this, message.getLocation().getLatitude() +", "+ message.getLocation().getLongitude(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.gps_no_data, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // initialize drawer
        initializeDrawer();

        // initialize and connect to mqtt broker if needed
        initializeMqttClient();

        // location
        initializeLocationClient();

        // message receiver
        mReceiver = new MessageReceiver();
        mIntentFilter = new IntentFilter(Mqtt.RECEIVER_MESSAGE_RECEIVED);
    }

    /**
     * Initialize the Paho's MQTT Client.
     */

    private void initializeMqttClient() {
        // The basic client information
        String clientId =  MqttClient.generateClientId();
        String uri = getString(R.string.mqtt_broker_protocoll);
        String server = getString(R.string.mqtt_broker_url);
        String port = getString(R.string.mqtt_broker_port);
        uri = new StringBuilder(uri)
                .append(server)
                .append(":")
                .append(port)
                .toString();

        try {
            mClient = AndroidClient.getInstance(this, uri, clientId);
        } catch (MqttException e) {
            Log.e(LogTag.ERROR, e.getStackTrace().toString());
        }
    }

    /**
     * Initializes the Location API.
     */
    private void initializeLocationClient() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(90);
        mLocationRequest.setFastestInterval(30);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationClient = new LocationClient(this, this, this);
    }

    /**
     * Initialize the Navigation Drawer.
     */
    private void initializeDrawer() {
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

        mDownloadedTextView = (TextView) findViewById(R.id.drawer_downloaded);
        mUploadedTextView = (TextView) findViewById(R.id.drawer_uploaded);

        mTrafficUpdateRunnable = new Runnable() {

            @Override
            public void run() {
                updateTraffic();
                if (mTrafficUpdateHandler != null) {
                    mTrafficUpdateHandler.postDelayed(this, TRAFFIC_UPDATE_INTERVAL);
                }
            }
        };
        mTrafficUpdateHandler = new Handler();
        mTrafficUpdateHandler.post(mTrafficUpdateRunnable);
    }

    /**
     * Updates traffic data in the navigation drawer.
     */
    private void updateTraffic() {
        mDownloadedTextView.setText(": "+StringHelper.humanReadableByteCount(mStats.getUidRxBytes(app.getApplicationInfo().uid) - app.getDownloadedAtStart(), false));
        mUploadedTextView.setText(": "+StringHelper.humanReadableByteCount(mStats.getUidTxBytes(app.getApplicationInfo().uid) - app.getUploadedAtStart(), false));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.getParcelable(MESSAGES) != null) {
            mMessageListView.onRestoreInstanceState(savedInstanceState.getParcelable(MESSAGES));
        }
        if (savedInstanceState.getParcelable(MESSAGE_ADAPTER) != null) {
            mMessageListAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(MESSAGES));
            mMessageListView.setAdapter(mMessageListAdapter);
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
        savedInstanceState.putParcelable(MESSAGE_ADAPTER, mMessageListAdapter.onSaveInstanceState());
        savedInstanceState.putString(CHAT_INPUT, mMessageInputText.getText().toString());
        savedInstanceState.putBoolean(CHAT_INPUT_STATE, (mMessageInputLayout.getVisibility() == View.VISIBLE));
        savedInstanceState.putBoolean(NAVIGATION_DRAWER_STATE, mDrawerLayout.isDrawerOpen(GravityCompat.START));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTrafficUpdateHandler.post(mTrafficUpdateRunnable);
        mLocationClient.connect();
        mMessageListAdapter.notifyDataSetChanged();
        mClient.registerResources(this);
        mClient.setCallback(new MqttCallbackHandler(this));
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTrafficUpdateHandler.removeCallbacks(mTrafficUpdateRunnable);
        mLocationClient.disconnect();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mClient != null && mClient.isConnected()) {
            mClient.unregisterResources();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // sync the toggle state after onRestoreInstanceState has occurred
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_connect:
                if (mClient != null) {
                    try {
                        if (!mClient.isConnected()) {
                            mClient.connect();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.mqtt_connected, Toast.LENGTH_SHORT).show();
                            //mClient.disconnect(5);
                        }
                    } catch (MqttException e) {
                        Log.e(LogTag.ERROR, e.getStackTrace().toString());
                    }
                }
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, FragmentActivity.class);
                settingsIntent.putExtra(Extras.FRAGMENT, Fragments.FragmentName.SETTINGS.ordinal());
                startActivity(settingsIntent);
                return true;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, FragmentActivity.class);
                aboutIntent.putExtra(Extras.FRAGMENT, Fragments.FragmentName.ABOUT.ordinal());
                startActivity(aboutIntent);
                return true;
            case R.id.action_logout:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void newMessageOnClick(View view) {
        Anim.showViewWithAnimation(this, mMessageInputLayout, R.anim.push_up_in);
        Anim.hideViewWithAnimation(this, mNewMessageButton, R.anim.abc_fade_out);
    }

    /**
     * Focuses the listview which contains the messages
     * to the newest message.
     */
    public void focusOnNewestMessage() {
        mMessageListView.setSelection(mMessageListView.getCount() - 1);
    }

    public void sendButtonOnClick(View view) {
        sendMessage(mMessageInputText.getText().toString());
    }

    public void sendMessage(String message) {
        if (mClient == null || !mClient.isConnected()) {
            Toast.makeText(MainActivity.this, R.string.mqtt_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if ((message != null) && (!message.trim().equals(""))) {
            mMessageInputText.setText(null);
            try {
                if (mClient.isConnected()) {
                    mClient.publish(Mqtt.DEFAULT_TOPIC, createMessage(mUsername, message).toByteArray(), 0, false);
                }
            } catch (MqttException e) {
                Log.e(LogTag.ERROR, e.getStackTrace().toString());
            }
        }
    }

    /**
     * Creates a Protocol Buffers Message object
     * to send via MQTT.
     * @param username The username.
     * @param message The message.
     * @return The encrypted message data.
     */
    private MessageProto.Message createMessage(String username, String message) {
        MessageProto.Message.Builder builder = MessageProto.Message.newBuilder()
                .setName(username)
                .setMessage(message);
        Location location = app.getLocation();
        if (location != null) {
            builder.setLocation(
                    MessageProto.Message.Location.newBuilder()
                            .setLatitude((float) location.getLatitude())
                            .setLongitude((float) location.getLongitude())
                            .build());
        }
        return builder.build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        mLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        app.setLocation(location);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Mqtt.RECEIVER_MESSAGE_RECEIVED)) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mMessageListAdapter.notifyDataSetChanged();
                        focusOnNewestMessage();
                    }
                }, 500);
            }
        }
    }

    /**
     * Checks if the username and password are valid.
     *
     * @param username The username.
     * @param password The password.
     * @return true, if the data is valid, false otherwise.
     */
    public boolean validateUser(String username, String password) {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        if (username != null && password != null) {
            if (username.length() >= 3 && password.length() >= 3) {
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