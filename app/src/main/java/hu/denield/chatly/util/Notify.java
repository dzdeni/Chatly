package hu.denield.chatly.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import hu.denield.chatly.MainActivity;
import hu.denield.chatly.R;

public class Notify {
    public static void notifcation(Context context, String title, String subtitle, int NotificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // create a notification builder
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .setAutoCancel(true);

        // set the intent
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notificationBuilder.setContentIntent(pendingIntent);

        // building
        Notification notification = notificationBuilder.build();

        // send the notification
        notificationManager.notify(NotificationId, notification);
    }
}