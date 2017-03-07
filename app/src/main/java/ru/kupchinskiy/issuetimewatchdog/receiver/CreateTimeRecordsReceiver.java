package ru.kupchinskiy.issuetimewatchdog.receiver;

import android.content.Context;
import android.content.Intent;

import com.google.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import roboguice.receiver.RoboBroadcastReceiver;
import ru.kupchinskiy.issuetimewatchdog.helper.TimeRecordHelper;

@Slf4j
public class CreateTimeRecordsReceiver extends RoboBroadcastReceiver {

    @Inject
    private TimeRecordHelper timeRecordHelper;

    @Override
    public void handleReceive(Context context, Intent intent) {
        log.debug("Alarm received");
        timeRecordHelper.startUploadAllNonInteractive();
    }

}