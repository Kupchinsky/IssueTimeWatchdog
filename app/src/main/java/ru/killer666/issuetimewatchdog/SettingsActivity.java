package ru.killer666.issuetimewatchdog;

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

@ContentView(R.layout.activity_settings)
public class SettingsActivity extends RoboAppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    @Inject
    private LoginCredentials loginCredentials;
    @Inject
    private CreateTimeRecordsSettings createTimeRecordsSettings;
    @Inject
    private FiltersSettings filtersSettings;
    @Inject
    private FilterSelectorDialog filterSelectorDialog;

    @InjectView(R.id.buttonSelectIssueFilter)
    private Button buttonSelectIssueFilter;
    @InjectView(R.id.buttonSelectTimeRecordFilter)
    private Button buttonSelectTimeRecordFilter;
    @InjectView(R.id.textViewIssueFilter)
    private TextView textViewIssueFilter;
    @InjectView(R.id.textViewTimeRecordFilter)
    private TextView textViewTimeRecordFilter;
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
        this.buttonSelectTimeRecordFilter.setOnClickListener(this);
        this.buttonChangeLoginCredentials.setOnClickListener(this);

        this.switchCreateTimeRecords.setOnCheckedChangeListener(this);
        this.switchCreateTimeRecords.setChecked(this.createTimeRecordsSettings.isEnabled());

        this.updateLoginCredentials();
        this.updateFilters();
    }

    private void updateLoginCredentials() {
        if (this.loginCredentials.isValid()) {
            this.textViewLoginCredentials.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            this.textViewLoginCredentials.setText(this.loginCredentials.getLogin());
        }
    }

    private void updateFilters() {
        String issueFilter = this.filtersSettings.getIssueFilter();

        this.textViewIssueFilter.setTypeface(null, Typeface.NORMAL);
        if (issueFilter == null) {
            this.textViewIssueFilter.setTypeface(this.textViewIssueFilter.getTypeface(), Typeface.BOLD);
        }
        this.textViewIssueFilter.setText(issueFilter != null ? issueFilter : "None");

        String timerecordFilter = this.filtersSettings.getTimerecordFilter();

        this.textViewTimeRecordFilter.setTypeface(null, Typeface.NORMAL);
        if (timerecordFilter == null) {
            this.textViewTimeRecordFilter.setTypeface(this.textViewTimeRecordFilter.getTypeface(), Typeface.BOLD);
        }
        this.textViewTimeRecordFilter.setText(timerecordFilter != null ? timerecordFilter : "None");
    }

    private boolean checkIsValidLoginCredentials() {
        if (!this.loginCredentials.isValid()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder
                    .setTitle("Warning")
                    .setMessage("Set login credentials before!")
                    .setIcon(android.R.drawable.stat_sys_warning)
                    .setNegativeButton("OK", (dialog, which) -> dialog.cancel())
                    .show();

            return false;
        }

        return true;
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
                if (!this.checkIsValidLoginCredentials()) {
                    break;
                }

                this.filterSelectorDialog.show(Issue.class).subscribe((filter) -> {
                    SettingsActivity.this.filtersSettings.setIssueFilter(filter);
                    SettingsActivity.this.updateFilters();
                });

                break;
            }
            case R.id.buttonSelectTimeRecordFilter: {
                if (!this.checkIsValidLoginCredentials()) {
                    break;
                }

                this.filterSelectorDialog.show(TimeRecord.class).subscribe((filter) -> {
                    SettingsActivity.this.filtersSettings.setTimerecordFilter(filter);
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
        this.createTimeRecordsSettings.setEnabled(isChecked);
    }

    private void askLoginCredentialsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText input1 = new EditText(this);
        input1.setHint("Login");
        input1.setText(this.loginCredentials.getLogin());
        layout.addView(input1);

        final EditText input2 = new EditText(this);
        input2.setHint("Password");
        input2.setTransformationMethod(new PasswordTransformationMethod());
        layout.addView(input2);

        builder
                .setTitle("Change credentials")
                .setView(layout)
                .setPositiveButton("Change", (dialog, which) -> {
                    if (TextUtils.isEmpty(input1.getText()) || TextUtils.isEmpty(input2.getText())) {
                        (new AlertDialog.Builder(this))
                                .setTitle("Error")
                                .setMessage("One of fields are empty!")
                                .show();

                        return;
                    }

                    SettingsActivity.this.loginCredentials
                            .setCredentials(input1.getText().toString(), input2.getText().toString());
                    SettingsActivity.this.updateLoginCredentials();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }
}
