package ru.killer666.issuetimewatchdog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.orm.SugarRecord;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lombok.RequiredArgsConstructor;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboAppCompatActivity implements View.OnClickListener {
    private final List<Issue> items = Lists.newArrayList();
    private IssueEntryAdapter listAdapter;

    @InjectView(R.id.recyclerView)
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.listAdapter = new IssueEntryAdapter(this.items);

        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setAdapter(this.listAdapter);

        Iterator<Issue> iterator = SugarRecord.findAll(Issue.class);

        while (iterator.hasNext()) {
            this.items.add(iterator.next());
        }

        Collections.sort(this.items);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTimeRecordUpdated(WatchdogService.TimeRecordUpdatedEvent event) {
        TimeRecord timeRecord = SugarRecord.findById(TimeRecord.class, event.getTimeRecordId());
        Issue issue = this.findIssueById(timeRecord.getIssue().getId());

        this.listAdapter.notifyItemChanged(this.items.indexOf(issue));
    }

    private Issue findIssueById(long id) {
        for (Issue issue : this.items) {
            if (id == issue.getId()) {
                return issue;
            }
        }

        throw new IllegalStateException();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onTimeRecordUsing(WatchdogService.TimeRecordUsingEvent event) {
        TimeRecord timeRecord = SugarRecord.findById(TimeRecord.class, event.getTimeRecordId());
        Issue issue = this.findIssueById(timeRecord.getIssue().getId());

        int position = this.items.indexOf(issue);

        if (issue.getState() != IssueState.Working) {
            issue.setState(IssueState.Working);
        }

        this.listAdapter.notifyItemChanged(position);

        if (position > 0) {
            this.items.remove(position);
            this.items.add(0, issue);

            this.listAdapter.notifyItemMoved(position, 0);
        }
    }

    private void askNewIssueDialog(final Function<Issue, Void> callable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText input1 = new EditText(this);
        input1.setHint("Issue name");
        layout.addView(input1);

        final EditText input2 = new EditText(this);
        input2.setHint("Issue description");
        layout.addView(input2);

        builder
                .setTitle("New issue")
                .setView(layout)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Issue issue = new Issue();
                        issue.setName(input1.getText().toString());
                        issue.setDescription(input2.getText().toString());

                        callable.apply(issue);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void askRemoveIssueDialog(final Issue issue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setTitle("Confirm remove issue")
                .setMessage("Are you sure you want to remove issue " + issue.getReadableName() + "?")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (issue.getState() == IssueState.Working) {
                            MainActivity.this.stopService(new Intent(MainActivity.this, WatchdogService.class));
                        }

                        int position = MainActivity.this.items.indexOf(issue);

                        MainActivity.this.items.remove(position);
                        MainActivity.this.listAdapter.notifyItemRemoved(position);

                        issue.delete();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab: {

                // Create issue
                this.askNewIssueDialog(new Function<Issue, Void>() {
                    @Override
                    public Void apply(Issue issue) {
                        issue.save();

                        MainActivity.this.items.add(issue);
                        MainActivity.this.listAdapter.notifyItemInserted(MainActivity.this.items.size() - 1);

                        return null;
                    }
                });

                break;
            }
        }
    }

    @RequiredArgsConstructor
    public class IssueEntryAdapter extends RecyclerView.Adapter<ViewHolder> implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {
        private final List<Issue> items;
        private final NumberFormat numberFormat = new DecimalFormat();
        private final DateFormat dateTimeFormatter = DateFormat.getDateInstance();

        private static final String EXTRA_ISSUE_POSITION = "issuePosition";

        {
            this.numberFormat.setMaximumFractionDigits(2);
        }

        private void showTimeRecord(Issue issue) {
            String message = "";

            for (TimeRecord timeRecord : issue.getLastTimeRecords()) {
                message += Html.fromHtml(this.formatTimeRecordHistory(timeRecord)) + "\n";
            }

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Time records of " + issue.getReadableName())
                    .setMessage(message + "\n(Time records of last " + Issue.TIME_RECORD_SHOW_LIMIT + " days)")
                    .show();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_2, parent, false);

            view.setClickable(true);
            view.setOnClickListener(this);

            return new ViewHolder(view);
        }

        private String formatState(IssueState state) {
            boolean escape = state == IssueState.Working;
            return (escape ? "<b>" : "") + state.getValue() + (escape ? "</b>" : "");
        }

        private String formatTimeRecordHistory(TimeRecord timeRecord) {
            return (DateUtils.isToday(timeRecord.getDate().getTime()) ? "<b>Today</b>" :
                    this.dateTimeFormatter.format(timeRecord.getDate())) + ": " +
                    this.numberFormat.format(timeRecord.getWorkedTime()) + " hrs.";
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Issue issue = this.items.get(position);

            holder.text1.setText(Html.fromHtml("<b>" + issue.getName() + "</b> " + issue.getDescription()));
            holder.text2.setText(Html.fromHtml("Status: " + this.formatState(issue.getState())));

            TimeRecord timeRecord = issue.getLastTimeRecord();

            if (timeRecord != null) {
                holder.text2.append(" " + Html.fromHtml(this.formatTimeRecordHistory(timeRecord)));
            }
        }

        @Override
        public int getItemCount() {
            return this.items.size();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Issue issue = this.items.get(item.getIntent().getIntExtra(EXTRA_ISSUE_POSITION, -1));

            switch (item.getItemId()) {
                case R.id.action_timerecord: {
                    this.showTimeRecord(issue);
                    break;
                }
                case R.id.action_switchstate: {
                    if (issue.getState() == IssueState.Working) {
                        MainActivity.this.stopService(new Intent(MainActivity.this, WatchdogService.class));

                        issue.setState(IssueState.Idle);
                        this.showTimeRecord(issue);
                    } else {
                        for (Issue checkIssue : this.items) {
                            if (checkIssue.getState() == IssueState.Working) {
                                MainActivity.this.stopService(new Intent(MainActivity.this, WatchdogService.class));

                                checkIssue.setState(IssueState.Idle);
                                this.notifyItemChanged(this.items.indexOf(checkIssue));
                                break;
                            }
                        }

                        // Создаём TimeRecord, если необходимо
                        TimeRecord timeRecord = issue.getLastTimeRecord();

                        if (timeRecord == null || !DateUtils.isToday(timeRecord.getDate().getTime())) {
                            timeRecord = new TimeRecord(issue);
                            timeRecord.save();
                        }

                        issue.setState(IssueState.Working);

                        int position = this.items.indexOf(issue);

                        this.items.remove(position);
                        this.items.add(0, issue);

                        this.notifyItemMoved(position, 0);

                        MainActivity.this.startService(new Intent(MainActivity.this, WatchdogService.class)
                                .putExtra(WatchdogService.EXTRA_TIME_RECORD_ID, timeRecord.getId()));
                    }

                    this.notifyItemChanged(this.items.indexOf(issue));
                    break;
                }
            }

            return true;
        }

        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(MainActivity.this, v);

            popup.setOnMenuItemClickListener(this);
            popup.getMenuInflater().inflate(R.menu.menu_issue, popup.getMenu());

            int position = MainActivity.this.recyclerView.getChildViewHolder(v).getAdapterPosition();

            for (int i = 0; i < popup.getMenu().size(); i++) {
                popup.getMenu().getItem(i).setIntent(new Intent(MainActivity.this, MainActivity.class)
                        .putExtra(EXTRA_ISSUE_POSITION, position));
            }

            popup.show();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView text1;
        private TextView text2;

        public ViewHolder(View itemView) {
            super(itemView);

            this.text1 = (TextView) itemView.findViewById(android.R.id.text1);
            this.text2 = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }
}
