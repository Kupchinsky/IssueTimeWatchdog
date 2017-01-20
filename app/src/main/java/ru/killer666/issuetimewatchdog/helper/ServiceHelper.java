package ru.killer666.issuetimewatchdog.helper;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;

import com.google.inject.Inject;

import roboguice.inject.ContextSingleton;

@ContextSingleton
public class ServiceHelper {

    @Inject
    private Context context;

    public boolean isRunning(Class<? extends Service> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
