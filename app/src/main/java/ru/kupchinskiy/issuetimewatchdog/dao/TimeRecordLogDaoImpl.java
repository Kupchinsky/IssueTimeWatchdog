package ru.kupchinskiy.issuetimewatchdog.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import ru.kupchinskiy.issuetimewatchdog.model.TimeRecord;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecordLog;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecordLogType;

public class TimeRecordLogDaoImpl extends RuntimeExceptionDao<TimeRecordLog, Integer>
        implements TimeRecordLogDao {

    public TimeRecordLogDaoImpl(Dao<TimeRecordLog, Integer> dao) {
        super(dao);
    }

    @Override
    public int createWithType(TimeRecord timeRecord, TimeRecordLogType type) {
        TimeRecordLog timeRecordLog = new TimeRecordLog();

        timeRecordLog.setTimeRecord(timeRecord);
        timeRecordLog.setType(type);
        timeRecordLog.setDate(Calendar.getInstance().getTime());

        return create(timeRecordLog);
    }

    @Override
    public TimeRecordLog queryLastStartForTimeRecord(TimeRecord timeRecord) {
        QueryBuilder<TimeRecordLog, Integer> queryBuilder = queryBuilder();

        try {
            queryBuilder.where().eq("timeRecord_id", timeRecord).and().eq("type", TimeRecordLogType.TypeWorking);

            return queryForFirst(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TimeRecordLog> queryOfTimeRecordList(TimeRecord timeRecord) {
        QueryBuilder<TimeRecordLog, Integer> queryBuilder = queryBuilder();

        try {
            queryBuilder.where().eq("timeRecord_id", timeRecord);
            queryBuilder.orderBy("date", true);

            return query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
