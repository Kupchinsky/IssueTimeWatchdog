package ru.killer666.issuetimewatchdog.services;

import android.content.Intent;
import android.os.PowerManager;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Response;
import roboguice.service.RoboIntentService;
import roboguice.util.Strings;
import ru.killer666.issuetimewatchdog.converter.TrackorTypeConverter;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordLogDao;
import ru.killer666.issuetimewatchdog.event.IssueTimeRecordsUploadCompleteEvent;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.IssueState;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordLogType;

public class UploadTimeRecordsService extends RoboIntentService {

    private static Logger logger;

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

    public UploadTimeRecordsService() {
        super(UploadTimeRecordsService.class.getSimpleName());
    }

    private void uploadSingleTimeRecord(TimeRecord timeRecord) throws IOException, IllegalStateException {
        Response<ApiClient.V2TrackorCreateResponse> response;
        ApiClient.V2TrackorCreateRequest trackorCreateRequest = new ApiClient.V2TrackorCreateRequest();
        trackorTypeConverter.fillTrackorCreateRequest(trackorCreateRequest, timeRecord);

        if (timeRecord.getRemoteTrackorId() == null) {
            trackorCreateRequest.getParents().add(ApiClient.V2TrackorCreateRequestParents.create()
                    .setTrackorType(Issue.getTrackorTypeName())
                    .addFilter("TRACKOR_KEY", timeRecord.getIssue().getTrackorKey()));

            response = apiClient.v2CreateTrackor(TimeRecord.getTrackorTypeName(), trackorCreateRequest).execute();

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
            String fields = Strings.join(",", trackorTypeConverter.formatTrackorTypeFields(TimeRecord.class));
            Response<List<JsonObject>> loadResponse = apiClient.v2LoadTrackors(TimeRecord.getTrackorTypeName(),
                    fields, null, filterParams).execute();

            if (HttpURLConnection.HTTP_OK == loadResponse.code() && loadResponse.body().size() != 0) {
                // If remote time record exists and nothing changed at local -> no update
                if (timeRecord.getWorkedTime() == timeRecord.getWroteTime()) {
                    logger.info("Nothing changed");
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

                response = apiClient.v2UpdateTrackors(TimeRecord.getTrackorTypeName(),
                        filterParams, trackorCreateRequest).execute();

                if (HttpURLConnection.HTTP_OK == response.code()) {
                    timeRecord.setWroteTime(newWorkedTime != null ? newWorkedTime : timeRecord.getWorkedTime());
                    timeRecordDao.update(timeRecord);
                    timeRecordLogDao.createWithType(timeRecord, TimeRecordLogType.TypeRemoteUpdate);
                } else {
                    throw new IllegalStateException("Update failed");
                }
            } else if (HttpURLConnection.HTTP_OK == loadResponse.code() && loadResponse.body().size() == 0) {
                logger.info("Remote time record not found, reset remote trackor id and wrote time, then upload again: {}", timeRecord);

                timeRecord.setRemoteTrackorId(null);
                timeRecord.setWroteTime(0);
                timeRecordDao.update(timeRecord);

                uploadSingleTimeRecord(timeRecord);
            } else {
                throw new IllegalStateException("Remote time record read failed");
            }
        }
    }

    private void uploadSingleIssue(Issue issue) {
        List<TimeRecord> timeRecordList = timeRecordDao.queryOfIssue(issue);

        // If issue is in working state: skip first time record
        if (IssueState.Working.equals(issue.getState())) {
            timeRecordList = new ArrayList<>(timeRecordList);
            timeRecordList.remove(0);
        }

        Throwable error = null;

        for (TimeRecord timeRecord : timeRecordList) {
            logger.info("Uploading {}", timeRecord);

            try {
                uploadSingleTimeRecord(timeRecord);
            } catch (Exception e) {
                logger.error("Time record upload error", e);
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
                logger.info("Deleted old time record: {}", timeRecord);
            }
        }

        EventBus.getDefault().post(new IssueTimeRecordsUploadCompleteEvent(issue, error));

        if (error == null && issue.isRemoveAfterUpload()) {
            issueDao.delete(issue);
            logger.info("Deleted {}", issue);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CreateTimeRecords");
        wl.acquire();

        logger.debug("Wakelock acquired");

        if (ACTION_UPLOAD_ALL.equals(intent.getAction())) {
            for (Issue issue : issueDao.queryForAll()) {
                uploadSingleIssue(issue);
            }
        } else if (ACTION_UPLOAD_SINGLE.equals(intent.getAction())) {
            int issueId = intent.getIntExtra(EXTRA_ISSUE_ID, 0);
            Issue issue = issueDao.queryForId(issueId);
            uploadSingleIssue(issue);
        }

        // TODO: show notify (with vibration and ringtone) if time records not writed to 10:00AM in next day, так-же напоминать периодически, чтобы не попасть на деньги

        wl.release();

        logger.debug("Wakelock released");
    }

}
