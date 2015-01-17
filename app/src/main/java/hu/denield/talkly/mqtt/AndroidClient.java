package hu.denield.talkly.mqtt;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * A simple wrapper for the Paho's MqttAndroidClient
 */
public class AndroidClient {

    private static MqttAndroidClient instance;

    public static MqttAndroidClient getInstance(Context context, String uri, String clientId) throws MqttException {
        if (instance == null) {
            instance = new MqttAndroidClient(context, uri, clientId);

            instance.setCallback(new MqttCallbackHandler(context));
            instance.setTraceCallback(new MqttTraceCallback());

            final MqttActionListener callback = new MqttActionListener(context,
                    MqttActionListener.Action.CONNECT, instance);

            int timeout = 10;
            int keepalive = 60;
            boolean cleanSession = true;

            MqttConnectOptions conOpt = new MqttConnectOptions();

            conOpt.setCleanSession(cleanSession);
            conOpt.setConnectionTimeout(timeout);
            conOpt.setKeepAliveInterval(keepalive);

            instance.connect(conOpt, null, callback);
        }
        return instance;
    }
}