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
import ru.killer666.issuetimewatchdog.helper.DialogHelper;
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

    @Inject
    private DialogHelper dialogHelper;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonSelectIssueFilter.setOnClickListener(this);
        buttonChangeLoginCredentials.setOnClickListener(this);

        switchCreateTimeRecords.setChecked(createTimeRecordsPrefs.isEnabled());
        switchCreateTimeRecords.setOnCheckedChangeListener(this);

        updateLoginCredentials();
        updateFilters();
    }

    private void updateLoginCredentials() {
        if (apiAuthPrefs.isValid()) {
            textViewLoginCredentials.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            textViewLoginCredentials.setText(apiAuthPrefs.getLogin());
        }
    }

    private void updateFilters() {
        String issueFilter = filtersPrefs.getFilter(Issue.class);

        textViewIssueFilter.setTypeface(null, Typeface.NORMAL);
        if (issueFilter == null) {
            textViewIssueFilter.setTypeface(textViewIssueFilter.getTypeface(), Typeface.BOLD);
        }
        textViewIssueFilter.setText(issueFilter != null ? issueFilter : "None");
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
                selectorDialog.showFilterSelect("Issue", filtersPrefs.getFilter(Issue.class)).subscribe((filter) -> {
                    filtersPrefs.setFilter(Issue.class, filter);
                    updateFilters();
                });

                break;
            }
            case R.id.buttonChangeLoginCredentials: {
                askLoginCredentialsDialog();
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        createTimeRecordsPrefs.setEnabled(isChecked);
    }

    private void askLoginCredentialsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText input1 = new EditText(this);
        input1.setHint("Login");
        input1.setText(apiAuthPrefs.getLogin());
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
                        dialogHelper.warning("One of fields are empty!");
                        return;
                    }

                    apiAuthPrefs.setCredentials(input1.getText().toString(), input2.getText().toString());
                    updateLoginCredentials();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

}
