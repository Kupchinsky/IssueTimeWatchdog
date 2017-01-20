package ru.killer666.issuetimewatchdog.dao;

import java.util.List;

import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordLog;
import ru.killer666.issuetimewatchdog.model.TimeRecordLogType;

public interface TimeRecordLogDao extends RuntimeExceptionDao<TimeRecordLog, Integer> {

    int SHOW_LIMIT = 7;

    int createWithType(TimeRecord timeRecord, TimeRecordLogType type);

    TimeRecordLog queryLastStartForTimeRecord(TimeRecord timeRecord);

    List<TimeRecordLog> queryOfTimeRecordList(TimeRecord timeRecord);

}
