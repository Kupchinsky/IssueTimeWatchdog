package ru.killer666.issuetimewatchdog;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

class IssueDao extends RuntimeExceptionDao<Issue, Integer> {
    IssueDao(Dao<Issue, Integer> dao) {
        super(dao);
    }

    List<Issue> queryNotAutoRemove() {
        QueryBuilder<Issue, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where().eq("autoRemove", false);

            return this.query(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    Issue queryForTrackorKey(String trackorKey) {
        QueryBuilder<Issue, Integer> queryBuilder = this.queryBuilder();

        try {
            queryBuilder.where().eq("trackorKey", trackorKey);

            return this.queryForFirst(queryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
