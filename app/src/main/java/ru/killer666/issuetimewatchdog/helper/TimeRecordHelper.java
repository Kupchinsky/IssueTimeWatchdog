package ru.killer666.issuetimewatchdog.helper;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.format.DateUtils;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import roboguice.inject.ContextSingleton;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordLogDao;
import ru.killer666.issuetimewatchdog.event.IssueTimeRecordsUploadCompleteEvent;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.IssueState;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordLogType;
import ru.killer666.issuetimewatchdog.services.UploadTimeRecordsService;

@ContextSingleton
public class TimeRecordHelper {

    @Inject
    private TimeRecordDao timeRecordDao;

    @Inject
    private TimeRecordLogDao timeRecordLogDao;

    @Inject
    private IssueHelper issueHelper;

    @Inject
    private DialogHelper dialogHelper;

    @Inject
    private ConfigFieldFormatter configFieldFormatter;

    @Inject
    private Context context;

    public void increaseTime(TimeRecord timeRecord, double amount) {
        timeRecord.increaseWorkedTime(amount);
        timeRecordDao.update(timeRecord);
        timeRecordLogDao.createWithType(timeRecord, TimeRecordLogType.TypeHandIncreaseTime);
    }

    public boolean decreaseTime(TimeRecord timeRecord, double amount) {
        if (timeRecord.getWorkedTime() < amount) {
            dialogHelper.warning("Input amount of time is greater then worked time");
            return false;
        }

        timeRecord.decreaseWorkedTime(amount);
        timeRecordDao.update(timeRecord);
        timeRecordLogDao.createWithType(timeRecord, TimeRecordLogType.TypeHandDecreaseTime);
        return true;
    }

    public void showLastForIssue(Issue issue) {
        String message = "";

        for (TimeRecord timeRecord : timeRecordDao.queryLastOfIssueList(issue)) {
            message += Html.fromHtml(formatHistory(timeRecord, true)) + "\n";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Time records of " + issue.getReadableName())
                .setMessage(message + "\n(Time records of last " + TimeRecordDao.SHOW_LIMIT + " days)")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        if (issue.getState() == IssueState.Working) {
            builder.setNeutralButton("Stop working", ((dialog, which) -> {
                dialog.dismiss();

                issueHelper.changeState(issue, IssueState.Idle, TimeRecordLogType.TypeIdle);
            }));
        }

        builder.show();
    }

    public TimeRecord getOrCreateLastTimeRecordForIssue(Issue issue) {
        TimeRecord timeRecord = timeRecordDao.queryLastOfIssue(issue);

        if (timeRecord == null || !DateUtils.isToday(timeRecord.getDate().getTime())) {
            timeRecord = new TimeRecord(issue);
            timeRecordDao.create(timeRecord);
        }

        return timeRecord;
    }

    public String formatHistory(TimeRecord timeRecord, boolean isLong) {
        String message = (DateUtils.isToday(timeRecord.getDate().getTime()) ? "<b>Today</b>" :
                configFieldFormatter.getDateFormatter().format(timeRecord.getDate())) + ": " +
                configFieldFormatter.getNumberFormatter().format(timeRecord.getWorkedTime());

        boolean isFullWrite = timeRecord.getWorkedTime() == timeRecord.getWroteTime();

        if (isLong) {
            message += " h. (wrote" + (isFullWrite ? "[full]" : "") + " " +
                    configFieldFormatter.getNumberFormatter().format(timeRecord.getWroteTime()) + " h.)";
        } else {
            message += "/" + configFieldFormatter.getNumberFormatter().format(timeRecord.getWroteTime()) + " (w" +
                    (isFullWrite ? "[f]" : "") + ") hrs.";
        }

        return message;
    }

    public String formatState(IssueState state) {
        boolean escape = state == IssueState.Working;
        return (escape ? "<b>" : "") + state.getValue() + (escape ? "</b>" : "");
    }

    public void startUploadAll() {
        context.startService(new Intent(context, UploadTimeRecordsService.class)
                .setAction(UploadTimeRecordsService.ACTION_UPLOAD_ALL));
    }

    public void startUploadSingle(Issue issue) {
        EventBus.getDefault().register(this);

        dialogHelper.showProgressDialog();
        context.startService(new Intent(context, UploadTimeRecordsService.class)
                .setAction(UploadTimeRecordsService.ACTION_UPLOAD_SINGLE)
                .putExtra(UploadTimeRecordsService.EXTRA_ISSUE_ID, issue.getId()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIssueTimeRecordsUploadComplete(IssueTimeRecordsUploadCompleteEvent event) {
        EventBus.getDefault().unregister(this);

        dialogHelper.dismissProgressDialog();

        if (event.isErrored()) {
            dialogHelper.error("Upload error: " + event.getThrowable().getMessage());
        }
    }

}
