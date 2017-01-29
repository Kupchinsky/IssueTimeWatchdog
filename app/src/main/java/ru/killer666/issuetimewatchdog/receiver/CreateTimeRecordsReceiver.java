package ru.killer666.issuetimewatchdog.receiver;

import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import org.slf4j.Logger;

import roboguice.receiver.RoboBroadcastReceiver;
import ru.killer666.issuetimewatchdog.helper.TimeRecordHelper;

public class CreateTimeRecordsReceiver extends RoboBroadcastReceiver {

    private static Logger logger;

    @Inject
    private TimeRecordHelper timeRecordHelper;

    @Override
    public void handleReceive(Context context, Intent intent) {
        logger.debug("Alarm received");
        timeRecordHelper.startUploadAllNonInteractive();
    }

}