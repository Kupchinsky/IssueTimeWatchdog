package ru.killer666.issuetimewatchdog;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.google.inject.Inject;

import org.slf4j.Logger;

import roboguice.RoboGuice;
import roboguice.receiver.RoboBroadcastReceiver;

public class CreateTimeRecordsBroadcastReceiver extends RoboBroadcastReceiver {
    private static Logger logger;

    @Inject
    private TrackorApiService trackorApiService;

    @Inject
    private PowerManager powerManager;

    @Override
    public void handleReceive(Context context, Intent intent) {
        RoboGuice.injectMembers(context, this);

        logger.debug("Alarm received");

        PowerManager.WakeLock wl = this.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CreateTimeRecords");
        wl.acquire();

        logger.debug("Wakelock acquired");

        // TODO

        wl.release();

        logger.debug("Wakelock released");
    }
}