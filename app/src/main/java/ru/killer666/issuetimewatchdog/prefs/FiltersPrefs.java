package ru.killer666.issuetimewatchdog.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ru.killer666.issuetimewatchdog.model.TrackorType;

@Singleton
public class FiltersPrefs {

    private final SharedPreferences preferences;

    @Inject
    private FiltersPrefs(android.app.Application application) {
        this.preferences = application.getSharedPreferences("filters_prefs", Context.MODE_PRIVATE);
    }

    public void setFilter(Class<? extends TrackorType> trackorType, String value) {
        SharedPreferences.Editor editor = this.preferences.edit();

        editor.putString(trackorType.getName(), value);
        editor.apply();
    }

    public String getFilter(Class<? extends TrackorType> trackorType) {
        return this.preferences.getString(trackorType.getName(), null);
    }

}
