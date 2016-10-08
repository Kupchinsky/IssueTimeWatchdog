package ru.killer666.issuetimewatchdog;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;

import java.util.Calendar;

import lombok.AllArgsConstructor;
import lombok.Getter;
import roboguice.service.RoboService;

public class WatchdogService extends RoboService {
    private static Logger logger;

    public static final String EXTRA_TIME_RECORD_ID = "timeRecordId";

    private static final int TIME_RECORD_DELAY = 60;
    private static final int TIME_RECORD_DELAY_MS = TIME_RECORD_DELAY * 1000;

    @Inject
    private TimeRecordDao timeRecordDao;
    @Inject
    private IssueDao issueDao;
    @Inject
    private TimeRecordStartStopDao timeRecordStartStopDao;

    private TimeRecord timeRecord;

    private long postTime;
    private Handler handler = new Handler();
    private Runnable runnable = () -> {
        logger.info("Running watcher");

        float increaseTime;
        boolean isToday = DateUtils.isToday(this.timeRecord.getDate().getTime());

        logger.info("Time record: {}", this.timeRecord);
        logger.info("isToday: {}", isToday);

        // Если день сменился, то создаём новый TimeRecord
        if (!isToday) {
            TimeRecord prevDayTimeRecord = this.timeRecord;

            // Высчитываем сколько времени не было учтено до конца дня
            float prevDayIncreaseTime = this.convertToHours(MyDateUtils.getEndOfDay(prevDayTimeRecord.getDate()).getTime() -
                    this.postTime);

            if (prevDayIncreaseTime > 0) {
                logger.info("Increased time: {}", prevDayIncreaseTime);
                prevDayTimeRecord.increaseWorkedTime(prevDayIncreaseTime);
                this.timeRecordDao.update(prevDayTimeRecord);
                EventBus.getDefault().post(new OnTimeRecordUpdatedEvent(prevDayTimeRecord));
            }

            this.timeRecord = new TimeRecord(this.timeRecord.getIssue());
            this.timeRecordDao.create(this.timeRecord);

            EventBus.getDefault().postSticky(new OnTimeRecordUsingEvent(this.timeRecord));

            // Высчитываем сколько времени не было учтено после начала нового дня
            increaseTime = this.convertToHours(this.currentTime() -
                    MyDateUtils.getStartOfDay(this.timeRecord.getDate()).getTime());

            logger.info("Created new time record: {}", this.timeRecord);

            this.timeRecordStartStopDao.createWithType(prevDayTimeRecord, TimeRecordStartStopType.TypeStopForDayEnd);
            this.timeRecordStartStopDao.createWithType(this.timeRecord, TimeRecordStartStopType.TypeStart);
        } else {
            // Высчитываем прошедшее время, если вдруг телефон заснул
            increaseTime = this.convertToHours(this.currentTime() -
                    this.postTime);
        }

        logger.info("Increased time: {}", increaseTime);
        this.timeRecord.increaseWorkedTime(increaseTime);
        this.timeRecordDao.update(this.timeRecord);
        EventBus.getDefault().post(new OnTimeRecordUpdatedEvent(this.timeRecord));

        this.postDelayed();
    };

    private float convertToHours(long time) {
        return time / TIME_RECORD_DELAY_MS / (float) TIME_RECORD_DELAY;
    }

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
        logger.info("Intent extras: {}", intent.getExtras().keySet());

        this.timeRecord = this.timeRecordDao.queryForId(intent.getIntExtra(EXTRA_TIME_RECORD_ID, -1));
        this.issueDao.refresh(this.timeRecord.getIssue());

        logger.info("Time record loaded: {}", this.timeRecord);

        this.timeRecordStartStopDao.createWithType(this.timeRecord, TimeRecordStartStopType.TypeStart);
        this.postDelayed();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MainActivity.ACTION_SHOW_TIMERECORD);
        notificationIntent.putExtra(MainActivity.EXTRA_ISSUE_ID, this.timeRecord.getIssue().getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText("Working: " + this.timeRecord.getIssue().getReadableName())
                .setContentIntent(pendingIntent).build();

        this.startForeground(1000, notification);

        EventBus.getDefault().postSticky(new OnTimeRecordUsingEvent(this.timeRecord));
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException();
    }

    @AllArgsConstructor
    @Getter
    public static class OnTimeRecordUsingEvent {
        private TimeRecord timeRecord;
    }

    @AllArgsConstructor
    @Getter
    public static class OnTimeRecordUpdatedEvent {
        private TimeRecord timeRecord;
    }
}
