package ru.killer666.issuetimewatchdog.helper;

import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import roboguice.inject.ContextSingleton;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordStartStopDao;
import ru.killer666.issuetimewatchdog.event.IssueStateChangedEvent;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.IssueState;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStop;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStopType;
import ru.killer666.issuetimewatchdog.services.NotificationService;
import ru.killer666.issuetimewatchdog.services.UploadTimeRecordsService;

@ContextSingleton
public class IssueHelper {

    @Inject
    private IssueDao issueDao;

    @Inject
    private TimeRecordDao timeRecordDao;

    @Inject
    private TimeRecordStartStopDao timeRecordStartStopDao;

    @Inject
    private Context context;

    public void changeState(Issue issue, IssueState newState, TimeRecordStartStopType timeRecordStartStopType) {
        IssueState oldState = issue.getState();
        issue.setState(newState);

        TimeRecord timeRecord = timeRecordDao.queryLastOfIssue(issue);

        if (IssueState.Working.equals(newState)) {
            // Check other issue for working
            Issue workingIssue = issueDao.queryWorkingState();
            if (workingIssue != null) {
                changeState(workingIssue, IssueState.Idle, TimeRecordStartStopType.TypeStopForOtherTask);
            }

            context.startService(new Intent(context, NotificationService.class)
                    .putExtra(NotificationService.EXTRA_TIME_RECORD_ID, timeRecord.getId()));
        } else {
            context.stopService(new Intent(context, NotificationService.class));

            // Put worked time at time record
            TimeRecordStartStop timeRecordStartStop = timeRecordStartStopDao.queryLastStartForTimeRecord(timeRecord);

            float workedTime = (Calendar.getInstance().getTime().getTime() - timeRecordStartStop.getDate().getTime()) / 1000 / 60 / 60;
            timeRecord.increaseWorkedTime(workedTime);
        }

        timeRecordStartStopDao.createWithType(timeRecord, timeRecordStartStopType);
        EventBus.getDefault().post(new IssueStateChangedEvent(issue, oldState, timeRecordStartStopType));
    }

    // TODO: use or remove
    public void remove(Issue issue, boolean uploadTimeRecords) {
        if (issue.getState() == IssueState.Working) {
            changeState(issue, IssueState.Idle, TimeRecordStartStopType.TypeStop);
        }

        if (uploadTimeRecords) {
            issue.setRemoveAfterUpload(true);
            context.startActivity(new Intent(context, UploadTimeRecordsService.class)
                    .setAction(UploadTimeRecordsService.ACTION_UPLOAD_SINGLE)
                    .putExtra(UploadTimeRecordsService.EXTRA_ISSUE_ID, issue.getId()));
        } else {
            issueDao.deleteWithAllChilds(issue);
        }
    }

}
