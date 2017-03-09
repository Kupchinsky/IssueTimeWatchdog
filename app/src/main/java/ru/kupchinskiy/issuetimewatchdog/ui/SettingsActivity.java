package ru.kupchinskiy.issuetimewatchdog.ui;

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

import retrofit2.Call;
import retrofit2.Response;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import ru.kupchinskiy.issuetimewatchdog.R;
import ru.kupchinskiy.issuetimewatchdog.helper.ApiCallback;
import ru.kupchinskiy.issuetimewatchdog.helper.DialogHelper;
import ru.kupchinskiy.issuetimewatchdog.helper.RemoteUserSettings;
import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.prefs.ApiAuthPrefs;
import ru.kupchinskiy.issuetimewatchdog.prefs.CreateTimeRecordsPrefs;
import ru.kupchinskiy.issuetimewatchdog.prefs.FiltersPrefs;
import ru.kupchinskiy.issuetimewatchdog.prefs.ViewPrefs;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient;
import rx.Subscriber;
import rx.internal.util.ActionNotificationObserver;

import static rx.Notification.Kind.OnCompleted;

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
    private ViewPrefs viewPrefs;

    @Inject
    private SelectorDialog selectorDialog;

    @Inject
    private DialogHelper dialogHelper;

    @Inject
    private RemoteUserSettings remoteUserSettings;

    @Inject
    private ApiClient apiClient;

    @InjectView(R.id.buttonSelectIssueFilter)
    private Button buttonSelectIssueFilter;

    @InjectView(R.id.buttonSelectIssueView)
    private Button buttonSelectIssueView;

    @InjectView(R.id.textViewIssueFilter)
    private TextView textViewIssueFilter;

    @InjectView(R.id.textViewIssueView)
    private TextView textViewIssueView;

    @InjectView(R.id.switchCreateTimeRecords)
    private Switch switchCreateTimeRecords;

    @InjectView(R.id.buttonChangeLoginCredentials)
    private Button buttonChangeLoginCredentials;

    @InjectView(R.id.textViewLoginCredentials)
    private TextView textViewLoginCredentials;

    @InjectView(R.id.buttonReloadUserSettings)
    private Button buttonReloadUserSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonSelectIssueFilter.setOnClickListener(this);
        buttonSelectIssueView.setOnClickListener(this);
        buttonChangeLoginCredentials.setOnClickListener(this);
        buttonReloadUserSettings.setOnClickListener(this);

        switchCreateTimeRecords.setChecked(createTimeRecordsPrefs.isEnabled());
        switchCreateTimeRecords.setOnCheckedChangeListener(this);

        updateLoginCredentials();
        updateFilters();
        updateViews();
    }

    private void updateLoginCredentials() {
        if (apiAuthPrefs.isValidCredentials()) {
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

    private void updateViews() {
        String issueView = viewPrefs.getView(Issue.class);

        textViewIssueView.setTypeface(null, Typeface.NORMAL);
        if (issueView == null) {
            textViewIssueView.setTypeface(textViewIssueView.getTypeface(), Typeface.BOLD);
        }
        textViewIssueView.setText(issueView != null ? issueView : "None");
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
            case R.id.buttonSelectIssueView: {
                selectorDialog.showViewSelect("Issue", viewPrefs.getView(Issue.class)).subscribe((view) -> {
                    viewPrefs.setView(Issue.class, view);
                    updateViews();
                });
                break;
            }
            case R.id.buttonChangeLoginCredentials: {
                askLoginCredentialsDialog();
                break;
            }
            case R.id.buttonReloadUserSettings: {
                remoteUserSettings.requestRemoteUserSettings().subscribe(new Subscriber<Void>() {

                    @Override
                    public void onStart() {
                        dialogHelper.showProgressDialog();
                    }

                    @Override
                    public void onCompleted() {
                        dialogHelper.dismissProgressDialog();
                        dialogHelper.info("Reloaded successfully!");
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialogHelper.error(e.getMessage());
                    }

                    @Override
                    public void onNext(Void result) {
                    }

                });
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
                .setTitle("Change credentials for API requests")
                .setView(layout)
                .setPositiveButton("Change", (dialog, which) -> {
                    if (TextUtils.isEmpty(input1.getText()) || TextUtils.isEmpty(input2.getText())) {
                        dialogHelper.warning("One of fields are empty!");
                        return;
                    }

                    String login = input1.getText().toString();
                    String password = input2.getText().toString();
                    String oldCredentials = apiAuthPrefs.getCredentials();
                    apiAuthPrefs.updateCredentials(login, password);

                    // Check credentials
                    dialogHelper.showProgressDialog();
                    Call<Void> call = apiClient.v2Authorize();
                    call.enqueue(new ApiCallback<Void>(this) {
                        @Override
                        public void onComplete() {
                            dialogHelper.dismissProgressDialog();
                        }

                        @Override
                        public void onError() {
                            apiAuthPrefs.setCredentials(oldCredentials);
                        }

                        @Override
                        public void onSuccess(Response<Void> response) {
                            apiAuthPrefs.saveCredentials(login, password);
                            updateLoginCredentials();

                            // Update remoteUserSettings
                            dialogHelper.showProgressDialog();
                            remoteUserSettings.requestRemoteUserSettings().subscribe(new ActionNotificationObserver<>(notification -> {
                                if (OnCompleted.equals(notification.getKind())) {
                                    dialogHelper.dismissProgressDialog();
                                }
                            }));
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

}
