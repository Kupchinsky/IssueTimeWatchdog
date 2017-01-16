package ru.killer666.issuetimewatchdog.dao;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStop;

public class IssueDaoImpl extends RuntimeExceptionDao<Issue, Integer> implements IssueDao {

    static final int LOAD_LIMIT_DAYS = 7;

    @Inject
    private TimeRecordStartStopDao timeRecordStartStopDao;
    @Inject
    private TimeRecordDao timeRecordDao;

    public IssueDaoImpl(Dao<Issue, Integer> dao) {
        super(dao);
    }

    @Override
    public List<Issue> queryNotAutoRemove() {
        QueryBuilder<Issue, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where().eq("autoRemove", false);

            return this.query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Issue> queryWithLoadLimit() {
        Calendar calendar = Calendar.getInstance();
        QueryBuilder<Issue, Integer> queryBuilder = this.queryBuilder();

        try {
            calendar.add(Calendar.DATE, 1);
            Date highDate = calendar.getTime();

            calendar.set(Calendar.DAY_OF_MONTH, -(LOAD_LIMIT_DAYS + 1));
            Date lowDate = calendar.getTime();

            queryBuilder
                    .where()
                    .between("date", lowDate, highDate);
            queryBuilder.orderBy("date", false);

            return this.query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Issue queryForTrackorKey(String trackorKey) {
        QueryBuilder<Issue, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where().eq("trackorKey", trackorKey);

            return this.queryForFirst(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteWithAllChilds(Issue issue) {
        for (TimeRecord timeRecord : issue.getTimeRecordForeignCollection()) {
            for (TimeRecordStartStop timeRecordStartStop : timeRecord.getTimeRecordStartStopForeignCollection()) {
                this.timeRecordStartStopDao.delete(timeRecordStartStop);
            }
        }

        issue.getTimeRecordForeignCollection().clear();
        this.delete(issue);
    }

}
