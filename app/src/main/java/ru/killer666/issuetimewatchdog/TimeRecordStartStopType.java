package ru.killer666.issuetimewatchdog;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TimeRecordStartStopType {
    TypeStart("Started"),
    TypeStop("Stopped"),
    TypeStopForOtherTask("Stopped (other task working)"),
    TypeStopForDayEnd("Stopped (day ends)");

    private final String value;
}
