package ru.kupchinskiy.issuetimewatchdog.receiver;

import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import roboguice.receiver.RoboBroadcastReceiver;
import ru.kupchinskiy.issuetimewatchdog.dao.IssueDao;
import ru.kupchinskiy.issuetimewatchdog.helper.IssueHelper;
import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.model.IssueState;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecordLogType;

@Slf4j
public class IssueRestartWorkReceiver extends RoboBroadcastReceiver {

    @Inject
    private IssueHelper issueHelper;

    @Inject
    private IssueDao issueDao;

    @Override
    public void handleReceive(Context context, Intent intent) {
        log.debug("Alarm received");
        Issue workingIssue = issueDao.queryWorkingState();
        if (workingIssue != null) {
            issueHelper.changeState(workingIssue, IssueState.Idle, TimeRecordLogType.TypeIdleForDayEnd);
            issueHelper.changeState(workingIssue, IssueState.Working, TimeRecordLogType.TypeWorking);
            log.debug("Issue {} restarted", workingIssue);
        }
    }

}