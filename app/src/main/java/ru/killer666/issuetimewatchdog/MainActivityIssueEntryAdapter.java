package ru.killer666.issuetimewatchdog;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.ContextSingleton;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDaoImpl;
import ru.killer666.issuetimewatchdog.dao.TimeRecordStartStopDao;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.IssueState;
import ru.killer666.issuetimewatchdog.model.TimeRecord;

@ContextSingleton
class MainActivityIssueEntryAdapter extends RecyclerView.Adapter<MainActivityIssueEntryAdapter.ViewHolder> implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {
    @Inject
    private TimeRecordDao timeRecordDao;
    @Inject
    private TimeRecordStartStopDao timeRecordStartStopDao;
    @Inject
    private IssueSelectorDialogSettings issueSelectorDialogSettings;
    @Inject
    private IssueDao issueDao;
    @Inject
    private Context context;

    private final RecyclerView recyclerView;
    private final List<Issue> items;

    private final NumberFormat numberFormat = new DecimalFormat();
    private final DateFormat dateFormatter = DateFormat.getDateInstance();
    private final DateFormat timeFormatter = DateFormat.getTimeInstance();

    private static final String EXTRA_ISSUE_POSITION = "issuePosition";

    {
        this.numberFormat.setMaximumFractionDigits(2);
    }

    MainActivityIssueEntryAdapter(Context context, RecyclerView recyclerView, List<Issue> items) {
        this.recyclerView = recyclerView;
        this.items = items;

        RoboGuice.injectMembers(context, this);
    }

    void showTimeRecord(Issue issue) {
        String message = "";

        for (TimeRecord timeRecord : this.timeRecordDao.queryLastOfIssueList(issue)) {
            message += Html.fromHtml(this.formatTimeRecordHistory(timeRecord, true)) + "\n";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this.context)
                .setTitle("Time records of " + issue.getReadableName())
                .setMessage(message + "\n(Time records of last " + TimeRecordDaoImpl.SHOW_LIMIT + " days)")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        if (issue.getState() == IssueState.Working) {
            builder.setNeutralButton("Stop working", ((dialog, which) -> {
                dialog.dismiss();

                this.context.stopService(new Intent(this.context, WatchdogService.class));
                issue.setState(IssueState.Idle);

                TimeRecord timeRecord = this.timeRecordDao.queryLastOfIssue(issue);
                this.timeRecordStartStopDao.createWithType(timeRecord, TimeRecordStartStopType.TypeStop);

                this.notifyItemChanged(this.items.indexOf(issue));
            }));
        }

        builder.show();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(android.R.layout.simple_list_item_2, parent, false);

        view.setClickable(true);
        view.setOnClickListener(this);

        return new ViewHolder(view);
    }

    private String formatState(IssueState state) {
        boolean escape = state == IssueState.Working;
        return (escape ? "<b>" : "") + state.getValue() + (escape ? "</b>" : "");
    }

