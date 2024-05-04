package com.example.checkersnadav;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "0";
    public static final String CHANNEL_NAME = "Daily Bonus Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        makeNotification(context);
    }

    private void makeNotification(Context context) {

        // create a notification - notice the same CHANNEL_ID!!
        NotificationCompat.Builder notify = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID);
        notify.setContentTitle("Daily Bonus Reminder");
        notify.setContentText("Don't forget to play a game today to earn your daily triple win bonus if you haven't already!");
        notify.setSmallIcon(R.drawable.logo);
        notify.setChannelId(CHANNEL_ID);

        // create the notification channel
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        NotificationManager manager = (NotificationManager) (context.getSystemService(Context.NOTIFICATION_SERVICE));
        manager.createNotificationChannel(channel);
        manager.notify(1, notify.build());
    }
}