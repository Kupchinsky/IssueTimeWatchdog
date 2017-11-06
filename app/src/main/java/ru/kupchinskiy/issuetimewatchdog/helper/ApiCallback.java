package ru.kupchinskiy.issuetimewatchdog.helper;

import android.content.Context;

import com.google.inject.Inject;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import roboguice.RoboGuice;

public abstract class ApiCallback<T> implements Callback<T> {

    @Inject
    private DialogHelper dialogHelper;

    private final Context context;

    public ApiCallback(Context context) {
        this.context = context;
        RoboGuice.injectMembers(context, this);
    }

    @Override
    public final void onResponse(Call<T> call, Response<T> response) {
        try {
            onComplete();
        } catch (Throwable t) {
            t.printStackTrace();
            showErrorDialog("Complete handler error: " + t.getMessage(), null);
        }

        if (HttpURLConnection.HTTP_OK == response.code()) {
            try {
                onSuccess(response);
            } catch (Throwable t) {
                t.printStackTrace();
                showErrorDialog("Success handler error: " + t.getMessage(), null);
            }
        } else {
            showErrorDialog(response.code() + " " + response.message(), response);
        }

        try {
            onComplete2();
        } catch (Throwable t) {
            t.printStackTrace();
            showErrorDialog("Complete2 handler error: " + t.getMessage(), null);
        }
    }

    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        Throwable t2 = null;
        Throwable t3 = null;

        try {
            onComplete();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            t2 = throwable;
        }

        try {
            onError();
        } catch (Throwable throwable2) {
            throwable2.printStackTrace();
            t3 = throwable2;
        }

        String message = t.getMessage();
        if (t2 != null) {
            message = "Complete handler error: " + t2.getMessage() + "\n\n" + message;
        }
        if (t3 != null) {
            message = "Error handler error: " + t3.getMessage() + "\n\n" + message;
        }

        showErrorDialog(message, null);
    }

    private void showErrorDialog(String message, Response<T> response) {
        String errorMessage = message;
        if (response != null && !response.isSuccessful()) {
            try {
                errorMessage += "\n\n" + response.errorBody().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        dialogHelper.error("Request failed:\n\n" + errorMessage);
    }

    public void onError() {
    }

    public void onComplete() {
    }

    public void onComplete2() {
    }

    public abstract void onSuccess(Response<T> response);

}
