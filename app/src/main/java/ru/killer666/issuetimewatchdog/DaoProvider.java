package ru.killer666.issuetimewatchdog;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class DaoProvider {
    static class IssueProvider implements Provider<IssueDao> {
        @Inject
        private DatabaseHelper databaseHelper;

        @Override
        public IssueDao get() {
            IssueDao result = this.databaseHelper.getRuntimeExceptionDao(Issue.class);
            result.setObjectCache(true);
            return result;
        }
    }

    static class TimeRecordProvider implements Provider<TimeRecordDao> {
        @Inject
        private DatabaseHelper databaseHelper;

        @Override
        public TimeRecordDao get() {
            TimeRecordDao result = this.databaseHelper.getRuntimeExceptionDao(TimeRecord.class);
            result.setObjectCache(true);
            return result;
        }
    }
}
