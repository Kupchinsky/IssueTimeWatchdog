package ru.kupchinskiy.issuetimewatchdog.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;
import roboguice.service.RoboIntentService;
import ru.kupchinskiy.issuetimewatchdog.R;
import ru.kupchinskiy.issuetimewatchdog.converter.TrackorTypeConverter;
import ru.kupchinskiy.issuetimewatchdog.dao.IssueDao;
import ru.kupchinskiy.issuetimewatchdog.dao.TimeRecordDao;
import ru.kupchinskiy.issuetimewatchdog.dao.TimeRecordLogDao;
import ru.kupchinskiy.issuetimewatchdog.event.IssueTimeRecordsUploadCompleteEvent;
import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.model.IssueState;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecord;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecordLogType;
import ru.kupchinskiy.issuetimewatchdog.prefs.CreateTimeRecordsPrefs;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3TrackorCreateResponse;
import ru.kupchinskiy.issuetimewatchdog.ui.MainActivity;

import static ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3TrackorCreateRequest;
import static ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3TrackorCreateRequestParents;

@Slf4j
public class UploadTimeRecordsService extends RoboIntentService {

    public static final String ACTION_UPLOAD_ALL = "uploadAll";
    public static final String ACTION_UPLOAD_SINGLE = "uploadSingleIssue";

    public static final String EXTRA_ISSUE_ID = "issueId";

    @Inject
    private IssueDao issueDao;

    @Inject
    private TimeRecordDao timeRecordDao;

    @Inject
    private TimeRecordLogDao timeRecordLogDao;

    @Inject
    private ApiClient apiClient;

    @Inject
    private TrackorTypeConverter trackorTypeConverter;

    @Inject
    private PowerManager powerManager;

    @Inject
    private CreateTimeRecordsPrefs createTimeRecordsPrefs;

    @Inject
    private NotificationManager notificationManager;

    public UploadTimeRecordsService() {
        super(UploadTimeRecordsService.class.getSimpleName());
    }

    private void uploadSingleTimeRecord(TimeRecord timeRecord) throws IOException, IllegalStateException {
        Response<V3TrackorCreateResponse> response;
        V3TrackorCreateRequest trackorCreateRequest = new V3TrackorCreateRequest();
        trackorTypeConverter.fillTrackorCreateRequest(trackorCreateRequest, timeRecord);

        if (timeRecord.getRemoteTrackorId() == null) {
            trackorCreateRequest.getParents().add(V3TrackorCreateRequestParents.create()
                    .setTrackorType(Issue.getTrackorTypeName())
                    .addFilter("TRACKOR_KEY", timeRecord.getIssue().getTrackorKey()));

            response = apiClient.v3CreateTrackor(TimeRecord.getTrackorTypeName(), trackorCreateRequest).execute();

            if (HttpURLConnection.HTTP_CREATED == response.code()) {
                timeRecord.setWroteTime(timeRecord.getWorkedTime());
                timeRecord.setRemoteTrackorId(response.body().getTrackorId());
                timeRecord.setTrackorKey(response.body().getTrackorKey());
                timeRecordDao.update(timeRecord);
                timeRecordLogDao.createWithType(timeRecord, TimeRecordLogType.TypeRemoteCreate);
            } else {
                throw new IllegalStateException("Create failed");
            }
        } else {
            Map<String, String> filterParams = Maps.newHashMap();
            filterParams.put("TRACKOR_ID", String.valueOf(timeRecord.getRemoteTrackorId()));

            // Check time changed
            List<String> fields = trackorTypeConverter.formatTrackorTypeFields(TimeRecord.class);
            Response<List<JsonObject>> loadResponse = apiClient.v3Trackors(TimeRecord.getTrackorTypeName(),
                    null, fields, null, filterParams).execute();

            if (HttpURLConnection.HTTP_OK == loadResponse.code() && loadResponse.body().size() != 0) {
                // If remote time record exists and nothing changed at local -> no update
                if (timeRecord.getWorkedTime() == timeRecord.getWroteTime()) {
                    log.info("Nothing changed");
                    return;
                }

                TimeRecord remoteInstance = trackorTypeConverter.fromJson(TimeRecord.class, loadResponse.body().get(0));
                Float newWorkedTime = null;

                // If remote time record time changed -> add local time to existing
                if (remoteInstance.getWorkedTime() != timeRecord.getWroteTime()) {
                    float timeDelta = timeRecord.getWorkedTime() - timeRecord.getWroteTime();

                    newWorkedTime = remoteInstance.getWorkedTime() + timeDelta;
                    trackorCreateRequest.getFields().put("VQS_IT_SPENT_HOURS", String.valueOf(newWorkedTime));
                }

                response = apiClient.v3UpdateTrackor(TimeRecord.getTrackorTypeName(),
                        filterParams, trackorCreateRequest).execute();

                if (HttpURLConnection.HTTP_OK == response.code()) {
                    timeRecord.setWroteTime(newWorkedTime != null ? newWorkedTime : timeRecord.getWorkedTime());
                    timeRecordDao.update(timeRecord);
                    timeRecordLogDao.createWithType(timeRecord, TimeRecordLogType.TypeRemoteUpdate);
                } else {
                    throw new IllegalStateException("Update failed");
                }
            } else if (HttpURLConnection.HTTP_OK == loadResponse.code() && loadResponse.body().size() == 0) {
                log.info("Remote time record not found, reset remote trackor id and wrote time, then upload again: {}", timeRecord);

                timeRecord.setRemoteTrackorId(null);
                timeRecord.setWroteTime(0);
                timeRecordDao.update(timeRecord);

                uploadSingleTimeRecord(timeRecord);
            } else {
                throw new IllegalStateException("Remote time record read failed");
            }
        }
    }

