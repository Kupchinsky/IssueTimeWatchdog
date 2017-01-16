package ru.killer666.issuetimewatchdog;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FiltersSettings {

    private static final String PREFS_ISSUE_FILTER = "issue_filter";
    private static final String PREFS_TIMERECORD_FILTER = "timerecord_filter";

    private final SharedPreferences preferences;

    @Inject
    private FiltersSettings(android.app.Application application) {
        this.preferences = application.getSharedPreferences("filters_settings", Context.MODE_PRIVATE);
    }

    public void setIssueFilter(String value) {
        SharedPreferences.Editor editor = this.preferences.edit();

        editor.putString(PREFS_ISSUE_FILTER, value);
        editor.apply();
    }

    public void setTimerecordFilter(String value) {
        SharedPreferences.Editor editor = this.preferences.edit();

        editor.putString(PREFS_TIMERECORD_FILTER, value);
        editor.apply();
    }

    public String getIssueFilter() {
        return this.preferences.getString(PREFS_ISSUE_FILTER, null);
    }

    public String getTimerecordFilter() {
        return this.preferences.getString(PREFS_TIMERECORD_FILTER, null);
    }

}
