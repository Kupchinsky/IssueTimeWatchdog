package ru.killer666.issuetimewatchdog.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ru.killer666.issuetimewatchdog.model.Trackor;

@Singleton
public class FiltersPrefs {

    private final SharedPreferences preferences;

    @Inject
    private FiltersPrefs(android.app.Application application) {
        preferences = application.getSharedPreferences("filters_prefs", Context.MODE_PRIVATE);
    }

    public void setFilter(Class<? extends Trackor> trackorType, String value) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(trackorType.getName(), value);
        editor.apply();
    }

    public String getFilter(Class<? extends Trackor> trackorType) {
        return preferences.getString(trackorType.getName(), null);
    }

}
