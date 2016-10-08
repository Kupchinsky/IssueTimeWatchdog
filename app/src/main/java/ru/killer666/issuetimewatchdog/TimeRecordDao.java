package ru.killer666.issuetimewatchdog;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

class TimeRecordDao extends RuntimeExceptionDao<TimeRecord, Integer> {
    static final int SHOW_LIMIT = 7;

    TimeRecordDao(Dao<TimeRecord, Integer> dao) {
        super(dao);
    }

    List<TimeRecord> queryNotUploadedOfIssue(Issue issue) {
        QueryBuilder<TimeRecord, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where()
                    .eq("issue_id", issue)
                    .and()
                    .not()
                    .raw("workedTime = wroteTime");
            queryBuilder.orderBy("date", false);

            return this.query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    List<TimeRecord> queryLastOfIssueList(Issue issue) {
        QueryBuilder<TimeRecord, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where().eq("issue_id", issue);
            queryBuilder.orderBy("date", false);
            queryBuilder.limit(SHOW_LIMIT);

            return this.query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    TimeRecord queryForIssueAndDate(Issue issue, Date date) {
        QueryBuilder<TimeRecord, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where()
                    .eq("issue_id", issue)
                    .and()
                    .eq("date", date);

            return this.queryForFirst(queryBuilder.prepare());
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

    List<TimeRecord> queryOldOfIssue(Issue issue) {
        Calendar calendar = Calendar.getInstance();
        QueryBuilder<TimeRecord, Integer> queryBuilder = this.queryBuilder();

        try {
            calendar.add(Calendar.DATE, -(SHOW_LIMIT + 1));

            queryBuilder.where()
                    .eq("issue_id", issue)
                    .and()
                    .lt("date", calendar.getTime());

            return this.query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
