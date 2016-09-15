package ru.killer666.issuetimewatchdog;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;

import lombok.NonNull;

public class WatchdogService extends Service {
    private static final int TIME_RECORD_DELAY = 60;
    private static final int TIME_RECORD_DELAY_MS = TIME_RECORD_DELAY * 1000;

    public static final String EXTRA_TIME_RECORD = "timeRecord";

    private TimeRecord timeRecord;

    private long postTime;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        private Date getEndOfDay(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);

            return calendar.getTime();
        }

        private Date getStartOfDay(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar.getTime();
        }

        private float convertToHours(long time) {
            return time / TIME_RECORD_DELAY_MS / (float) TIME_RECORD_DELAY;
        }

        @Override
        public void run() {
            float increaseTime;

            // Если день сменился, то создаём новый TimeRecord
            if (!DateUtils.isToday(WatchdogService.this.timeRecord.getDate().getTime())) {
                TimeRecord prevDayTimeRecord = WatchdogService.this.timeRecord;

                // Высчитываем сколько времени не было учтено до конца дня
                float prevDayIncreaseTime = this.convertToHours(this.getEndOfDay(prevDayTimeRecord.getDate()).getTime() -
                        WatchdogService.this.postTime);

                if (prevDayIncreaseTime > 0) {
                    prevDayTimeRecord.increaseWorkedTime(prevDayIncreaseTime);
                }

                WatchdogService.this.timeRecord = new TimeRecord(WatchdogService.this.timeRecord.getIssue());

                // Высчитываем сколько времени не было учтено после начала нового дня
                increaseTime = this.convertToHours(WatchdogService.this.currentTime() -
                        this.getStartOfDay(WatchdogService.this.timeRecord.getDate()).getTime());
            } else {
                // Высчитываем прошедшее время, если вдруг телефон заснул
                increaseTime = this.convertToHours(WatchdogService.this.currentTime() -
                        WatchdogService.this.postTime);
            }

            WatchdogService.this.timeRecord.increaseWorkedTime(increaseTime);
            EventBus.getDefault().post(new TimeRecordUpdatedEvent(WatchdogService.this.timeRecord));

            WatchdogService.this.postDelayed();
        }
    };

    private long currentTime() {
        return Calendar.getInstance().getTime().getTime();
    }

    private void postDelayed() {
        this.handler.removeCallbacks(this.runnable);
        this.handler.postDelayed(this.runnable, TIME_RECORD_DELAY_MS);

        this.postTime = this.currentTime();
    }

    public WatchdogService() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        this.handler.removeCallbacks(this.runnable);
        this.stopForeground(true);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        this.timeRecord = (TimeRecord) intent.getSerializableExtra(EXTRA_TIME_RECORD);
        this.postDelayed();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText("Working: " + this.timeRecord.getIssue().getReadableName())
                .setContentIntent(pendingIntent).build();

        this.startForeground(1000, notification);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException();
    }

    public static class TimeRecordUpdatedEvent {
        @NonNull
        private TimeRecord timeRecord;

        public TimeRecordUpdatedEvent(TimeRecord timeRecord) {
            this.timeRecord = timeRecord;
        }

        public TimeRecord getTimeRecord() {
            return timeRecord;
        }
    }
}
