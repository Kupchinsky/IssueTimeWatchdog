package ru.kupchinskiy.issuetimewatchdog.dao;

import java.util.List;

import ru.kupchinskiy.issuetimewatchdog.model.TimeRecord;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecordLog;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecordLogType;

public interface TimeRecordLogDao extends RuntimeExceptionDao<TimeRecordLog, Integer> {

    int SHOW_LIMIT = 7;

    int createWithType(TimeRecord timeRecord, TimeRecordLogType type);

    TimeRecordLog queryLastStartForTimeRecord(TimeRecord timeRecord);

    List<TimeRecordLog> queryOfTimeRecordList(TimeRecord timeRecord);

}
