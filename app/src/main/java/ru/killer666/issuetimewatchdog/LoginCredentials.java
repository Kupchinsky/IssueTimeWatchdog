package ru.killer666.issuetimewatchdog;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.Getter;
import okhttp3.Credentials;

@Singleton
public class LoginCredentials {
    private static final String PREFS_LOGIN = "login";
    private static final String PREFS_PASSWORD = "password";

    private final SharedPreferences preferences;

    @Getter
    private String credentials;

    @Inject
    private LoginCredentials(android.app.Application application) {
        this.preferences = application.getSharedPreferences("login_credentials", Context.MODE_PRIVATE);

        this.updateCredentials();
    }

    private void updateCredentials() {
        String login = this.getLogin();
        String password = this.getPassword();

        if (login == null || password == null) {
            this.credentials = null;
            return;
        }

        this.credentials = Credentials.basic(login, password);
    }

    void setCredentials(String login, String password) {
        SharedPreferences.Editor editor = this.preferences.edit();

        editor.putString(PREFS_LOGIN, login);
        editor.putString(PREFS_PASSWORD, password);
        editor.apply();

        this.updateCredentials();
    }

    String getLogin() {
        return this.preferences.getString(PREFS_LOGIN, null);
    }

    private String getPassword() {
        return this.preferences.getString(PREFS_PASSWORD, null);
    }

    boolean isValid() {
        return this.credentials != null;
    }
}
