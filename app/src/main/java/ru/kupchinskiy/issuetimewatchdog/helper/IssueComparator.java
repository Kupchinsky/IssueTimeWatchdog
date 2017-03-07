package ru.kupchinskiy.issuetimewatchdog.helper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ru.kupchinskiy.issuetimewatchdog.dao.TimeRecordDao;
import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecord;

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
