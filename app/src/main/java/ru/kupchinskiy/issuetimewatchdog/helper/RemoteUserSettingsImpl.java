package ru.kupchinskiy.issuetimewatchdog.helper;

import android.content.Context;

import com.google.inject.Inject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Response;
import roboguice.inject.ContextSingleton;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3UserSettingsResponse;
import rx.Observable;

@ContextSingleton
public class RemoteUserSettingsImpl implements RemoteUserSettings {

    @Inject
    private ApiClient apiClient;

    @Inject
    private Context context;

    @Getter
    private DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);

    @Getter
    private DateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss aa", Locale.ENGLISH);

    @Getter
    private NumberFormat numberFormatter = new DecimalFormat("#.##");

    @Override
    public Observable<Void> requestRemoteUserSettings() {
        return Observable.defer(() -> Observable.create(subscriber -> {
            Call<V3UserSettingsResponse> call = apiClient.v3UserSettings();
            call.enqueue(new ApiCallback<V3UserSettingsResponse>(context) {
                @Override
                public void onComplete() {
                    subscriber.onCompleted();
                }

                @Override
                public void onSuccess(Response<V3UserSettingsResponse> response) {
                    V3UserSettingsResponse userSettings = response.body();
                    dateFormatter = new SimpleDateFormat(userSettings.getDateFormat(), Locale.ENGLISH);
                    timeFormatter = new SimpleDateFormat(userSettings.getTimeFormat(), Locale.ENGLISH);
                }
            });
        }));
    }

}
