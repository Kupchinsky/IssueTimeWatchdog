package ru.killer666.issuetimewatchdog.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TimeRecordLogType {

    TypeWorking("Working"),
    TypeIdle("Stop working"),
    TypeIdleForOtherTask("Stop working (other task raised)"),
    TypeIdleByKillApp("Stop working (no service, will be restarted)"),
    TypeIdleForDayEnd("Stop working (day ends)"),
    TypeHandDecreaseTime("Hand decrease time"),
    TypeHandIncreaseTime("Hand increase time"),
    // TODO: use this
    TypeRemoteCreate("Remote trackor created"),
    TypeRemoteUpdate("Remote trackor updated");

    private final String value;

    public static boolean isIdle(TimeRecordLogType timeRecordLogType) {
        return TypeIdle.equals(timeRecordLogType) ||
                TypeIdleForOtherTask.equals(timeRecordLogType) ||
                TypeIdleByKillApp.equals(timeRecordLogType) ||
                TypeIdleForDayEnd.equals(timeRecordLogType);
    }

    public static boolean isHandModify(TimeRecordLogType timeRecordLogType) {
        return TypeHandDecreaseTime.equals(timeRecordLogType) ||
                TypeHandIncreaseTime.equals(timeRecordLogType);
    }

}
