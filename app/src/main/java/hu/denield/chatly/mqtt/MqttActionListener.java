package hu.denield.chatly.mqtt;

import android.content.Context;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import hu.denield.chatly.R;
import hu.denield.chatly.constant.Mqtt;

/**
 * This Class handles receiving information
 * the action
 */
public class MqttActionListener implements IMqttActionListener {

    /**
     * Actions that can be performed Asynchronously <strong>and</strong> associated with a
     * {@link MqttActionListener} object
     *
     */
    public enum Action {
        /** Connect Action **/
        CONNECT,
        /** Disconnect Action **/
        DISCONNECT,
        /** Subscribe Action **/
        SUBSCRIBE,
        /** Publish Action **/
        PUBLISH
    }

    /**
     * The {@link Action} that is associated with this instance of
     * <code>ActionListener</code>
     **/
    private Action action;
    private Context context;
    private MqttAndroidClient client;

    public MqttActionListener(Context context, Action action, MqttAndroidClient client) {
        this.context = context;
        this.action = action;
        this.client = client;
    }

    /**
     * The action associated with this listener has been successful.
     **/
    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        switch (action) {
            case CONNECT :
                connect();
                break;
            case DISCONNECT :
                disconnect();
                break;
            case SUBSCRIBE :
                subscribe();
                break;
            case PUBLISH :
                publish();
                break;
        }

    }

    /**
     * A publish action has been successfully completed, update connection
     * object associated with the client this action belongs to, then notify the
     * user of success
     */
    private void publish() {

    }

    /**
     * A subscribe action has been successfully completed, update the connection
     * object associated with the client this action belongs to and then notify
     * the user of success
     */
    private void subscribe() {

    }

    /**
     * A disconnection action has been successfully completed, update the
     * connection object associated with the client this action belongs to and
     * then notify the user of success.
     */
    private void disconnect() {
        Toast.makeText(context, context.getString(R.string.mqtt_not_connected), Toast.LENGTH_SHORT).show();
    }

    /**
     * A connection action has been successfully completed, update the
     * connection object associated with the client this action belongs to and
     * then notify the user of success.
     */
    private void connect() {
        Toast.makeText(context, context.getString(R.string.mqtt_connected), Toast.LENGTH_SHORT).show();
        try {
            client.subscribe(Mqtt.DEFAULT_TOPIC, 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * The action associated with the object was a failure
     *
     * @param token
     *            This argument is not used
     * @param exception
     *            The exception which indicates why the action failed
     */
    @Override
    public void onFailure(IMqttToken token, Throwable exception) {
        switch (action) {
            case CONNECT :
                connect(exception);
                break;
            case DISCONNECT :
                disconnect(exception);
                break;
            case SUBSCRIBE :
                subscribe(exception);
                break;
            case PUBLISH :
                publish(exception);
                break;
        }

    }

    /**
     * A publish action was unsuccessful, notify user and update client history
     *
     * @param exception
     *            This argument is not used
     */
    private void publish(Throwable exception) {

    }

    /**
     * A subscribe action was unsuccessful, notify user and update client history
     * @param exception This argument is not used
     */
    private void subscribe(Throwable exception) {

    }

    /**
     * A disconnect action was unsuccessful, notify user and update client history
     * @param exception This argument is not used
     */
    private void disconnect(Throwable exception) {
        Toast.makeText(context, context.getString(R.string.mqtt_disconnection_failure), Toast.LENGTH_SHORT).show();
    }

    /**
     * A connect action was unsuccessful, notify the user and update client history
     * @param exception This argument is not used
     */
    private void connect(Throwable exception) {
        Toast.makeText(context, context.getString(R.string.mqtt_connection_failure), Toast.LENGTH_SHORT).show();
    }

}