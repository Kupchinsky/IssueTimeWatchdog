package ru.killer666.issuetimewatchdog.helper;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.format.DateUtils;

import com.google.inject.Inject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import roboguice.inject.ContextSingleton;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDaoImpl;
import ru.killer666.issuetimewatchdog.dao.TimeRecordStartStopDao;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.IssueState;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStopType;

@ContextSingleton
public class TimeRecordHelper {

    @Inject
    private TimeRecordDao timeRecordDao;

    @Inject
    private TimeRecordStartStopDao timeRecordStartStopDao;

    @Inject
    private IssueHelper issueHelper;

    @Inject
    private Context context;

    private final DateFormat dateFormatter = DateFormat.getDateInstance();
    private final NumberFormat numberFormat = new DecimalFormat();

    {
        numberFormat.setMaximumFractionDigits(2);
    }

    public void showLastForIssue(Issue issue) {
        String message = "";

        for (TimeRecord timeRecord : timeRecordDao.queryLastOfIssueList(issue)) {
            message += Html.fromHtml(formatHistory(timeRecord, true)) + "\n";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Time records of " + issue.getReadableName())
                .setMessage(message + "\n(Time records of last " + TimeRecordDaoImpl.SHOW_LIMIT + " days)")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        if (issue.getState() == IssueState.Working) {
            builder.setNeutralButton("Stop working", ((dialog, which) -> {
                dialog.dismiss();

                issueHelper.changeState(issue, IssueState.Idle, TimeRecordStartStopType.TypeStop);
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
                dateFormatter.format(timeRecord.getDate())) + ": " +
                numberFormat.format(timeRecord.getWorkedTime());

        boolean isFullWrite = timeRecord.getWorkedTime() == timeRecord.getWroteTime();

        if (isLong) {
            message += " h. (wrote" + (isFullWrite ? "[full]" : "") + " " +
                    numberFormat.format(timeRecord.getWroteTime()) + " h.)";
        } else {
            message += "/" + numberFormat.format(timeRecord.getWroteTime()) + " (w" +
                    (isFullWrite ? "[f]" : "") + ") hrs.";
        }

        return message;
    }

    public String formatState(IssueState state) {
        boolean escape = state == IssueState.Working;
        return (escape ? "<b>" : "") + state.getValue() + (escape ? "</b>" : "");
    }

}
