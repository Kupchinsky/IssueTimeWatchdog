package ru.killer666.issuetimewatchdog;

import com.google.inject.Injector;

import roboguice.RoboGuice;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RoboGuice.setUseAnnotationDatabases(false);
        Injector injector = RoboGuice.getOrCreateBaseApplicationInjector(this);

        // Do nothing, auto initialize
        injector.getInstance(CreateTimeRecordsSettings.class);
    }

}
