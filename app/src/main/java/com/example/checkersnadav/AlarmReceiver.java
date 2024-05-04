package com.example.checkersnadav;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "reminder_channel_id"; // Use a constant ID for the notification channel
    private static final String CHANNEL_NAME = "Reminder Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for daily reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Alarm received.", Toast.LENGTH_SHORT).show();
        makeNotification(context);
    }

    private void makeNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.mini_logo)
                .setContentTitle("Your Title")
                .setContentText("Your text")
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }

        // Intent to launch when the notification is clicked
        Intent notificationIntent = new Intent(context, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        builder.setContentIntent(pendingIntent);

        notificationManager.notify(0, builder.build()); // ID 0 signifies the ID of the notification
    }
}
