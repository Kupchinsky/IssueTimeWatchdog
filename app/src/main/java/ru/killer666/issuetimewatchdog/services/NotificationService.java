package ru.killer666.issuetimewatchdog.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.inject.Inject;

import org.slf4j.Logger;

import roboguice.service.RoboService;
import ru.killer666.issuetimewatchdog.R;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.ui.MainActivity;

public class NotificationService extends RoboService {

    private static Logger logger;

    public static final String EXTRA_TIME_RECORD_ID = "timeRecordId";

    @Inject
    private TimeRecordDao timeRecordDao;

    public NotificationService() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info("Intent extras: {}", intent.getExtras().keySet());

        TimeRecord timeRecord = timeRecordDao.queryForId(intent.getIntExtra(EXTRA_TIME_RECORD_ID, -1));
        logger.info("Time record loaded: {}", timeRecord);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MainActivity.ACTION_SHOW_TIMERECORD);
        notificationIntent.putExtra(MainActivity.EXTRA_ISSUE_ID, timeRecord.getIssue().getId());

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Working: " + timeRecord.getIssue().getReadableName())
                .setContentIntent(pendingIntent).build();

        startForeground(1000, notification);
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException();
    }

}
