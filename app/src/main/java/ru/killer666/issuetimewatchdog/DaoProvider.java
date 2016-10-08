package ru.killer666.issuetimewatchdog;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import roboguice.RoboGuice;

public class DaoProvider {
    static class IssueProvider implements Provider<IssueDao> {
        @Inject
        private DatabaseHelper databaseHelper;
        @Inject
        private Application application;

        @Override
        public IssueDao get() {
            try {
                Dao<Issue, Integer> dao = this.databaseHelper.getDao(Issue.class);
                dao.setObjectCache(true);

                IssueDao result = new IssueDao(dao);
                RoboGuice.getOrCreateBaseApplicationInjector(this.application).injectMembers(result);

                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class TimeRecordProvider implements Provider<TimeRecordDao> {
        @Inject
        private DatabaseHelper databaseHelper;
        @Inject
        private Application application;

        @Override
        public TimeRecordDao get() {
            try {
                Dao<TimeRecord, Integer> dao = this.databaseHelper.getDao(TimeRecord.class);
                dao.setObjectCache(true);

                TimeRecordDao result = new TimeRecordDao(dao);
                RoboGuice.getOrCreateBaseApplicationInjector(this.application).injectMembers(result);

                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class TimeRecordStartStopProvider implements Provider<TimeRecordStartStopDao> {
        @Inject
        private DatabaseHelper databaseHelper;
        @Inject
        private Application application;

        @Override
        public TimeRecordStartStopDao get() {
            try {
                Dao<TimeRecordStartStop, Integer> dao = this.databaseHelper.getDao(TimeRecordStartStop.class);
                dao.setObjectCache(true);

                TimeRecordStartStopDao result = new TimeRecordStartStopDao(dao);
                RoboGuice.getOrCreateBaseApplicationInjector(this.application).injectMembers(result);

                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
