package ru.killer666.issuetimewatchdog.helper;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.format.DateUtils;

import com.google.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import roboguice.inject.ContextSingleton;
import ru.killer666.issuetimewatchdog.dao.TimeRecordStartStopDao;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStop;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStopType;

@ContextSingleton
public class TimeRecordStartStopHelper {

    @Inject
    private TimeRecordStartStopDao timeRecordStartStopDao;

    @Inject
    private ConfigFieldFormatter configFieldFormatter;

    @Inject
    private Context context;

    private List<TimeUnit> historyFormatTimeUnits = Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES);

    public void showForTimeRecord(TimeRecord timeRecord) {
        String message = "";

        TimeRecordStartStop timeRecordStartStopPrev = null;
        for (TimeRecordStartStop timeRecordStartStop : timeRecordStartStopDao.queryOfTimeRecordList(timeRecord)) {
            message += Html.fromHtml(formatHistory(timeRecordStartStop, timeRecordStartStopPrev)) + "\n";
            timeRecordStartStopPrev = timeRecordStartStop;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Start/stop log of " + timeRecord.getReadableName(configFieldFormatter))
                .setMessage(message + "\n(Log of last " + TimeRecordStartStopDao.SHOW_LIMIT + " days)")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private String formatHistory(TimeRecordStartStop timeRecordStartStop, TimeRecordStartStop timeRecordStartStopPrev) {
        String message = (DateUtils.isToday(timeRecordStartStop.getDate().getTime()) ? "<b>Today</b>" :
                configFieldFormatter.getDateFormatter().format(timeRecordStartStop.getDate())) + " " +
                configFieldFormatter.getTimeFormatter().format(timeRecordStartStop.getDate()) + ": " +
                timeRecordStartStop.getType().getValue();

        if (timeRecordStartStopPrev != null && TimeRecordStartStopType.isIdle(timeRecordStartStop.getType()) &&
                TimeRecordStartStopType.TypeWorking.equals(timeRecordStartStopPrev.getType())) {
            message += "(+" + MyDateUtils.getTimeDifference(timeRecordStartStopPrev.getDate(),
                    timeRecordStartStop.getDate(), historyFormatTimeUnits);
        }

        return message;
    }

}
