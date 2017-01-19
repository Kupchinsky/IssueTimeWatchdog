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
        TimeRecord timeRecordLhs = timeRecordDao.queryLastOfIssue(lhs);
        TimeRecord timeRecordRhs = timeRecordDao.queryLastOfIssue(rhs);

        return timeRecordLhs == null ? 1 : (timeRecordRhs == null ? -1 : (timeRecordLhs.compareTo(timeRecordRhs)));
    }

}
