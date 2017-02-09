package com.app.android.sp;
//All imports
import android.content.BroadcastReceiver;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    private NotificationManager notificationManager;
    private int id;

    public void onReceive(Context context, Intent intent) {

        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(id, notification);
    }

}