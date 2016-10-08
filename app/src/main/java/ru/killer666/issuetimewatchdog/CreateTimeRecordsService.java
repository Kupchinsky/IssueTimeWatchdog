package ru.killer666.issuetimewatchdog;

import android.content.Intent;

import roboguice.service.RoboIntentService;

public class CreateTimeRecordsService extends RoboIntentService {
    public CreateTimeRecordsService() {
        super(CreateTimeRecordsService.class.getSimpleName());
    }

    // TODO: upload "unuploaded" (Не забывать проверять, если кто-нить часы перекрутил руками), т.е. сначала Read, а потом CreateOrUpdate
    // TODO: remove local issues+timerecords with autoremove field == true
    // TODO: remove timerecords older than {#TimeRecordDao.SHOW_LIMIT}
    // TODO: show notify (with vibration and ringtone) if time records not writed to 11:00AM in next day, так-же напоминать периодически, чтобы не попасть на еду

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
