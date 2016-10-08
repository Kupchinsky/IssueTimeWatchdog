package ru.killer666.issuetimewatchdog;

import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;

import roboguice.receiver.RoboBroadcastReceiver;

public class CreateTimeRecordsBroadcastReceiver extends RoboBroadcastReceiver {
    private static Logger logger;

    @Override
    public void handleReceive(Context context, Intent intent) {
        logger.debug("Alarm received");
        context.startService(new Intent(context, CreateTimeRecordsService.class).setAction(CreateTimeRecordsService.ACTION_UPDATE_ALL));
    }
}