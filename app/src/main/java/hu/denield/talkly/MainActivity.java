package hu.denield.talkly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.Files;
import com.skyfishjy.library.RippleBackground;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.IOException;

import hu.denield.talkly.constant.Extras;
import hu.denield.talkly.constant.Fragments;
import hu.denield.talkly.constant.LogTag;
import hu.denield.talkly.constant.Mqtt;
import hu.denield.talkly.data.MessageProto;
import hu.denield.talkly.mqtt.AndroidClient;
import hu.denield.talkly.mqtt.MqttCallbackHandler;
import hu.denield.talkly.util.StringHelper;

/**
 * The entry point of the application.
 */
public class MainActivity extends BaseActivity {

    public static final int TRAFFIC_UPDATE_INTERVAL = 2000;

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String NAVIGATION_DRAWER_STATE = "navigationDrawerState";

    private Talkly app;

    private TrafficStats mStats;

    private Menu mMenu;

    private SharedPreferences mSp;

    private String mUsername;
    private String mPassword;
    private boolean mRemember;

    private ImageButton mRecordButton;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView mDownloadedTextView;
    private TextView mUploadedTextView;

    private Runnable mTrafficUpdateRunnable;
    private Handler mTrafficUpdateHandler;

    private MqttAndroidClient mClient;

    private MessageReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private String mFileName = null;
    private String mPlayFileName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);

        app = (Talkly) getApplication();

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
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // initialize the audio file
        mFileName = getCacheDir().getAbsolutePath();
        mFileName += "/voicerecording.3gp";

        mPlayFileName = getCacheDir().getAbsolutePath();
        mPlayFileName += "/incoming.3gp";

        // initialize the record button
        mRecordButton = (ImageButton) findViewById(R.id.recordButton);
        mRecordButton.setOnTouchListener(new RecordingListener());

        // load the view from the inflated layout to local variables and do some things with them
        TextView drawerUsernameTextView = (TextView) findViewById(R.id.drawer_username);
        drawerUsernameTextView.setText(mUsername);

        // initialize drawer
        initializeDrawer();

        // initialize and connect to mqtt broker if needed
        initializeMqttClient();

        // message receiver
        mReceiver = new MessageReceiver();
        mIntentFilter = new IntentFilter(Mqtt.RECEIVER_MESSAGE_RECEIVED);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mPlayFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LogTag.ERROR, "prepare() failed");
        }
    }

    private void stopPlaying() {
        if ((mPlayer != null) && (mPlayer.isPlaying())) mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LogTag.ERROR, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch(RuntimeException stopException){
                //handle cleanup here
            }
            mRecorder.release();
            mRecorder = null;
        }
    }

    public class RecordingListener implements View.OnTouchListener {
        final RippleBackground rippleBackground = (RippleBackground) findViewById(R.id.content);
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    rippleBackground.startRippleAnimation();
                    stopPlaying();
                    startRecording();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    rippleBackground.stopRippleAnimation();
                    stopRecording();
                    sendVoice(mFileName);
                    break;
            }
            return true;
        }
    }

    /**
     * Initialize the Paho's MQTT Client.
     */
    private void initializeMqttClient() {
        // The basic client information
        String clientId = MqttClient.generateClientId();
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
     * Initialize the Navigation Drawer.
     */
    private void initializeDrawer() {
        // drawer toogle (lollipop's hamburger to arrow)
        mDrawerToggle = new ActionBarDrawerToggle(
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
        mDownloadedTextView.setText(": " + StringHelper.humanReadableByteCount(mStats.getUidRxBytes(app.getApplicationInfo().uid) - app.getDownloadedAtStart(), false));
        mUploadedTextView.setText(": " + StringHelper.humanReadableByteCount(mStats.getUidTxBytes(app.getApplicationInfo().uid) - app.getUploadedAtStart(), false));
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_main;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.getBoolean(NAVIGATION_DRAWER_STATE))
            mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(USERNAME, mUsername);
        savedInstanceState.putString(PASSWORD, mPassword);
        savedInstanceState.putBoolean(NAVIGATION_DRAWER_STATE, mDrawerLayout.isDrawerOpen(GravityCompat.START));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTrafficUpdateHandler.post(mTrafficUpdateRunnable);
        mClient.registerResources(this);
        mClient.setCallback(new MqttCallbackHandler(this));
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTrafficUpdateHandler.removeCallbacks(mTrafficUpdateRunnable);
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
                quit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendVoice(String fileName) {
        if (mClient == null || !mClient.isConnected()) {
            Toast.makeText(MainActivity.this, R.string.mqtt_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (mClient.isConnected()) {
                File file = new File(fileName);

                if (file.exists()) {
                    mClient.publish(Mqtt.DEFAULT_TOPIC + "/" + mUsername, Files.toByteArray(file), 0, false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MqttException e) {
                Log.e(LogTag.ERROR, e.getStackTrace().toString());
        }
    }

    /**
     * A broadcast receiver that receives the MQTT message,
     * update the messages with its data and notify the user
     * about it.
     */
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Mqtt.RECEIVER_MESSAGE_RECEIVED)) {
                String topic = intent.getStringExtra(Mqtt.MESSAGE_TOPIC);
                if (!topic.endsWith(mUsername)) {
                    File file = new File(mPlayFileName);
                    try {
                        Files.write(intent.getByteArrayExtra(Mqtt.MESSAGE_PAYLOAD), file);
                        startPlaying();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Creates a Protocol Buffers Message object
     * to send via MQTT.
     *
     * @param username The username.
     * @param message  The message.
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

    private void quit() {
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        quit();
    }
}