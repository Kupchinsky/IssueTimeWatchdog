package ru.killer666.issuetimewatchdog;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

class TimeRecordDao extends RuntimeExceptionDao<TimeRecord, Integer> {
    static final int SHOW_LIMIT = 7;

    TimeRecordDao(Dao<TimeRecord, Integer> dao) {
        super(dao);
    }

    List<TimeRecord> queryLastOfIssueList(Issue issue) {
        QueryBuilder<TimeRecord, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where().eq("issue_id", issue);
            queryBuilder.orderBy("date", false);
            queryBuilder.limit(Long.valueOf(SHOW_LIMIT));

            return this.query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    TimeRecord queryLastOfIssue(Issue issue) {
        QueryBuilder<TimeRecord, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where().eq("issue_id", issue);
            queryBuilder.orderBy("date", false);

            return this.queryForFirst(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    TimeRecord queryForTrackorKey(String trackorKey) {
        QueryBuilder<TimeRecord, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where().eq("trackorKey", trackorKey);

            return this.queryForFirst(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
