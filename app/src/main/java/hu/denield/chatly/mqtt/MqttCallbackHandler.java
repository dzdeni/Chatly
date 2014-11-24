package hu.denield.chatly.mqtt;

import android.content.Context;
import android.content.Intent;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import hu.denield.chatly.constant.Mqtt;
import hu.denield.chatly.data.MessageProto;

public class MqttCallbackHandler implements MqttCallback {

    private Context context;

    public MqttCallbackHandler(Context context) {
        this.context = context;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        // do nothing
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        MessageProto.Message parsedMessage;
        parsedMessage = MessageProto.Message.parseFrom(mqttMessage.getPayload());

        if (parsedMessage != null) {
            Intent i = new Intent(Mqtt.RECEIVER_MESSAGE_RECEIVED);
            i.putExtra(Mqtt.MESSAGE_NAME, parsedMessage.getName());
            i.putExtra(Mqtt.MESSAGE_MESSAGE, parsedMessage.getMessage());

            context.sendBroadcast(i);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        // do nothing
    }
}
