package ru.killer666.issuetimewatchdog;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import roboguice.inject.InjectView;

public class MainActivity extends RoboAppCompatActivity implements View.OnClickListener {
    static final String ACTION_SHOW_TIMERECORD = "showTimeRecord";
    static final String EXTRA_ISSUE_ID = "issueId";

    private final List<Issue> items = Lists.newArrayList();
    private MainActivityIssueEntryAdapter listAdapter;

    @Inject
    private IssueDao issueDao;
    @Inject
    private Issue.Comparator issueComparator;
    @Inject
    private SelectorDialog selectorDialog;
    @Inject
    private FiltersSettings filtersSettings;
    @Inject
    private IssueSelectorDialogSettings issueSelectorDialogSettings;

    @InjectView(R.id.recyclerView)
    private RecyclerView recyclerView;
    @InjectView(R.id.toolbar)
    private Toolbar toolbar;
    @InjectView(R.id.fab)
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.setSupportActionBar(this.toolbar);

        this.fab.setOnClickListener(this);

        this.listAdapter = new MainActivityIssueEntryAdapter(this, this.recyclerView, this.items);

        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setAdapter(this.listAdapter);

        this.items.addAll(this.issueDao.queryNotAutoRemove());
        Collections.sort(this.items, this.issueComparator);

        if (this.getIntent().getAction().equals(ACTION_SHOW_TIMERECORD) && this.getIntent().hasExtra(EXTRA_ISSUE_ID)) {
            int issueId = this.getIntent().getIntExtra(EXTRA_ISSUE_ID, 0);
            Issue issue = this.issueDao.queryForId(issueId);

            if (issue != null) {
                this.listAdapter.showTimeRecord(issue);
            }
        }
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
    public void onTimeRecordUpdated(WatchdogService.OnTimeRecordUpdatedEvent event) {
        this.listAdapter.notifyItemChanged(this.items.indexOf(event.getTimeRecord().getIssue()));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onTimeRecordUsing(WatchdogService.OnTimeRecordUsingEvent event) {
        Issue issue = event.getTimeRecord().getIssue();

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab: {
                this.selectorDialog.showTrackorReadSelect(Issue.class, this.filtersSettings.getIssueFilter(),
                        this.issueSelectorDialogSettings)
                        .subscribe(issue -> {
                            if (issue.isAutoRemove()) {
                                issue.setAutoRemove(false);
                            }

                            this.issueDao.createOrUpdate(issue);
                            int position = this.items.indexOf(issue);

                            if (position == -1) {
                                this.items.add(issue);
                                this.listAdapter.notifyItemInserted(this.items.size() - 1);
                            } else {
                                this.listAdapter.notifyItemChanged(position);
                            }
                        });
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                this.startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
