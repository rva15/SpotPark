package com.example.android.sp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public NotificationManager notificationManager;
    int id;

    public void onReceive(Context context, Intent intent) {

        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);


        id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(id, notification);


    }

}