package ru.killer666.issuetimewatchdog.services;

import android.content.Intent;
import android.os.PowerManager;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;

import java.util.List;

import roboguice.service.RoboIntentService;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.event.IssueTimeRecordsUploadCompleteEvent;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.TimeRecord;

public class UploadTimeRecordsService extends RoboIntentService {
    private static Logger logger;

    public static final String ACTION_UPLOAD_ALL = "uploadAll";
    public static final String ACTION_UPLOAD_SINGLE = "uploadSingle";

    public static final String EXTRA_ISSUE_ID = "issueId";

    @Inject
    private IssueDao issueDao;
    @Inject
    private TimeRecordDao timeRecordDao;
    @Inject
    private ApiClient apiClient;
    @Inject
    private PowerManager powerManager;

    public UploadTimeRecordsService() {
        super(UploadTimeRecordsService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CreateTimeRecords");
        wl.acquire();

        logger.debug("Wakelock acquired");

        for (Issue issue : issueDao.queryForAll()) {
            List<TimeRecord> timeRecordList = timeRecordDao.queryNotUploadedOfIssue(issue);

            for (TimeRecord timeRecord : timeRecordList) {
                logger.info("Uploading {}", timeRecord);

                // TODO: create trackor
            }

            boolean isPostEvent = !timeRecordList.isEmpty();

            if (issue.isRemoveAfterUpload()) {
                issueDao.deleteWithAllChilds(issue);
                logger.info("Deleted {}", issue);
            } else {
                // Clear old time records
                List<TimeRecord> oldTimeRecords = timeRecordDao.queryOldOfIssue(issue);

                for (TimeRecord timeRecord : oldTimeRecords) {
                    timeRecord.getTimeRecordLogForeignCollection().clear();
                    timeRecordDao.delete(timeRecord);
                }

                isPostEvent = isPostEvent || !oldTimeRecords.isEmpty();
            }

            if (isPostEvent) {
                EventBus.getDefault().post(new IssueTimeRecordsUploadCompleteEvent(issue, null));
            }
        }

        // TODO: upload "unuploaded" (Не забывать проверять, если кто-нить часы перекрутил руками), т.е. сначала Read, а потом CreateOrUpdate
        // TODO: remove local issues+timerecords with autoremove field == true
        // TODO: remove timerecords older than {#TimeRecordDao.SHOW_LIMIT}
        // TODO: show notify (with vibration and ringtone) if time records not writed to 10:00AM in next day, так-же напоминать периодически, чтобы не попасть на деньги
        // TODO: удалять Issue здесь, но отправлять инстансы в событие

        wl.release();

        logger.debug("Wakelock released");
    }

}
