package ru.killer666.issuetimewatchdog.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.inject.Inject;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.ContextSingleton;
import ru.killer666.issuetimewatchdog.helper.DialogHelper;
import ru.killer666.issuetimewatchdog.helper.IssueSelectorDialogSettings;
import ru.killer666.issuetimewatchdog.R;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.event.IssueStateChangedEvent;
import ru.killer666.issuetimewatchdog.event.IssueTimeRecordsUploadedEvent;
import ru.killer666.issuetimewatchdog.helper.IssueHelper;
import ru.killer666.issuetimewatchdog.helper.TimeRecordHelper;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.IssueState;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStopType;
import ru.killer666.issuetimewatchdog.services.NotificationService;

@ContextSingleton
class MainActivityIssueEntryAdapter extends RecyclerView.Adapter<MainActivityIssueEntryAdapter.ViewHolder>
        implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {

    private static final String EXTRA_ISSUE_POSITION = "issuePosition";

    @Inject
    private IssueSelectorDialogSettings issueSelectorDialogSettings;

    @Inject
    private TimeRecordHelper timeRecordHelper;

    @Inject
    private TimeRecordDao timeRecordDao;

    @Inject
    private IssueHelper issueHelper;

    @Inject
    private IssueDao issueDao;

    @Inject
    private DialogHelper dialogHelper;

    @Inject
    private Context context;

    private final RecyclerView recyclerView;
    private final List<Issue> items;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIssueTimeRecordsUpdated(IssueTimeRecordsUploadedEvent event) {
        Issue issue = event.getIssue();

        if (issue.isRemoveAfterUpload()) {
            int position = items.indexOf(issue);
            items.remove(position);

            notifyItemRemoved(position);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIssueStateChanged(IssueStateChangedEvent event) {
        Issue issue = issueDao.queryForSameId(event.getIssue());
        if (IssueState.Working.equals(event.getOldState()) &&
                TimeRecordStartStopType.TypeStop.equals(event.getTimeRecordStartStopType())) {
            timeRecordHelper.showLastForIssue(event.getIssue());
        }

        notifyItemChanged(items.indexOf(issue));
    }

    MainActivityIssueEntryAdapter(Context context, RecyclerView recyclerView, List<Issue> items) {
        this.recyclerView = recyclerView;
        this.items = items;

        RoboGuice.injectMembers(context, this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);

        view.setClickable(true);
        view.setOnClickListener(this);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Issue issue = items.get(position);

        holder.text1.setText(Html.fromHtml("<b>" + issue.getTrackorKey() + "</b> " + issue.getSummary()));
        holder.text2.setText(Html.fromHtml("Work status: " + timeRecordHelper.formatState(issue.getState())));

        TimeRecord timeRecord = timeRecordDao.queryLastOfIssue(issue);

        if (timeRecord != null) {
            holder.text2.append(" " + Html.fromHtml(timeRecordHelper.formatHistory(timeRecord, false)));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // TODO: use this
    private DatePickerDialog createDatePickerForNow(DatePickerDialog.OnDateSetListener onDateSetListener) {
        Calendar calendar = Calendar.getInstance();
        return createDatePicker(onDateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    private DatePickerDialog createDatePicker(DatePickerDialog.OnDateSetListener onDateSetListener,
                                              int year, int monthOfYear, int dayOfMonth) {
        return new DatePickerDialog(context, onDateSetListener, year, monthOfYear, dayOfMonth);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Issue issue = items.get(item.getIntent().getIntExtra(EXTRA_ISSUE_POSITION, -1));

        switch (item.getItemId()) {
            case R.id.action_timerecord: {
                timeRecordHelper.showLastForIssue(issue);
                break;
            }
            case R.id.action_startstoplog: {
                // TODO
                //new TimeRecordStartStopLogDialog(issue).show();
                break;
            }
            case R.id.action_switchstate: {
                if (IssueState.Working.equals(issue.getState())) {
                    issueHelper.changeState(issue, IssueState.Idle, TimeRecordStartStopType.TypeStop);
                } else {
                    TimeRecord timeRecord = timeRecordHelper.getOrCreateLastTimeRecordForIssue(issue);
                    issueHelper.changeState(issue, IssueState.Working, TimeRecordStartStopType.TypeStart);

                    int position = items.indexOf(issue);

                    items.remove(position);
                    items.add(0, issue);

                    notifyItemMoved(position, 0);

                    context.startService(new Intent(context, NotificationService.class)
                            .putExtra(NotificationService.EXTRA_TIME_RECORD_ID, timeRecord.getId()));
                }

                break;
            }
            case R.id.action_show_info: {
                // TODO: implement (required v3 api for read config field labels)
                dialogHelper.warning("Not implemented now!");
                //dialogHelper.info(issueSelectorDialogSettings.getDetailsMessage(issue));
                break;
            }
            case R.id.action_change_status: {
                // TODO: implement (required v3 api for read all vtable values)
                dialogHelper.warning("Not implemented now!");
                break;
            }
            case R.id.action_add_unwatched_time: {
                Calendar calendar = Calendar.getInstance();
                // TODO: rewrite
/*                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        context,
                        (view, year, monthOfYear, dayOfMonth) -> {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, monthOfYear);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            if (!MyDateUtils.isCurrentMonth(calendar.getTime())) {
                                // Warning
                            }

                            timeRecordDao.queryForIssueAndDate(issue, calendar.getTime());

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
*/
                break;
            }
            case R.id.action_remove_watched_time: {
                // TODO
                break;
            }
            case R.id.action_upload_timerecords: {
                // TODO: start service with specific intent
                break;
            }
            case R.id.action_remove: {
                CharSequence[] items = new CharSequence[]{"Create time records and remove", "Remove", "Cancel"};

                (new AlertDialog.Builder(context))
                        .setTitle("Confirm remove issue")
                        .setSingleChoiceItems(items, -1, (dialog, action) -> {
                            dialog.dismiss();

                            // TODO
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                        .show();
                break;
            }
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        PopupMenu popup = new PopupMenu(context, v);

        popup.setOnMenuItemClickListener(this);
        popup.getMenuInflater().inflate(R.menu.menu_issue, popup.getMenu());

        int position = recyclerView.getChildViewHolder(v).getAdapterPosition();

        for (int i = 0; i < popup.getMenu().size(); i++) {
            popup.getMenu().getItem(i).setIntent(new Intent(context, MainActivity.class)
                    .putExtra(EXTRA_ISSUE_POSITION, position));
        }

        popup.show();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView text1;
        private TextView text2;

        ViewHolder(View itemView) {
            super(itemView);

            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text2 = (TextView) itemView.findViewById(android.R.id.text2);
        }

    }

}