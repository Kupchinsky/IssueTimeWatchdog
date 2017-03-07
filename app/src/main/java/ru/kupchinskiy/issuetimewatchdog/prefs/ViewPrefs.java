package ru.kupchinskiy.issuetimewatchdog.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ru.kupchinskiy.issuetimewatchdog.model.Trackor;

@Singleton
public class ViewPrefs {

    private final SharedPreferences preferences;

    @Inject
    private ViewPrefs(android.app.Application application) {
        preferences = application.getSharedPreferences("view_prefs", Context.MODE_PRIVATE);
    }

    public void setView(Class<? extends Trackor> trackorType, String value) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(trackorType.getName(), value);
        editor.apply();
    }

    public String getView(Class<? extends Trackor> trackorType) {
        return preferences.getString(trackorType.getName(), null);
    }

}
