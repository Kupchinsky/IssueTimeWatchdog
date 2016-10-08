package ru.killer666.issuetimewatchdog;

import android.content.Intent;
import android.os.PowerManager;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import roboguice.service.RoboIntentService;

public class CreateTimeRecordsService extends RoboIntentService {
    private static Logger logger;

    static final String ACTION_UPDATE_ALL = "updateAll";
    static final String ACTION_UPDATE_SINGLE = "updateSingle";

    static final String EXTRA_ISSUE_ID = "issueId";

    @Inject
    private IssueDao issueDao;
    @Inject
    private TimeRecordDao timeRecordDao;
    @Inject
    private TrackorApiService trackorApiService;
    @Inject
    private PowerManager powerManager;

    public CreateTimeRecordsService() {
        super(CreateTimeRecordsService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager.WakeLock wl = this.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CreateTimeRecords");
        wl.acquire();

        logger.debug("Wakelock acquired");

        for (Issue issue : this.issueDao.queryForAll()) {
            List<TimeRecord> timeRecordList = this.timeRecordDao.queryNotUploadedOfIssue(issue);

            for (TimeRecord timeRecord : timeRecordList) {
                logger.info("Uploading {}", timeRecord);
            }

            boolean isPostEvent = !timeRecordList.isEmpty();

            if (issue.isAutoRemove()) {
                this.issueDao.deleteWithAllChilds(issue);
                logger.info("Deleted {}", issue);
            } else {
                // Clear old time records
                List<TimeRecord> oldTimeRecords = this.timeRecordDao.queryOldOfIssue(issue);

                for (TimeRecord timeRecord : oldTimeRecords) {
                    timeRecord.getTimeRecordStartStopForeignCollection().clear();
                    this.timeRecordDao.delete(timeRecord);
                }

                isPostEvent = isPostEvent || !oldTimeRecords.isEmpty();
            }

            if (isPostEvent) {
                EventBus.getDefault().post(new OnTimeRecordsUpdatedEvent(issue));
            }
        }

        // TODO: upload "unuploaded" (Не забывать проверять, если кто-нить часы перекрутил руками), т.е. сначала Read, а потом CreateOrUpdate
        // TODO: remove local issues+timerecords with autoremove field == true
        // TODO: remove timerecords older than {#TimeRecordDao.SHOW_LIMIT}
        // TODO: show notify (with vibration and ringtone) if time records not writed to 10:00AM in next day, так-же напоминать периодически, чтобы не попасть на еду
        // TODO: удалять Issue здесь, но отправлять инстансы в событие

        wl.release();

        logger.debug("Wakelock released");
    }

    @AllArgsConstructor
    @Getter
    public static class OnTimeRecordsUpdatedEvent {
        private Issue issue;
    }
}
