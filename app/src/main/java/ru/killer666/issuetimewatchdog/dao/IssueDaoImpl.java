package ru.killer666.issuetimewatchdog.dao;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.IssueState;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordStartStop;

public class IssueDaoImpl extends RuntimeExceptionDao<Issue, Integer> implements IssueDao {

    @Inject
    private TimeRecordStartStopDao timeRecordStartStopDao;

    public IssueDaoImpl(Dao<Issue, Integer> dao) {
        super(dao);
    }

    @Override
    public List<Issue> queryNotAutoRemove() {
        QueryBuilder<Issue, Integer> queryBuilder = queryBuilder();

        try {
            queryBuilder.where().eq("removeAfterUpload", false);

            return query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Issue queryWorkingState() {
        QueryBuilder<Issue, Integer> queryBuilder = queryBuilder();

        try {
            queryBuilder.where().eq("state", IssueState.Working);

            return queryForFirst(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean trackorKeyExists(String trackorKey) {
        try {
            QueryBuilder<Issue, Integer> queryBuilder = queryBuilder();
            queryBuilder.selectRaw("COUNT(*)").where().eq("trackorKey", trackorKey);
            String query = queryBuilder.prepareStatementString();

            DatabaseConnection connection = getConnectionSource().getReadOnlyConnection();
            return connection.queryForLong(query) != 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteWithAllChilds(Issue issue) {
        for (TimeRecord timeRecord : issue.getTimeRecordForeignCollection()) {
            for (TimeRecordStartStop timeRecordStartStop : timeRecord.getTimeRecordStartStopForeignCollection()) {
                timeRecordStartStopDao.delete(timeRecordStartStop);
            }
        }

        issue.getTimeRecordForeignCollection().clear();
        delete(issue);
    }

}
