package com.example.checkersnadav;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

/**
 * Broadcast receiver responsible for handling alarms.
 * Specifically, it triggers notifications related to daily bonuses.
 */
public class AlarmReceiver extends BroadcastReceiver
{
    public static final String CHANNEL_ID = "0";
    public static final String CHANNEL_NAME = "Daily Bonus Reminders";

    /**
     * Called when the BroadcastReceiver receives an Intent broadcast.
     * This method is triggered when an alarm goes off.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        makeNotification(context);
    }

    /**
     * Creates and displays a notification to remind the user about their daily bonus.
     * This method sets up a notification channel if it does not already exist, and
     * sends out a notification.
     *
     * @param context The context used for creating the notification.
     */
    private void makeNotification(Context context)
    {
        // Create a notification using the Builder pattern
        NotificationCompat.Builder notify = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID);
        notify.setContentTitle("Daily Bonus Reminder")
                .setContentText("Don't forget to play a game today to earn your daily triple win bonus if you haven't already!")
                .setSmallIcon(R.drawable.logo)
                .setChannelId(CHANNEL_ID);

        // Create the Notification Channel
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        // Register the channel with the system
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        manager.notify(1, notify.build());
    }
}
