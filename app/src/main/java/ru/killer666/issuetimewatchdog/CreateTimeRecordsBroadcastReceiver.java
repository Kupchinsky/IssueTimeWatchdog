package ru.killer666.issuetimewatchdog;

import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;

import roboguice.receiver.RoboBroadcastReceiver;
import ru.killer666.issuetimewatchdog.services.UploadTimeRecordsService;

public class CreateTimeRecordsBroadcastReceiver extends RoboBroadcastReceiver {

    private static Logger logger;

    @Override
    public void handleReceive(Context context, Intent intent) {

        logger.debug("Alarm received");
        context.startService(new Intent(context, UploadTimeRecordsService.class).setAction(UploadTimeRecordsService.ACTION_UPDATE_ALL));

    }

}