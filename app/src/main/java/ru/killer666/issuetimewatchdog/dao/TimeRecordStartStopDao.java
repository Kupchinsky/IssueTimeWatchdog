package ru.killer666.issuetimewatchdog.dao;

import java.util.List;

import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStop;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStopType;

public interface TimeRecordStartStopDao extends RuntimeExceptionDao<TimeRecordStartStop, Integer> {

    int SHOW_LIMIT = 7;

    int createWithType(TimeRecord timeRecord, TimeRecordStartStopType type);

    TimeRecordStartStop queryLastStartForTimeRecord(TimeRecord timeRecord);

    List<TimeRecordStartStop> queryOfTimeRecordList(TimeRecord timeRecord);

}
