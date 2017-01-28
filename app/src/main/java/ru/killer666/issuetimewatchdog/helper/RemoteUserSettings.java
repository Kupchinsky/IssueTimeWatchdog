package ru.killer666.issuetimewatchdog.helper;

import java.text.DateFormat;
import java.text.NumberFormat;

import rx.Observable;

public interface RemoteUserSettings {

    DateFormat getDateFormatter();

    DateFormat getTimeFormatter();

    NumberFormat getNumberFormatter();

    Observable<Void> requestRemoteUserSettings();

    // TODO: Use or remove
    boolean isRemoteSettingsLoaded();

}
