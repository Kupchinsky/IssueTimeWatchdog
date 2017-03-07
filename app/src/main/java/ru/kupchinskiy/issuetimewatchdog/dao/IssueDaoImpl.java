package ru.kupchinskiy.issuetimewatchdog.dao;

import com.google.inject.Inject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.model.IssueState;

public class IssueDaoImpl extends RuntimeExceptionDao<Issue, Integer> implements IssueDao {

    @Inject
    private TimeRecordLogDao timeRecordLogDao;

    @Inject
    private TimeRecordDao timeRecordDao;

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

}
