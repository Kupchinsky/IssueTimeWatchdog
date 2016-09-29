package ru.killer666.issuetimewatchdog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Calendar;

@Singleton
public class CreateTimeRecordsSettings {
    private static final String PREFS_ENABLED = "enabled";

    private Context context;

    @Inject
    private CreateTimeRecordsBroadcastReceiver alarmBroadcastReceiver;

    private final SharedPreferences preferences;

    @Inject
    private CreateTimeRecordsSettings(android.app.Application application) {
        this.context = application;
        this.preferences = this.context.getSharedPreferences("create_time_records_settings", Context.MODE_PRIVATE);

        this.updateAlarm();
    }

    private void updateAlarm() {
        if (this.isEnabled()) {
            this.scheduleCreateTimeRecords();
        } else {
            this.disableCreateTimeRecords();
        }
    }

    public void setEnabled(boolean enabled) {
        SharedPreferences.Editor editor = this.preferences.edit();

        editor.putBoolean(PREFS_ENABLED, enabled);
        editor.apply();

        this.updateAlarm();
    }

    private boolean isEnabled() {
        return this.preferences.getBoolean(PREFS_ENABLED, false);
    }

    private void scheduleCreateTimeRecords() {
        Intent intent = new Intent(this.context, this.alarmBroadcastReceiver.getClass());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        AlarmManager alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void disableCreateTimeRecords() {
        Intent intent = new Intent(this.context, this.alarmBroadcastReceiver.getClass());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
