package ru.killer666.issuetimewatchdog.helper;

import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import roboguice.inject.ContextSingleton;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordLogDao;
import ru.killer666.issuetimewatchdog.event.IssueStateChangedEvent;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.IssueState;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordLog;
import ru.killer666.issuetimewatchdog.model.TimeRecordLogType;
import ru.killer666.issuetimewatchdog.services.NotificationService;

@ContextSingleton
public class IssueHelper {

    @Inject
    private IssueDao issueDao;

    @Inject
    private TimeRecordDao timeRecordDao;

    @Inject
    private TimeRecordLogDao timeRecordLogDao;

    @Inject
    private TimeRecordHelper timeRecordHelper;

    @Inject
    private Context context;

    public void changeState(Issue issue, IssueState newState, TimeRecordLogType timeRecordLogType) {
        IssueState oldState = issue.getState();
        issue.setState(newState);

        boolean daoUpdateState = true;
        TimeRecord timeRecord = timeRecordDao.queryLastOfIssue(issue);

        if (IssueState.Working.equals(newState)) {
            // Check other issue for working
            Issue workingIssue = issueDao.queryWorkingState();
            if (workingIssue != null) {
                daoUpdateState = workingIssue.getId() != issue.getId();
                changeState(workingIssue, IssueState.Idle, TimeRecordLogType.TypeIdleForOtherTask);
            }

            context.startService(new Intent(context, NotificationService.class)
                    .putExtra(NotificationService.EXTRA_TIME_RECORD_ID, timeRecord.getId()));
        } else {
            context.stopService(new Intent(context, NotificationService.class));

            // Put worked time at time record
            TimeRecordLog timeRecordLog = timeRecordLogDao.queryLastStartForTimeRecord(timeRecord);

            float workedTime = (Calendar.getInstance().getTime().getTime() - timeRecordLog.getDate().getTime()) / 1000 / 60 / 60;
            timeRecord.increaseWorkedTime(workedTime);
            timeRecordDao.update(timeRecord);
        }

        if (daoUpdateState) {
            issueDao.update(issue);
        }

        timeRecordLogDao.createWithType(timeRecord, timeRecordLogType);
        EventBus.getDefault().post(new IssueStateChangedEvent(issue, oldState, timeRecordLogType));
    }

    public void remove(Issue issue, boolean uploadTimeRecords) {
        if (issue.getState() == IssueState.Working) {
            changeState(issue, IssueState.Idle, TimeRecordLogType.TypeIdle);
        }

        if (uploadTimeRecords) {
            issue.setRemoveAfterUpload(true);
            timeRecordHelper.startUploadSingle(issue);
        } else {
            issueDao.delete(issue);
        }
    }

}
