package ru.killer666.issuetimewatchdog;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;

import com.orm.SugarRecord;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;

import java.util.Calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class WatchdogService extends Service {
    private static Logger logger;

    private static final int TIME_RECORD_DELAY = 60;
    private static final int TIME_RECORD_DELAY_MS = TIME_RECORD_DELAY * 1000;

    public static final String EXTRA_TIME_RECORD_ID = "timeRecordId";

    private TimeRecord timeRecord;

    private long postTime;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        private float convertToHours(long time) {
            return time / TIME_RECORD_DELAY_MS / (float) TIME_RECORD_DELAY;
        }

        @Override
        public void run() {
            logger.info("Running watcher");

            float increaseTime;
            boolean isToday = DateUtils.isToday(WatchdogService.this.timeRecord.getDate().getTime());

            logger.info("Time record: {}", WatchdogService.this.timeRecord);
            logger.info("isToday: {}", isToday);

            // Если день сменился, то создаём новый TimeRecord
            if (!isToday) {
                TimeRecord prevDayTimeRecord = WatchdogService.this.timeRecord;

                // Высчитываем сколько времени не было учтено до конца дня
                float prevDayIncreaseTime = this.convertToHours(MyDateUtils.getEndOfDay(prevDayTimeRecord.getDate()).getTime() -
                        WatchdogService.this.postTime);

                if (prevDayIncreaseTime > 0) {
                    logger.info("Increased time: {}", prevDayIncreaseTime);
                    prevDayTimeRecord.increaseWorkedTime(prevDayIncreaseTime);
                }

                WatchdogService.this.timeRecord = new TimeRecord(WatchdogService.this.timeRecord.getIssue());

                EventBus.getDefault().postSticky(new TimeRecordUsingEvent(WatchdogService.this.timeRecord.getId()));

                // Высчитываем сколько времени не было учтено после начала нового дня
                increaseTime = this.convertToHours(WatchdogService.this.currentTime() -
                        MyDateUtils.getStartOfDay(WatchdogService.this.timeRecord.getDate()).getTime());

                logger.info("Created new time record: {}", WatchdogService.this.timeRecord);
            } else {
                // Высчитываем прошедшее время, если вдруг телефон заснул
                increaseTime = this.convertToHours(WatchdogService.this.currentTime() -
                        WatchdogService.this.postTime);
            }

            logger.info("Increased time: {}", increaseTime);
            WatchdogService.this.timeRecord.increaseWorkedTime(increaseTime);
            EventBus.getDefault().post(new TimeRecordUpdatedEvent(WatchdogService.this.timeRecord.getId()));

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

        EventBus.getDefault().removeStickyEvent(TimeRecordUsingEvent.class);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info("Intent extras: {}", intent.getExtras().keySet());

        this.timeRecord = SugarRecord.findById(TimeRecord.class, intent.getLongExtra(EXTRA_TIME_RECORD_ID, -1));
        logger.info("Time record loaded: {}", this.timeRecord);

        this.postDelayed();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText("Working: " + this.timeRecord.getIssue().getReadableName())
                .setContentIntent(pendingIntent).build();

        this.startForeground(1000, notification);

        EventBus.getDefault().postSticky(new TimeRecordUsingEvent(this.timeRecord.getId()));

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @AllArgsConstructor
    @Getter
    public static class TimeRecordUpdatedEvent {
        private long timeRecordId;
    }

    @AllArgsConstructor
    @Getter
    public static class TimeRecordUsingEvent {
        private long timeRecordId;
    }
}
