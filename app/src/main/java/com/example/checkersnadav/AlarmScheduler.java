package com.example.checkersnadav;

import static android.content.Context.ALARM_SERVICE;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Utility class for scheduling alarms.
 */
public class AlarmScheduler
{

    /**
     * Schedules an alarm that will trigger at midnight based on the timezone specified ("Asia/Jerusalem").
     * This alarm triggers daily notifications reminding the user to collect their daily bonus.
     *
     * @param context The Context in which the PendingIntent and AlarmManager are created.
     * @param activity The Activity from which the getSystemService is called to retrieve the AlarmManager.
     */
    public static void scheduleMidnightAlarm(Context context, Activity activity)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        // Creating a PendingIntent that fires when the alarm triggers
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Setting the alarm time for midnight using the specified timezone
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Ensure the alarm is set for the next midnight if it's already past midnight today
        if (calendar.getTimeInMillis() < System.currentTimeMillis())
        {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Set the alarm to fire at the calculated time, allowing it to trigger even during device idle times
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
