package ru.killer666.issuetimewatchdog.helper;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import roboguice.inject.ContextSingleton;
import roboguice.util.Strings;
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

    private List<TimeUnit> historyFormatTimeUnits = Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS);

    public void showForTimeRecord(TimeRecord timeRecord) {
        List<String> messageList = Lists.newArrayList();

        TimeRecordStartStop timeRecordStartStopNext = null;
        for (TimeRecordStartStop timeRecordStartStop : timeRecordStartStopDao.queryOfTimeRecordList(timeRecord)) {
            messageList.add(formatHistory(timeRecordStartStop, timeRecordStartStopNext));
            timeRecordStartStopNext = timeRecordStartStop;
        }

        Collections.reverse(messageList);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Start/stop log of " + timeRecord.getReadableName(configFieldFormatter))
                .setMessage(Strings.join("\n", messageList) + "\n(Log of last " + TimeRecordStartStopDao.SHOW_LIMIT + " days)")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private String formatHistory(TimeRecordStartStop timeRecordStartStop, TimeRecordStartStop timeRecordStartStopNext) {
        String message = (DateUtils.isToday(timeRecordStartStop.getDate().getTime()) ? "Today" :
                configFieldFormatter.getDateFormatter().format(timeRecordStartStop.getDate())) + " " +
                configFieldFormatter.getTimeFormatter().format(timeRecordStartStop.getDate()) + ": " +
                timeRecordStartStop.getType().getValue();

        if (timeRecordStartStopNext != null &&
                TimeRecordStartStopType.isIdle(timeRecordStartStop.getType()) &&
                TimeRecordStartStopType.TypeWorking.equals(timeRecordStartStopNext.getType())) {
            message += " (+" + MyDateUtils.getTimeDifference(timeRecordStartStopNext.getDate(),
                    timeRecordStartStop.getDate(), historyFormatTimeUnits) + ")";
        } else if (TimeRecordStartStopType.TypeWorking.equals(timeRecordStartStop.getType())) {
            message += "\n";
        }

        return message;
    }

}
