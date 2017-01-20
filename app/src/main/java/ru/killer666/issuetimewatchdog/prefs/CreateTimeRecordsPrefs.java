package ru.killer666.issuetimewatchdog.prefs;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.text.DateFormat;
import java.util.Calendar;

import roboguice.RoboGuice;
import ru.killer666.issuetimewatchdog.receiver.CreateTimeRecordsReceiver;

@Singleton
public class CreateTimeRecordsPrefs {

    private static final String PREFS_ENABLED = "enabled";

    private final Context context;
    private final SharedPreferences preferences;

    @Inject
    private AlarmManager alarmManager;

    @Inject
    private CreateTimeRecordsPrefs(Application application) {
        context = application;
        preferences = context.getSharedPreferences("create_time_records_prefs", Context.MODE_PRIVATE);

        RoboGuice.injectMembers(context, this);

        updateAlarm(false);
    }

    private void updateAlarm(boolean showToast) {
        if (isEnabled()) {
            scheduleCreateTimeRecords(showToast);
        } else {
            disableCreateTimeRecords();
        }
    }

    public void setEnabled(boolean enabled) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(PREFS_ENABLED, enabled);
        editor.apply();

        updateAlarm(true);
    }

    public boolean isEnabled() {
        return preferences.getBoolean(PREFS_ENABLED, false);
    }

    private void scheduleCreateTimeRecords(boolean showToast) {
        Intent intent = new Intent(context, CreateTimeRecordsReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        if (showToast) {
            DateFormat dateTimeFormatter = DateFormat.getDateTimeInstance();
            Toast.makeText(context, "Next update will be at " + dateTimeFormatter.format(calendar.getTime()),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void disableCreateTimeRecords() {
        Intent intent = new Intent(context, CreateTimeRecordsReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.cancel(pendingIntent);
    }

}
