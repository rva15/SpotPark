package com.example.android.sp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ruturaj on 1/19/17.
 */
public class CancelNotification extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationId = intent.getIntExtra("notificationId", 0);

        // if you want cancel notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }
}
