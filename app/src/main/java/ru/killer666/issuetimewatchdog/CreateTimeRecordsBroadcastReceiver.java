package ru.killer666.issuetimewatchdog;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.google.inject.Inject;

import roboguice.receiver.RoboBroadcastReceiver;

public class CreateTimeRecordsBroadcastReceiver extends RoboBroadcastReceiver {
    @Inject
    private TrackorApiService trackorApiService;

    @Override
    public void handleReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CreateTimeRecords");
        wl.acquire();

        // TODO

        wl.release();
    }
}