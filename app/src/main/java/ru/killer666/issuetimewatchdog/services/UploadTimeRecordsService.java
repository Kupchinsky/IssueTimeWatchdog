package ru.killer666.issuetimewatchdog.services;

import android.content.Intent;
import android.os.PowerManager;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import retrofit2.Response;
import roboguice.service.RoboIntentService;
import roboguice.util.Strings;
import ru.killer666.issuetimewatchdog.converter.TrackorTypeConverter;
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
    private TrackorTypeConverter trackorTypeConverter;

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
                Response<ApiClient.V2TrackorCreateResponse> response;
                ApiClient.V2TrackorCreateRequest trackorCreateRequest = new ApiClient.V2TrackorCreateRequest();
                trackorTypeConverter.fillTrackorCreateRequest(trackorCreateRequest, timeRecord);
                short retriesLeft = 5;

                while (--retriesLeft > 0) {
                    logger.info("Uploading {} (retry {} of 5)", timeRecord, retriesLeft);

                    try {
                        if (timeRecord.getRemoteTrackorId() == null) {
                            trackorCreateRequest.getFields().put(Issue.getTrackorTypeName() + ".TRACKOR_KEY", issue.getTrackorKey());

                            response = apiClient.v2CreateTrackor(TimeRecord.getTrackorTypeName(), trackorCreateRequest).execute();

                            if (HttpURLConnection.HTTP_OK == response.code()) {
                                timeRecord.setWroteTime(timeRecord.getWorkedTime());
                                timeRecord.setRemoteTrackorId(response.body().getTrackorId());
                                timeRecord.setTrackorKey(response.body().getTrackorKey());
                                timeRecordDao.update(timeRecord);
                            } else {
                                throw new RuntimeException("Create failed");
                            }
                        } else {
                            Map<String, String> filterParams = Maps.newHashMap();
                            filterParams.put("TRACKOR_ID", String.valueOf(timeRecord.getRemoteTrackorId()));

                            // Check time changed
                            String fields = Strings.join(",", trackorTypeConverter.formatTrackorTypeFields(TimeRecord.class));
                            Response<List<JsonObject>> loadResponse = apiClient.v2LoadTrackors(TimeRecord.getTrackorTypeName(), fields, null,
                                    Maps.newHashMap(), timeRecord.getRemoteTrackorId()).execute();

                            if (HttpURLConnection.HTTP_OK == loadResponse.code()) {
                                TimeRecord remoteInstance = trackorTypeConverter.fromJson(TimeRecord.class, loadResponse.body().get(0));
                                Double newWorkedTime = null;

                                if (remoteInstance.getWorkedTime() != timeRecord.getWroteTime()) {
                                    newWorkedTime = remoteInstance.getWorkedTime() + (timeRecord.getWorkedTime() - timeRecord.getWroteTime());
                                    trackorCreateRequest.getFields().put("VQS_IT_SPENT_HOURS", String.valueOf(newWorkedTime));
                                }

                                response = apiClient.v2UpdateTrackors(TimeRecord.getTrackorTypeName(),
                                        filterParams, trackorCreateRequest).execute();

                                if (HttpURLConnection.HTTP_OK == response.code() && timeRecord.getRemoteTrackorId().equals(response.body().getTrackorId())) {
                                    timeRecord.setWroteTime(newWorkedTime != null ? newWorkedTime : timeRecord.getWorkedTime());
                                    timeRecordDao.update(timeRecord);
                                } else {
                                    throw new RuntimeException("Update failed");
                                }
                            } else {
                                throw new RuntimeException("Remote trackor read failed");
                            }
                        }

                        break;
                    } catch (Exception e) {
                        logger.error("Time record upload error: {}", e);
                    }
                }
            }

            boolean isPostEvent = !timeRecordList.isEmpty();

            if (issue.isRemoveAfterUpload()) {
                issueDao.delete(issue);
                logger.info("Deleted {}", issue);
            } else {
                // Clear old time records
                List<TimeRecord> oldTimeRecords = timeRecordDao.queryOldOfIssue(issue);

                for (TimeRecord timeRecord : oldTimeRecords) {
                    timeRecord.getTimeRecordLogForeignCollection().clear();
                    timeRecordDao.delete(timeRecord);
                    logger.info("Deleted old time record: {}", timeRecord);
                }

                isPostEvent = isPostEvent || !oldTimeRecords.isEmpty();
            }

            if (isPostEvent) {
                EventBus.getDefault().post(new IssueTimeRecordsUploadCompleteEvent(issue, null));
            }
        }

        // TODO: show notify (with vibration and ringtone) if time records not writed to 10:00AM in next day, так-же напоминать периодически, чтобы не попасть на деньги

        wl.release();

        logger.debug("Wakelock released");
    }

}
