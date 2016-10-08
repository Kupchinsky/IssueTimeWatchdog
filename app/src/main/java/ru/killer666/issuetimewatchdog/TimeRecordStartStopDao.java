package ru.killer666.issuetimewatchdog;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

class TimeRecordStartStopDao extends RuntimeExceptionDao<TimeRecordStartStop, Integer> {
    TimeRecordStartStopDao(Dao<TimeRecordStartStop, Integer> dao) {
        super(dao);
    }

    int createWithType(TimeRecord timeRecord, TimeRecordStartStopType type) {
        TimeRecordStartStop timeRecordStartStop = new TimeRecordStartStop();

        timeRecordStartStop.setTimeRecord(timeRecord);
        timeRecordStartStop.setType(type);
        timeRecordStartStop.setDate(Calendar.getInstance().getTime());

        return this.create(timeRecordStartStop);
    }

    List<TimeRecordStartStop> queryOfTimeRecordList(TimeRecord timeRecord) {
        QueryBuilder<TimeRecordStartStop, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where().eq("timeRecord_id", timeRecord);
            queryBuilder.orderBy("date", false);

            return this.query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    List<TimeRecordStartStop> queryForTimeRecordAndDate(TimeRecord timeRecord, Date date) {
        QueryBuilder<TimeRecordStartStop, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where()
                    .eq("timeRecord_id", timeRecord)
                    .and()
                    .eq("date", date);

            return this.query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
