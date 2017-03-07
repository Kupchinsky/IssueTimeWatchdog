package ru.kupchinskiy.issuetimewatchdog.helper;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Calendar;

import roboguice.RoboGuice;
import ru.kupchinskiy.issuetimewatchdog.receiver.IssueRestartWorkReceiver;

@Singleton
public class IssueAlarms {

    @Inject
    private AlarmManager alarmManager;

    private Context context;

    @Inject
    public IssueAlarms(Application application) {
        context = application;

        RoboGuice.injectMembers(context, this);

        disableRestartWorkAlarm();
        scheduleRestartWorkAlarm();
    }

    private void scheduleRestartWorkAlarm() {
        Intent intent = new Intent(context, IssueRestartWorkReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void disableRestartWorkAlarm() {
        Intent intent = new Intent(context, IssueRestartWorkReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.cancel(pendingIntent);
    }

}
