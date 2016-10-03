package ru.killer666.issuetimewatchdog;

import android.content.Intent;
import android.os.IBinder;

import roboguice.service.RoboService;

public class CreateTimeRecordsService extends RoboService {
    public CreateTimeRecordsService() {
    }

    // TODO: upload "unuploaded" (Не забывать проверять, если кто-нить часы перекрутил руками)
    // TODO: remove local issues+timerecords with autoremove field == true

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException();
    }
}
