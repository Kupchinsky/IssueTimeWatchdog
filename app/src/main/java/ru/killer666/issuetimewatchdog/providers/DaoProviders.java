package ru.killer666.issuetimewatchdog.providers;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import ru.killer666.issuetimewatchdog.helper.DatabaseHelper;
import ru.killer666.issuetimewatchdog.dao.IssueDaoImpl;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDaoImpl;
import ru.killer666.issuetimewatchdog.dao.TimeRecordLogDaoImpl;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.TimeRecordLog;

public class DaoProviders {

    public static class IssueProvider implements Provider<IssueDaoImpl> {

        @Inject
        private DatabaseHelper databaseHelper;
        @Inject
        private Injector injector;

        @Override
        public IssueDaoImpl get() {
            try {
                Dao<Issue, Integer> dao = databaseHelper.getDao(Issue.class);
                dao.setObjectCache(true);

                IssueDaoImpl result = new IssueDaoImpl(dao);
                injector.injectMembers(result);
                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static class TimeRecordProvider implements Provider<TimeRecordDaoImpl> {

        @Inject
        private DatabaseHelper databaseHelper;

        @Override
        public TimeRecordDaoImpl get() {
            try {
                Dao<TimeRecord, Integer> dao = databaseHelper.getDao(TimeRecord.class);
                dao.setObjectCache(true);

                return new TimeRecordDaoImpl(dao);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static class TimeRecordStartStopProvider implements Provider<TimeRecordLogDaoImpl> {

        @Inject
        private DatabaseHelper databaseHelper;

        @Override
        public TimeRecordLogDaoImpl get() {
            try {
                Dao<TimeRecordLog, Integer> dao = databaseHelper.getDao(TimeRecordLog.class);
                dao.setObjectCache(true);

                return new TimeRecordLogDaoImpl(dao);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