    private String formatTimeRecordHistory(TimeRecord timeRecord, boolean isLong) {
        String message = (DateUtils.isToday(timeRecord.getDate().getTime()) ? "<b>Today</b>" :
                this.dateFormatter.format(timeRecord.getDate())) + ": " +
                this.numberFormat.format(timeRecord.getWorkedTime());

        boolean isFullWrite = timeRecord.getWorkedTime() == timeRecord.getWroteTime();

        if (isLong) {
            message += " h. (wrote" + (isFullWrite ? "[full]" : "") + " " +
                    this.numberFormat.format(timeRecord.getWroteTime()) + " h.)";
        } else {
            message += "/" + this.numberFormat.format(timeRecord.getWroteTime()) + " (w" +
                    (isFullWrite ? "[f]" : "") + ") hrs.";
        }

        return message;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Issue issue = this.items.get(position);

        holder.text1.setText(Html.fromHtml("<b>" + issue.getTrackorKey() + "</b> " + issue.getSummary()));
        holder.text2.setText(Html.fromHtml("Work status: " + this.formatState(issue.getState())));

        TimeRecord timeRecord = this.timeRecordDao.queryLastOfIssue(issue);

        if (timeRecord != null) {
            holder.text2.append(" " + Html.fromHtml(this.formatTimeRecordHistory(timeRecord, false)));
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    private DatePickerDialog createDatePickerForNow(DatePickerDialog.OnDateSetListener onDateSetListener) {
        Calendar calendar = Calendar.getInstance();
        return this.createDatePicker(onDateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    private DatePickerDialog createDatePicker(DatePickerDialog.OnDateSetListener onDateSetListener,
                                              int year, int monthOfYear, int dayOfMonth) {
        return new DatePickerDialog(this.context, onDateSetListener, year, monthOfYear, dayOfMonth);
    }

    private class TimeRecordStartStopLogDialog {
        private final Issue issue;

        private DatePickerDialog datePickerDialog;
        private boolean isDateSet;

        private final Calendar calendar = Calendar.getInstance();
        private final DatePickerDialog.OnDateSetListener onDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            this.calendar.set(Calendar.YEAR, year);
            this.calendar.set(Calendar.MONTH, monthOfYear);
            this.calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            this.calendar.set(Calendar.HOUR, 0);
            this.calendar.set(Calendar.MINUTE, 0);
            this.calendar.set(Calendar.SECOND, 0);
            this.calendar.set(Calendar.MILLISECOND, 0);

            this.isDateSet = true;
        };

        TimeRecordStartStopLogDialog(Issue issue) {
            this.issue = issue;
        }

        private void showTimeRecordStartStopLog(TimeRecord timeRecord) {
            String message = "";

            for (TimeRecordStartStop timeRecordStartStop :
                    MainActivityIssueEntryAdapter.this.timeRecordStartStopDao.queryOfTimeRecordList(timeRecord)) {
                String time = MainActivityIssueEntryAdapter.this.timeFormatter.format(timeRecordStartStop.getDate());
                message += time + ": " + timeRecordStartStop.getType().getValue() + "\n";
            }

            String date = DateUtils.isToday(timeRecord.getDate().getTime()) ? "Today" :
                    MainActivityIssueEntryAdapter.this.dateFormatter.format(timeRecord.getDate());

            new AlertDialog.Builder(MainActivityIssueEntryAdapter.this.context)
                    .setTitle("Start/stop log of " + this.issue.getTrackorKey() +
                            " (" + date + ")")
                    .setMessage(message)
                    .show();
        }

        private void showAlert(Date date) {
            Dialog.OnDismissListener onDismissListener = dialog -> {
                dialog.dismiss();
                this.show();
            };

            new AlertDialog.Builder(MainActivityIssueEntryAdapter.this.context)
                    .setTitle("Warning")
                    .setMessage("Time record not found for date " + MainActivityIssueEntryAdapter.this.dateFormatter.format(date))
                    .setPositiveButton("OK", (dialog, which) -> onDismissListener.onDismiss(dialog))
                    .setOnDismissListener(onDismissListener)
                    .show();
        }

        void show() {
            if (this.datePickerDialog == null) {
                this.datePickerDialog = MainActivityIssueEntryAdapter.this.createDatePickerForNow(this.onDateSetListener);

                this.datePickerDialog.setOnCancelListener(dialog -> this.isDateSet = false);
                this.datePickerDialog.setOnDismissListener(dialog -> {
                    if (!this.isDateSet) {
                        return;
                    }

                    TimeRecord timeRecord = MainActivityIssueEntryAdapter.this.timeRecordDao.queryForIssueAndDate(this.issue, this.calendar.getTime());

                    if (timeRecord == null) {
                        this.showAlert(this.calendar.getTime());
                        return;
                    }

                    this.showTimeRecordStartStopLog(timeRecord);
                });
            }

            this.datePickerDialog.show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    private void onTimeRecordsUpdated(CreateTimeRecordsService.OnTimeRecordsUpdatedEvent event) {
        Issue issue = event.getIssue();

        if (issue.isAutoRemove()) {
            int position = this.items.indexOf(issue);
            this.items.remove(position);

            this.notifyItemRemoved(position);
        }
    }

    private void removeIssue(Issue issue, boolean update) {
        if (issue.getState() == IssueState.Working) {
            this.context.stopService(new Intent(this.context, WatchdogService.class));
        }

        issue.setAutoRemove(true);
        this.issueDao.update(issue);

        if (update) {
            this.context.startActivity(new Intent(this.context, CreateTimeRecordsService.class)
                    .setAction(CreateTimeRecordsService.ACTION_UPDATE_SINGLE)
                    .putExtra(CreateTimeRecordsService.EXTRA_ISSUE_ID, issue.getId()));
        } else {
            this.issueDao.deleteWithAllChilds(issue);
            EventBus.getDefault().post(new CreateTimeRecordsService.OnTimeRecordsUpdatedEvent(issue));
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Issue issue = this.items.get(item.getIntent().getIntExtra(EXTRA_ISSUE_POSITION, -1));

        switch (item.getItemId()) {
            case R.id.action_timerecord: {
                this.showTimeRecord(issue);
                break;
            }
            case R.id.action_startstoplog: {
                new TimeRecordStartStopLogDialog(issue).show();
                break;
            }
            case R.id.action_switchstate: {
                if (issue.getState() == IssueState.Working) {
                    this.context.stopService(new Intent(this.context, WatchdogService.class));

                    issue.setState(IssueState.Idle);

                    TimeRecord timeRecord = this.timeRecordDao.queryLastOfIssue(issue);
                    this.timeRecordStartStopDao.createWithType(timeRecord, TimeRecordStartStopType.TypeStop);

                    this.showTimeRecord(issue);
                } else {
                    for (Issue checkIssue : this.items) {
                        if (checkIssue.getState() == IssueState.Working) {
                            this.context.stopService(new Intent(this.context, WatchdogService.class));
                            checkIssue.setState(IssueState.Idle);

                            TimeRecord timeRecord = this.timeRecordDao.queryLastOfIssue(checkIssue);
                            this.timeRecordStartStopDao.createWithType(timeRecord, TimeRecordStartStopType.TypeStopForOtherTask);

                            this.notifyItemChanged(this.items.indexOf(checkIssue));
                            break;
                        }
                    }

                    // Создаём TimeRecord, если необходимо
                    TimeRecord timeRecord = this.timeRecordDao.queryLastOfIssue(issue);

                    if (timeRecord == null || !DateUtils.isToday(timeRecord.getDate().getTime())) {
                        timeRecord = new TimeRecord(issue);
                        this.timeRecordDao.create(timeRecord);
                    }

                    issue.setState(IssueState.Working);

                    int position = this.items.indexOf(issue);

                    this.items.remove(position);
                    this.items.add(0, issue);

                    this.notifyItemMoved(position, 0);

                    this.context.startService(new Intent(this.context, WatchdogService.class)
                            .putExtra(WatchdogService.EXTRA_TIME_RECORD_ID, timeRecord.getId()));
                }

                this.notifyItemChanged(this.items.indexOf(issue));
                break;
            }
            case R.id.action_show_info: {
                (new AlertDialog.Builder(this.context))
                        .setTitle("Information")
                        .setMessage(this.issueSelectorDialogSettings.getDetailsMessage(issue, false))
                        .show();
                break;
            }
            case R.id.action_change_status: {
                (new AlertDialog.Builder(this.context))
                        .setTitle("Select new status")
                        .setSingleChoiceItems(Issue.getStatuses(),
                                Arrays.asList(Issue.getStatuses()).indexOf(issue.getStatus()),
                                (dialog, itemIndex) -> {
                                    dialog.dismiss();

                                    String newStatus = Issue.getStatuses()[itemIndex];

                                    if (newStatus.equals(issue.getStatus())) {
                                        return;
                                    }

                                    // TODO: update trackor with new status
                                })
                        .show();

                break;
            }
            case R.id.action_add_unwatched_time: {
                Calendar calendar = Calendar.getInstance();
                // TODO: rewrite
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this.context,
                        (view, year, monthOfYear, dayOfMonth) -> {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, monthOfYear);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            if (!MyDateUtils.isCurrentMonth(calendar.getTime())) {
                                // Warning
                            }

                            this.timeRecordDao.queryForIssueAndDate(issue, calendar.getTime());

                            // TODO: if contains time records:
                            // TODO: Ask confirm
                            // TODO: Else: Ask confirm to create new time record for this date
                            // TODO: Сделать активность и сущности просмотра логов заливки
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();

                break;
            }
            case R.id.action_remove_watched_time: {
                // TODO
                break;
            }
            case R.id.action_update_timerecords: {
                // TODO: start service with specific intent
                break;
            }
            case R.id.action_remove: {
                (new AlertDialog.Builder(this.context))
                        .setTitle("Confirm remove issue")
                        .setMessage("Are you sure you want to remove issue " + issue.getReadableName() + " from local database?\n\n" +
                                "Note: you can also remove issue with time records that not uploaded to Trackor")
                        .setNeutralButton("Upload and remove", (dialog, which) -> this.removeIssue(issue, true))
                        .setPositiveButton("Remove without upload", (dialog, which) ->
                                (new AlertDialog.Builder(this.context))
                                        .setTitle("Confirm remove issue without upload")
                                        .setMessage("Confirm this action?")
                                        .setPositiveButton("Remove without upload", (dialog1, which1) -> this.removeIssue(issue, false))
                                        .setNegativeButton("Cancel", (dialog1, which1) -> dialog.cancel())
                                        .show())
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                        .show();
                break;
            }
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        PopupMenu popup = new PopupMenu(this.context, v);

        popup.setOnMenuItemClickListener(this);
        popup.getMenuInflater().inflate(R.menu.menu_issue, popup.getMenu());

        int position = this.recyclerView.getChildViewHolder(v).getAdapterPosition();

        for (int i = 0; i < popup.getMenu().size(); i++) {
            popup.getMenu().getItem(i).setIntent(new Intent(this.context, MainActivity.class)
                    .putExtra(EXTRA_ISSUE_POSITION, position));
        }

        popup.show();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView text1;
        private TextView text2;

        ViewHolder(View itemView) {
            super(itemView);

            this.text1 = (TextView) itemView.findViewById(android.R.id.text1);
            this.text2 = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }
}