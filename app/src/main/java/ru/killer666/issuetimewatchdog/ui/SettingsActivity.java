package ru.killer666.issuetimewatchdog.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.inject.Inject;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import ru.killer666.issuetimewatchdog.R;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.prefs.ApiAuthPrefs;
import ru.killer666.issuetimewatchdog.prefs.CreateTimeRecordsPrefs;
import ru.killer666.issuetimewatchdog.prefs.FiltersPrefs;

@ContentView(R.layout.activity_settings)
public class SettingsActivity extends RoboAppCompatActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    @Inject
    private ApiAuthPrefs apiAuthPrefs;

    @Inject
    private CreateTimeRecordsPrefs createTimeRecordsPrefs;

    @Inject
    private FiltersPrefs filtersPrefs;

    @Inject
    private SelectorDialog selectorDialog;

    @InjectView(R.id.buttonSelectIssueFilter)
    private Button buttonSelectIssueFilter;

    @InjectView(R.id.textViewIssueFilter)
    private TextView textViewIssueFilter;

    @InjectView(R.id.switchCreateTimeRecords)
    private Switch switchCreateTimeRecords;

    @InjectView(R.id.buttonChangeLoginCredentials)
    private Button buttonChangeLoginCredentials;

    @InjectView(R.id.textViewLoginCredentials)
    private TextView textViewLoginCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.buttonSelectIssueFilter.setOnClickListener(this);
        this.buttonChangeLoginCredentials.setOnClickListener(this);

        this.switchCreateTimeRecords.setChecked(this.createTimeRecordsPrefs.isEnabled());
        this.switchCreateTimeRecords.setOnCheckedChangeListener(this);

        this.updateLoginCredentials();
        this.updateFilters();
    }

    private void updateLoginCredentials() {
        if (this.apiAuthPrefs.isValid()) {
            this.textViewLoginCredentials.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            this.textViewLoginCredentials.setText(this.apiAuthPrefs.getLogin());
        }
    }

    private void updateFilters() {
        String issueFilter = this.filtersPrefs.getFilter(Issue.class);

        this.textViewIssueFilter.setTypeface(null, Typeface.NORMAL);
        if (issueFilter == null) {
            this.textViewIssueFilter.setTypeface(this.textViewIssueFilter.getTypeface(), Typeface.BOLD);
        }
        this.textViewIssueFilter.setText(issueFilter != null ? issueFilter : "None");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSelectIssueFilter: {
                this.selectorDialog.showFilterSelect("Issue", this.filtersPrefs.getFilter(Issue.class)).subscribe((filter) -> {
                    SettingsActivity.this.filtersPrefs.setFilter(Issue.class, filter);
                    SettingsActivity.this.updateFilters();
                });

                break;
            }
            case R.id.buttonChangeLoginCredentials: {
                this.askLoginCredentialsDialog();
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        this.createTimeRecordsPrefs.setEnabled(isChecked);
    }

    private void askLoginCredentialsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText input1 = new EditText(this);
        input1.setHint("Login");
        input1.setText(this.apiAuthPrefs.getLogin());
        layout.addView(input1);

        final EditText input2 = new EditText(this);
        input2.setHint("Password");
        input2.setTransformationMethod(new PasswordTransformationMethod());
        layout.addView(input2);

        builder
                .setTitle("Change credentials for API request")
                .setView(layout)
                .setPositiveButton("Change", (dialog, which) -> {
                    if (TextUtils.isEmpty(input1.getText()) || TextUtils.isEmpty(input2.getText())) {
                        (new AlertDialog.Builder(this))
                                .setTitle("Error")
                                .setMessage("One of fields are empty!")
                                .show();

                        return;
                    }

                    SettingsActivity.this.apiAuthPrefs
                            .setCredentials(input1.getText().toString(), input2.getText().toString());
                    SettingsActivity.this.updateLoginCredentials();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

}
