package ru.killer666.issuetimewatchdog.dao;

import java.util.Date;
import java.util.List;

import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStop;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStopType;

public interface TimeRecordStartStopDao extends RuntimeExceptionDao<TimeRecordStartStop, Integer> {

    int createWithType(TimeRecord timeRecord, TimeRecordStartStopType type);

    TimeRecordStartStop queryLastStartForTimeRecord(TimeRecord timeRecord);

    // TODO: use or remove
    List<TimeRecordStartStop> queryOfTimeRecordList(TimeRecord timeRecord);

    // TODO: use or remove
    List<TimeRecordStartStop> queryForTimeRecordAndDate(TimeRecord timeRecord, Date date);

}
