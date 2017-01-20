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
import ru.killer666.issuetimewatchdog.dao.TimeRecordLogDao;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordLog;
import ru.killer666.issuetimewatchdog.model.TimeRecordLogType;

@ContextSingleton
public class TimeRecordLogHelper {

    @Inject
    private TimeRecordLogDao timeRecordLogDao;

    @Inject
    private RemoteUserSettings remoteUserSettings;

    @Inject
    private Context context;

    private List<TimeUnit> historyFormatTimeUnits = Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS);

    public void showForTimeRecord(TimeRecord timeRecord) {
        List<String> messageList = Lists.newArrayList();

        TimeRecordLog timeRecordLogNext = null;
        for (TimeRecordLog timeRecordLog : timeRecordLogDao.queryOfTimeRecordList(timeRecord)) {
            messageList.add(formatHistory(timeRecordLog, timeRecordLogNext));
            timeRecordLogNext = timeRecordLog;
        }

        Collections.reverse(messageList);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Start/stop log of " + timeRecord.getReadableName(remoteUserSettings))
                .setMessage(Strings.join("\n", messageList) + "\n(Log of last " + TimeRecordLogDao.SHOW_LIMIT + " days)")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private String formatHistory(TimeRecordLog timeRecordLog, TimeRecordLog timeRecordLogNext) {
        String message = (DateUtils.isToday(timeRecordLog.getDate().getTime()) ? "Today" :
                remoteUserSettings.getDateFormatter().format(timeRecordLog.getDate())) + " " +
                remoteUserSettings.getTimeFormatter().format(timeRecordLog.getDate()) + ": " +
                timeRecordLog.getType().getValue();

        if (timeRecordLogNext != null &&
                TimeRecordLogType.isIdle(timeRecordLog.getType()) &&
                TimeRecordLogType.TypeWorking.equals(timeRecordLogNext.getType())) {
            message += " (+" + MyDateUtils.getTimeDifference(timeRecordLogNext.getDate(),
                    timeRecordLog.getDate(), historyFormatTimeUnits) + ")";
        } else if (TimeRecordLogType.TypeWorking.equals(timeRecordLog.getType())) {
            message += "\n";
        }

        return message;
    }

}
