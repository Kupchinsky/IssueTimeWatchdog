package ru.killer666.issuetimewatchdog.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import com.google.inject.Inject;

import roboguice.inject.ContextSingleton;
import rx.Observable;

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
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
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

    public Observable<Double> showInputNumberDialog(String title) {
        return Observable.defer(() -> Observable.create(subscriber -> {
            EditText editText = new EditText(context);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setRawInputType(Configuration.KEYBOARD_12KEY);

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                    .setTitle(title)
                    .setView(editText)
                    .setPositiveButton("OK", (dialog, whichButton) -> {
                        if (editText.getText() != null) {
                            dialog.dismiss();
                            subscriber.onNext(Double.valueOf(editText.getText().toString()));
                            subscriber.onCompleted();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, whichButton) -> {
                        dialog.cancel();
                        subscriber.onCompleted();
                    });
            alertDialog.show();
        }));
    }

}
