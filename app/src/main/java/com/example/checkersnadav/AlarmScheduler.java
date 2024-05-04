package com.example.checkersnadav;

import static android.content.Context.ALARM_SERVICE;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.TimeZone;

public class AlarmScheduler {
    public static void scheduleMidnightAlarm(Context context, Activity activity)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Setting alarm time for midnight
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Add a day in case the calendar is already behind the system at the time of creating the alarm
        // For example, if we just passed midnight in our case, we are making sure to not set the alarm to the midnight
        // that just passed, but rather to the next one.
        if (calendar.getTimeInMillis() < System.currentTimeMillis())
        {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

}
