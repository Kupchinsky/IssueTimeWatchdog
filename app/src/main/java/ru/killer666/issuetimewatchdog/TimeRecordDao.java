package ru.killer666.issuetimewatchdog;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

public class TimeRecordDao extends RuntimeExceptionDao<TimeRecord, Integer> {
    public TimeRecordDao(Dao<TimeRecord, Integer> dao) {
        super(dao);
    }

    List<TimeRecord> getLastOfIssueList(Issue issue) {
        QueryBuilder<TimeRecord, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where().eq("issue", issue);
            queryBuilder.orderBy("date", false);
            queryBuilder.limit(7L);

            return this.query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    TimeRecord getLastOfIssue(Issue issue) {
        QueryBuilder<TimeRecord, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where().eq("issue", issue);
            queryBuilder.orderBy("date", false);

            return this.queryForFirst(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
