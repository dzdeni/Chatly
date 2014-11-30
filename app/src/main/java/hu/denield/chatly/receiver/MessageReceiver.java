package hu.denield.chatly.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import hu.denield.chatly.constant.Mqtt;
import hu.denield.chatly.data.MessageData;
import hu.denield.chatly.data.MessageDataManager;
import hu.denield.chatly.util.Notify;

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
            String username = intent.getStringExtra(Mqtt.MESSAGE_NAME);
            String message = intent.getStringExtra(Mqtt.MESSAGE_MESSAGE);

            Location location = new Location("dummyprovider");
            Float latitude = intent.getFloatExtra(Mqtt.MESSAGE_LATITUDE, 0);
            Float longitude = intent.getFloatExtra(Mqtt.MESSAGE_LONGITUDE, 0);
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            MessageDataManager.add(new MessageData(System.currentTimeMillis(), username, message, location));
            Notify.notifcation(context, username, message, Mqtt.NOTIFICATION_ID);
        }
    }
}