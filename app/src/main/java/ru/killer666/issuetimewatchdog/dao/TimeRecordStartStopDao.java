package ru.killer666.issuetimewatchdog.dao;

import java.util.Date;
import java.util.List;

import ru.killer666.issuetimewatchdog.TimeRecordStartStop;
import ru.killer666.issuetimewatchdog.TimeRecordStartStopType;
import ru.killer666.issuetimewatchdog.model.TimeRecord;

public interface TimeRecordStartStopDao extends MyRuntimeExceptionDao<TimeRecordStartStop, Integer> {
    int createWithType(TimeRecord timeRecord, TimeRecordStartStopType type);
    List<TimeRecordStartStop> queryOfTimeRecordList(TimeRecord timeRecord);
    List<TimeRecordStartStop> queryForTimeRecordAndDate(TimeRecord timeRecord, Date date);
}
