package ru.kupchinskiy.issuetimewatchdog.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.inject.Inject;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.ContextSingleton;
import ru.kupchinskiy.issuetimewatchdog.R;
import ru.kupchinskiy.issuetimewatchdog.dao.IssueDao;
import ru.kupchinskiy.issuetimewatchdog.dao.TimeRecordDao;
import ru.kupchinskiy.issuetimewatchdog.event.IssueStateChangedEvent;
import ru.kupchinskiy.issuetimewatchdog.event.IssueTimeRecordChangedEvent;
import ru.kupchinskiy.issuetimewatchdog.event.IssueTimeRecordsUploadCompleteEvent;
import ru.kupchinskiy.issuetimewatchdog.helper.ApiClientWithObservables;
import ru.kupchinskiy.issuetimewatchdog.helper.DialogHelper;
import ru.kupchinskiy.issuetimewatchdog.helper.IssueHelper;
import ru.kupchinskiy.issuetimewatchdog.helper.IssueSelectorDialogSettings;
import ru.kupchinskiy.issuetimewatchdog.helper.MyDateUtils;
import ru.kupchinskiy.issuetimewatchdog.helper.RemoteUserSettings;
import ru.kupchinskiy.issuetimewatchdog.helper.TimeRecordHelper;
import ru.kupchinskiy.issuetimewatchdog.helper.TimeRecordLogHelper;
import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.model.IssueState;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecord;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecordLogType;
import ru.kupchinskiy.issuetimewatchdog.prefs.ViewPrefs;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3TrackorTypeSpec;
import ru.kupchinskiy.issuetimewatchdog.services.NotificationService;
import rx.Observable;
import rx.Observer;

