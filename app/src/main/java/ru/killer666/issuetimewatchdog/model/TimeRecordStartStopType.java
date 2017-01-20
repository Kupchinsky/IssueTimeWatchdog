package ru.killer666.issuetimewatchdog.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TimeRecordStartStopType {

    TypeWorking("Working"),
    TypeIdle("Stop working"),
    TypeIdleForOtherTask("Stop working (other task raised)"),
    TypeIdleByKillApp("Stop working (no service, will be restarted)"),
    TypeIdleForDayEnd("Stop working (day ends)");

    private final String value;

    public static boolean isIdle(TimeRecordStartStopType timeRecordStartStopType) {
        return TypeIdle.equals(timeRecordStartStopType) ||
                TypeIdleForOtherTask.equals(timeRecordStartStopType) ||
                TypeIdleByKillApp.equals(timeRecordStartStopType) ||
                TypeIdleForDayEnd.equals(timeRecordStartStopType);
    }

}