    private Throwable uploadSingleIssue(Issue issue) {
        List<TimeRecord> timeRecordList = timeRecordDao.queryOfIssue(issue);

        // If issue is in working state: skip first time record
        if (IssueState.Working.equals(issue.getState())) {
            timeRecordList = Lists.newArrayList(timeRecordList);
            timeRecordList.remove(0);
        }

        Throwable error = null;

        for (TimeRecord timeRecord : timeRecordList) {
            log.info("Uploading {}", timeRecord);

            try {
                uploadSingleTimeRecord(timeRecord);
            } catch (Exception e) {
                log.error("Time record upload error", e);
                error = e;
                break;
            }
        }

        if (error == null && !issue.isRemoveAfterUpload()) {
            // Clear old time records
            List<TimeRecord> oldTimeRecords = timeRecordDao.queryOldOfIssue(issue);

            for (TimeRecord timeRecord : oldTimeRecords) {
                timeRecord.getTimeRecordLogForeignCollection().clear();
                timeRecordDao.delete(timeRecord);
                log.info("Deleted old time record: {}", timeRecord);
            }
        }

        EventBus.getDefault().post(new IssueTimeRecordsUploadCompleteEvent(issue, error));

        if (error == null && issue.isRemoveAfterUpload()) {
            issueDao.delete(issue);
            log.info("Deleted {}", issue);
        }

        return error;
    }

    private void showWarningNotify(Throwable error) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MainActivity.ACTION_SHOW_UPLOAD_ERROR);
        notificationIntent.putExtra(MainActivity.EXTRA_ERROR, error);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Time record upload failed! Click for details")
                .setContentIntent(pendingIntent)
                .setSound(soundUri).build();
        notificationManager.notify(0, notification);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CreateTimeRecords");
        wl.acquire();

        log.debug("Wakelock acquired");

        if (ACTION_UPLOAD_ALL.equals(intent.getAction())) {
            for (Issue issue : issueDao.queryForAll()) {
                Throwable error = uploadSingleIssue(issue);
                if (error != null) {
                    showWarningNotify(error);
                    createTimeRecordsPrefs.scheduleRetryCreateTimeRecordsOnce();
                    break;
                }
            }
        } else if (ACTION_UPLOAD_SINGLE.equals(intent.getAction())) {
            int issueId = intent.getIntExtra(EXTRA_ISSUE_ID, 0);
            Issue issue = issueDao.queryForId(issueId);
            uploadSingleIssue(issue);
        }

        wl.release();

        log.debug("Wakelock released");
    }

}
