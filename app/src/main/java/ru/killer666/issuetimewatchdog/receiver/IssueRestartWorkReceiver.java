package ru.killer666.issuetimewatchdog.receiver;

import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.slf4j.Logger;

import roboguice.receiver.RoboBroadcastReceiver;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.helper.IssueHelper;
import ru.killer666.issuetimewatchdog.helper.ServiceHelper;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.IssueState;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStopType;
import ru.killer666.issuetimewatchdog.services.NotificationService;

public class IssueRestartWorkReceiver extends RoboBroadcastReceiver {

    private static Logger logger;

    @Inject
    private IssueHelper issueHelper;

    @Inject
    private IssueDao issueDao;

    @Inject
    private ServiceHelper serviceHelper;

    @Override
    public void handleReceive(Context context, Intent intent) {
        logger.debug("Alarm received");
        Issue workingIssue = issueDao.queryWorkingState();
        if (workingIssue != null && serviceHelper.isRunning(NotificationService.class)) {
            issueHelper.changeState(workingIssue, IssueState.Idle, TimeRecordStartStopType.TypeIdleForDayEnd);
            issueHelper.changeState(workingIssue, IssueState.Working, TimeRecordStartStopType.TypeWorking);
            logger.debug("Issue {} restarted", workingIssue);
        }
    }

}