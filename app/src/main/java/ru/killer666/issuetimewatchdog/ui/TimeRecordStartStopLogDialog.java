package ru.killer666.issuetimewatchdog.ui;


import android.app.DatePickerDialog;

import java.util.Calendar;

import ru.killer666.issuetimewatchdog.model.Issue;

public class TimeRecordStartStopLogDialog {
    private final Issue issue;

    private DatePickerDialog datePickerDialog;
    private boolean isDateSet;

    private final Calendar calendar = Calendar.getInstance();
    private final DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        isDateSet = true;
    };

    TimeRecordStartStopLogDialog(Issue issue) {
        this.issue = issue;
    }
/*
    private void showTimeRecordStartStopLog(TimeRecord timeRecord) {
        String message = "";

        for (TimeRecordStartStop timeRecordStartStop :
                MainActivityIssueEntryAdapter.timeRecordStartStopDao.queryOfTimeRecordList(timeRecord)) {
            String time = MainActivityIssueEntryAdapter.timeFormatter.format(timeRecordStartStop.getDate());
            message += time + ": " + timeRecordStartStop.getType().getValue() + "\n";
        }

        String date = DateUtils.isToday(timeRecord.getDate().getTime()) ? "Today" :
                MainActivityIssueEntryAdapter.dateFormatter.format(timeRecord.getDate());

        new AlertDialog.Builder(MainActivityIssueEntryAdapter.context)
                .setTitle("Start/stop log of " + issue.getTrackorKey() +
                        " (" + date + ")")
                .setMessage(message)
                .show();
    }

    private void showAlert(Date date) {
        Dialog.OnDismissListener onDismissListener = dialog -> {
            dialog.dismiss();
            show();
        };

        new AlertDialog.Builder(MainActivityIssueEntryAdapter.context)
                .setTitle("Warning")
                .setMessage("Time record not found for date " + MainActivityIssueEntryAdapter.dateFormatter.format(date))
                .setPositiveButton("OK", (dialog, which) -> onDismissListener.onDismiss(dialog))
                .setOnDismissListener(onDismissListener)
                .show();
    }

    void show() {
        if (datePickerDialog == null) {
            datePickerDialog = MainActivityIssueEntryAdapter.createDatePickerForNow(onDateSetListener);

            datePickerDialog.setOnCancelListener(dialog -> isDateSet = false);
            datePickerDialog.setOnDismissListener(dialog -> {
                if (!isDateSet) {
                    return;
                }

                TimeRecord timeRecord = MainActivityIssueEntryAdapter.timeRecordDao.queryForIssueAndDate(issue, calendar.getTime());

                if (timeRecord == null) {
                    showAlert(calendar.getTime());
                    return;
                }

                showTimeRecordStartStopLog(timeRecord);
            });
        }

        datePickerDialog.show();
    }*/
}
