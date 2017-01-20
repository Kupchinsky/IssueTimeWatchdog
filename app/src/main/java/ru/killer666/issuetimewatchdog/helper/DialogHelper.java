package ru.killer666.issuetimewatchdog.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.google.inject.Inject;

import roboguice.inject.ContextSingleton;

@ContextSingleton
public class DialogHelper {

    @Inject
    private Context context;

    private ProgressDialog progressDialog;

    public void error(String message) {
        show("Error", message);
    }

    public void warning(String message) {
        show("Warning", message);
    }

    public void info(String message) {
        show("Information", message);
    }

    public void warnNotImplemented() {
        warning("Not implemented now!");
    }

    public void show(String title, String message) {
        (new AlertDialog.Builder(context))
                .setTitle(title)
                .setMessage(message)
                .show();
    }

    public void showProgressDialog() {
        dismissProgressDialog();

        progressDialog = ProgressDialog.show(context, "Please wait", "Loading please wait..", true);
        progressDialog.setCancelable(false);
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }

            progressDialog = null;
        }
    }

    public boolean isProgressDialogShowing() {
        return progressDialog != null && progressDialog.isShowing();
    }

}
