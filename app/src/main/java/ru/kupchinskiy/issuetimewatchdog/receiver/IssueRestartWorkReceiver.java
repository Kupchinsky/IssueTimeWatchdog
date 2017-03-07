package ru.kupchinskiy.issuetimewatchdog.receiver;

import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.slf4j.Logger;

import roboguice.receiver.RoboBroadcastReceiver;
import ru.kupchinskiy.issuetimewatchdog.dao.IssueDao;
import ru.kupchinskiy.issuetimewatchdog.helper.IssueHelper;
import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.model.IssueState;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecordLogType;

public class IssueRestartWorkReceiver extends RoboBroadcastReceiver {

    private static Logger logger;

    @Inject
    private IssueHelper issueHelper;

    @Inject
    private IssueDao issueDao;

    @Override
    public void handleReceive(Context context, Intent intent) {
        logger.debug("Alarm received");
        Issue workingIssue = issueDao.queryWorkingState();
        if (workingIssue != null) {
            issueHelper.changeState(workingIssue, IssueState.Idle, TimeRecordLogType.TypeIdleForDayEnd);
            issueHelper.changeState(workingIssue, IssueState.Working, TimeRecordLogType.TypeWorking);
            logger.debug("Issue {} restarted", workingIssue);
        }
    }

}