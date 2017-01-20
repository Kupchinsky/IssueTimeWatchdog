package ru.killer666.issuetimewatchdog.helper;

import com.google.inject.Inject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import lombok.Getter;
import roboguice.inject.ContextSingleton;
import ru.killer666.issuetimewatchdog.services.ApiClient;
import rx.Observable;

@ContextSingleton
public class ConfigFieldFormatterImpl implements ConfigFieldFormatter {

    @Inject
    private ApiClient apiClient;

    @Inject
    private DialogHelper dialogHelper;

    @Getter
    private DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);

    @Getter
    private DateFormat timeFormatter = new SimpleDateFormat("hh:mm:ss aa", Locale.ENGLISH);

    @Getter
    private NumberFormat numberFormatter = new DecimalFormat("#.##");

    @Override
    public Observable requestUserSettings() {
        return Observable.defer(() -> Observable.create(subscriber -> {
            dialogHelper.showProgressDialog();
            dialogHelper.dismissProgressDialog();
            // TODO: implement(required v3 api for read user settings)

            dialogHelper.warnNotImplemented();
            subscriber.onCompleted();
        }));
    }

}
