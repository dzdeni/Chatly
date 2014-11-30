package hu.denield.chatly.mqtt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import hu.denield.chatly.constant.Mqtt;
import hu.denield.chatly.data.MessageProto;

/**
 * Handles the callbacks of the AndroidMqttClient
 */
public class MqttCallbackHandler implements MqttCallback {

    private Context context;

    public MqttCallbackHandler(Context context) {
        this.context = context;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        // do nothing
    }

    /**
     * When a new mqtt message arrive at a topic we subscribed, it will send a broadcast with its data.
     * @param topic The name of the topic.
     * @param mqttMessage The MQTT message.
     * @throws Exception
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        MessageProto.Message parsedMessage;
        parsedMessage = MessageProto.Message.parseFrom(mqttMessage.getPayload());

        if (parsedMessage != null) {
            Intent i = new Intent(Mqtt.RECEIVER_MESSAGE_RECEIVED);
            i.putExtra(Mqtt.MESSAGE_NAME, parsedMessage.getName());
            i.putExtra(Mqtt.MESSAGE_MESSAGE, parsedMessage.getMessage());
            if (parsedMessage.hasLocation()) {
                i.putExtra(Mqtt.MESSAGE_LATITUDE, parsedMessage.getLocation().getLatitude());
                i.putExtra(Mqtt.MESSAGE_LONGITUDE, parsedMessage.getLocation().getLongitude());
            }
            context.sendBroadcast(i);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        // do nothing
    }
}