@ContextSingleton
class MainActivityIssueEntryAdapter extends RecyclerView.Adapter<MainActivityIssueEntryAdapter.ViewHolder>
        implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {

    private static final String EXTRA_ISSUE_POSITION = "issuePosition";

    @Inject
    private IssueSelectorDialogSettings issueSelectorDialogSettings;

    @Inject
    private TimeRecordHelper timeRecordHelper;

    @Inject
    private TimeRecordLogHelper timeRecordLogHelper;

    @Inject
    private TimeRecordDao timeRecordDao;

    @Inject
    private IssueHelper issueHelper;

    @Inject
    private IssueDao issueDao;

    @Inject
    private DialogHelper dialogHelper;

    @Inject
    private RemoteUserSettings remoteUserSettings;

    @Inject
    private ApiClientWithObservables apiClientWithObservables;

    @Inject
    private ViewPrefs viewPrefs;

    @Inject
    private Context context;

    private final RecyclerView recyclerView;
    private final List<Issue> items;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIssueTimeRecordsUploadComplete(IssueTimeRecordsUploadCompleteEvent event) {
        if (event.isErrored()) {
            return;
        }

        Issue issue = event.getIssue();
        int position = items.indexOf(issue);

        if (issue.isRemoveAfterUpload()) {
            items.remove(position);

            notifyItemRemoved(position);
        } else {
            notifyItemChanged(position);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIssueStateChanged(IssueStateChangedEvent event) {
        Issue issue = issueDao.queryForSameId(event.getIssue());
        if (IssueState.Working.equals(event.getOldState()) &&
                TimeRecordLogType.TypeIdle.equals(event.getTimeRecordLogType())) {
            timeRecordHelper.showLastForIssue(event.getIssue());
        }

        notifyItemChanged(items.indexOf(issue));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIssueTimeRecordChangedEvent(IssueTimeRecordChangedEvent event) {
        Issue issue = issueDao.queryForSameId(event.getIssue());
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

    private DatePickerDialog createDatePickerForNow(DatePickerDialog.OnDateSetListener onDateSetListener) {
        Calendar calendar = Calendar.getInstance();
        return createDatePicker(onDateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    private DatePickerDialog createDatePicker(DatePickerDialog.OnDateSetListener onDateSetListener,
                                              int year, int monthOfYear, int dayOfMonth) {
        return new DatePickerDialog(context, onDateSetListener, year, monthOfYear, dayOfMonth);
    }

    private Observable<TimeRecord> selectTimeRecordByDate(Issue issue) {
        return Observable.defer(() -> Observable.create(subscriber -> {
            DatePickerDialog datePickerDialog = createDatePickerForNow((view, year, monthOfYear, dayOfMonth) -> {
                Date date = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();

                if (!MyDateUtils.isCurrentMonth(date)) {
                    dialogHelper.warning("Selected time record isn't in current month!");
                    subscriber.onCompleted();
                    return;
                }

                TimeRecord timeRecord = timeRecordDao.queryForIssueAndDate(issue, date);

                if (timeRecord == null) {
                    dialogHelper.warning("No time records found at " +
                            remoteUserSettings.getDateFormatter().format(date));
                } else {
                    subscriber.onNext(timeRecord);
                }

                subscriber.onCompleted();
            });
            Pair<Long, Long> maxMinDates = timeRecordDao.queryForMaxMinDateOfIssue(issue);
            if (maxMinDates == null) {
                dialogHelper.warning("No time records found!");
                return;
            }

            datePickerDialog.getDatePicker().setMaxDate(maxMinDates.first);
            datePickerDialog.getDatePicker().setMinDate(maxMinDates.second);
            datePickerDialog.show();
        }));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int position = item.getIntent().getIntExtra(EXTRA_ISSUE_POSITION, -1);
        Issue issue = items.get(position);

        switch (item.getItemId()) {
            case R.id.action_timerecord: {
                timeRecordHelper.showLastForIssue(issue);
                break;
            }
            case R.id.action_startstoplog: {
                selectTimeRecordByDate(issue).subscribe(timeRecord -> timeRecordLogHelper.showForTimeRecord(timeRecord));
                break;
            }
            case R.id.action_switchstate: {
                if (IssueState.Working.equals(issue.getState())) {
                    issueHelper.changeState(issue, IssueState.Idle, TimeRecordLogType.TypeIdle);
                } else {
                    TimeRecord timeRecord = timeRecordHelper.getOrCreateLastTimeRecordForIssue(issue);
                    issueHelper.changeState(issue, IssueState.Working, TimeRecordLogType.TypeWorking);

                    items.remove(position);
                    items.add(0, issue);

                    notifyItemMoved(position, 0);

                    context.startService(new Intent(context, NotificationService.class)
                            .putExtra(NotificationService.EXTRA_TIME_RECORD_ID, timeRecord.getId()));
                }

                break;
            }
            case R.id.action_show_info: {
                String view = viewPrefs.getView(Issue.class);

                dialogHelper.showProgressDialog();
                apiClientWithObservables.v3TrackorTypeSpecs(Issue.class, view).subscribe(new Observer<List<V3TrackorTypeSpec>>() {
                    @Override
                    public void onCompleted() {
                        dialogHelper.dismissProgressDialog();
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<V3TrackorTypeSpec> trackorTypeSpecs) {
                        dialogHelper.info(issueSelectorDialogSettings.getDetailsMessage(issue, trackorTypeSpecs));
                    }
                });
                break;
            }
            case R.id.action_change_status: {
                // TODO: implement (required v3 api for read all vtable values)
                dialogHelper.warnNotImplemented();
                break;
            }
            case R.id.action_add_unwatched_time: {
                selectTimeRecordByDate(issue).subscribe(timeRecord -> {
                    dialogHelper.showInputNumberDialog("Enter hours for add:").subscribe(value -> {
                        timeRecordHelper.increaseTime(timeRecord, value);

                        dialogHelper.info("Successfully added hours for issue " + issue.getReadableName() +
                                " and date " + remoteUserSettings.getDateFormatter().format(timeRecord.getDate()) +
                                ": " + remoteUserSettings.getNumberFormatter().format(value) + " h.");
                    });
                });
                break;
            }
            case R.id.action_remove_watched_time: {
                selectTimeRecordByDate(issue).subscribe(timeRecord -> {
                    dialogHelper.showInputNumberDialog("Enter hours for remove:").subscribe(value -> {
                        if (timeRecordHelper.decreaseTime(timeRecord, value)) {
                            dialogHelper.info("Successfully removed hours for issue " + issue.getReadableName() +
                                    " and date " + remoteUserSettings.getDateFormatter().format(timeRecord.getDate()) +
                                    ": " + remoteUserSettings.getNumberFormatter().format(value) + " h.");
                        }
                    });
                });
                break;
            }
            case R.id.action_upload_timerecords: {
                timeRecordHelper.startUploadSingle(issue);
                break;
            }
            case R.id.action_timerecords_remote: {
                // TODO: implement (required v3 api for read all config field labels)
                dialogHelper.warnNotImplemented();
                break;
            }
            case R.id.action_remove: {
                CharSequence[] dialogItems = new CharSequence[]{"Create time records and remove", "Remove", "Cancel"};

                (new AlertDialog.Builder(context))
                        .setTitle("Confirm remove issue")
                        .setSingleChoiceItems(dialogItems, -1, (dialog, action) -> {
                            if (action == 2) {
                                dialog.cancel();
                                return;
                            }

                            dialog.dismiss();

                            boolean isUpload = action == 0;
                            issueHelper.remove(issue, isUpload);

                            if (!isUpload) {
                                items.remove(position);
                                notifyItemRemoved(position);
                            }
                        })
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