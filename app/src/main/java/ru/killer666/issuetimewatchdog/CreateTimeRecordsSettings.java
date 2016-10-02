package ru.killer666.issuetimewatchdog;

        import android.app.AlarmManager;
        import android.app.PendingIntent;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;

        import com.google.inject.Inject;
        import com.google.inject.Singleton;

        import java.util.Calendar;

        import roboguice.RoboGuice;

@Singleton
public class CreateTimeRecordsSettings {
    private static final String PREFS_ENABLED = "enabled";

    private final Context context;
    private final SharedPreferences preferences;

    @Inject
    private AlarmManager alarmManager;

    @Inject
    private CreateTimeRecordsSettings(android.app.Application application) {
        this.context = application;
        this.preferences = this.context.getSharedPreferences("create_time_records_settings", Context.MODE_PRIVATE);

        RoboGuice.injectMembers(context, this);

        this.updateAlarm();
    }

    private void updateAlarm() {
        if (this.isEnabled()) {
            this.scheduleCreateTimeRecords();
        } else {
            this.disableCreateTimeRecords();
        }
    }

    void setEnabled(boolean enabled) {
        SharedPreferences.Editor editor = this.preferences.edit();

        editor.putBoolean(PREFS_ENABLED, enabled);
        editor.apply();

        this.updateAlarm();
    }

    boolean isEnabled() {
        return this.preferences.getBoolean(PREFS_ENABLED, false);
    }

    private void scheduleCreateTimeRecords() {
        Intent intent = new Intent(this.context, CreateTimeRecordsBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE, 10);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        this.alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void disableCreateTimeRecords() {
        Intent intent = new Intent(this.context, CreateTimeRecordsBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context, 0, intent, 0);

        this.alarmManager.cancel(pendingIntent);
    }
}
