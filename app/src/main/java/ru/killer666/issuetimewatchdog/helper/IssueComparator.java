package ru.killer666.issuetimewatchdog.helper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.TimeRecord;

@Singleton
public class IssueComparator implements java.util.Comparator<Issue> {

    @Inject
    private TimeRecordDao timeRecordDao;

    @Override
    public int compare(Issue lhs, Issue rhs) {
        if (lhs.isAutoRemove()) {
            return 1;
        } else if (rhs.isAutoRemove()) {
            return -1;
        }

        TimeRecord timeRecordLhs = this.timeRecordDao.queryLastOfIssue(lhs);
        TimeRecord timeRecordRhs = this.timeRecordDao.queryLastOfIssue(rhs);

        return timeRecordLhs == null ? 1 : (timeRecordRhs == null ? -1 : (timeRecordLhs.compareTo(timeRecordRhs)));
    }

}
