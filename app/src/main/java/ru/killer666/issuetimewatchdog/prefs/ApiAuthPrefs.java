package ru.killer666.issuetimewatchdog.prefs;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.Getter;
import okhttp3.Credentials;

@Singleton
public class ApiAuthPrefs {

    private static final String PREFS_LOGIN = "login";
    private static final String PREFS_PASSWORD = "password";

    private final SharedPreferences preferences;

    @Getter
    private String credentials;

    @Inject
    private ApiAuthPrefs(Application application) {
        preferences = application.getSharedPreferences("apiauth_prefs", Context.MODE_PRIVATE);
        updateCredentials();
    }

    private void updateCredentials() {
        String login = getLogin();
        String password = getPassword();

        if (login == null || password == null) {
            credentials = null;
            return;
        }

        credentials = Credentials.basic(login, password);
    }

    public void setCredentials(String login, String password) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(PREFS_LOGIN, login);
        editor.putString(PREFS_PASSWORD, password);
        editor.apply();

        updateCredentials();
    }

    public String getLogin() {
        return preferences.getString(PREFS_LOGIN, null);
    }

    private String getPassword() {
        return preferences.getString(PREFS_PASSWORD, null);
    }

    public boolean isValid() {
        return credentials != null;
    }

}
