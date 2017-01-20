package ru.killer666.issuetimewatchdog.helper;

import java.text.DateFormat;
import java.text.NumberFormat;

import rx.Observable;

public interface ConfigFieldFormatter {

    DateFormat getDateFormatter();

    DateFormat getTimeFormatter();

    NumberFormat getNumberFormatter();

    // TODO: use
    Observable requestUserSettings();

}
