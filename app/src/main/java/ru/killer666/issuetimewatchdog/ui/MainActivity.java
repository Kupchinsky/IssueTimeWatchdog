package ru.killer666.issuetimewatchdog.ui;

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

import java.util.Collections;
import java.util.List;

import roboguice.inject.InjectView;
import ru.killer666.issuetimewatchdog.R;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.helper.DialogHelper;
import ru.killer666.issuetimewatchdog.helper.IssueComparator;
import ru.killer666.issuetimewatchdog.helper.IssueHelper;
import ru.killer666.issuetimewatchdog.helper.IssueSelectorDialogSettings;
import ru.killer666.issuetimewatchdog.helper.ServiceHelper;
import ru.killer666.issuetimewatchdog.helper.TimeRecordHelper;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.IssueState;
import ru.killer666.issuetimewatchdog.model.TimeRecordLogType;
import ru.killer666.issuetimewatchdog.prefs.FiltersPrefs;
import ru.killer666.issuetimewatchdog.services.NotificationService;

public class MainActivity extends RoboAppCompatActivity implements View.OnClickListener {

    public static final String ACTION_SHOW_TIMERECORD = "showTimeRecord";
    public static final String EXTRA_ISSUE_ID = "issueId";

    private final List<Issue> items = Lists.newArrayList();
    private MainActivityIssueEntryAdapter listAdapter;

    @Inject
    private IssueDao issueDao;

    @Inject
    private IssueComparator issueComparator;

    @Inject
    private SelectorDialog selectorDialog;

    @Inject
    private FiltersPrefs filtersPrefs;

    @Inject
    private IssueSelectorDialogSettings issueSelectorDialogSettings;

    @Inject
    private TimeRecordHelper timeRecordHelper;

    @Inject
    private IssueHelper issueHelper;

    @Inject
    private DialogHelper dialogHelper;

    @Inject
    private ServiceHelper serviceHelper;

    @InjectView(R.id.recyclerView)
    private RecyclerView recyclerView;

    @InjectView(R.id.toolbar)
    private Toolbar toolbar;

    @InjectView(R.id.fab)
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(this);

        listAdapter = new MainActivityIssueEntryAdapter(this, recyclerView, items);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(listAdapter);

        items.addAll(issueDao.queryNotAutoRemove());
        Collections.sort(items, issueComparator);

        if (getIntent().getAction().equals(ACTION_SHOW_TIMERECORD) && getIntent().hasExtra(EXTRA_ISSUE_ID)) {
            int issueId = getIntent().getIntExtra(EXTRA_ISSUE_ID, 0);
            Issue issue = issueDao.queryForId(issueId);

            if (issue != null) {
                timeRecordHelper.showLastForIssue(issue);
            }
        }

        // Start notification service if working issue found and no service running
        Issue workingIssue = issueDao.queryWorkingState();
        if (workingIssue != null && !serviceHelper.isRunning(NotificationService.class)) {
            issueHelper.changeState(workingIssue, IssueState.Idle, TimeRecordLogType.TypeIdleByKillApp);
            issueHelper.changeState(workingIssue, IssueState.Working, TimeRecordLogType.TypeWorking);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(listAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(listAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab: {
                String filter = filtersPrefs.getFilter(Issue.class);
                if (filter == null) {
                    dialogHelper.warning("No filter set for Issue");
                    return;
                }

                selectorDialog.showTrackorReadSelectByFilter(Issue.class, filter,
                        issueSelectorDialogSettings)
                        .subscribe(issue -> {
                            if (issueDao.trackorKeyExists(issue.getTrackorKey())) {
                                dialogHelper.warning("Issue already in list!");
                                recyclerView.scrollToPosition(items.indexOf(issue));
                                return;
                            }

                            issueDao.createOrUpdate(issue);

                            int position = items.indexOf(issue);

                            if (position == -1) {
                                items.add(issue);
                                listAdapter.notifyItemInserted(items.size() - 1);
                            } else {
                                listAdapter.notifyItemChanged(position);
                            }
                        });
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
