package ru.killer666.issuetimewatchdog;

import com.google.inject.Injector;

import roboguice.RoboGuice;
import ru.killer666.issuetimewatchdog.helper.IssueAlarms;
import ru.killer666.issuetimewatchdog.prefs.CreateTimeRecordsPrefs;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RoboGuice.setUseAnnotationDatabases(false);
        Injector injector = RoboGuice.getOrCreateBaseApplicationInjector(this);

        // Auto initialization
        injector.getInstance(CreateTimeRecordsPrefs.class);
        injector.getInstance(IssueAlarms.class);
    }

}
