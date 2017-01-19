package ru.killer666.issuetimewatchdog.helper;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.google.inject.Inject;

import roboguice.inject.ContextSingleton;

@ContextSingleton
public class DialogHelper {

    @Inject
    private Context context;

    public void error(String message) {
        show("Error", message);
    }

    public void warning(String message) {
        show("Warning", message);
    }

    public void info(String message) {
        show("Information", message);
    }

    public void show(String title, String message) {
        (new AlertDialog.Builder(context))
                .setTitle(title)
                .setMessage(message)
                .show();
    }

}
