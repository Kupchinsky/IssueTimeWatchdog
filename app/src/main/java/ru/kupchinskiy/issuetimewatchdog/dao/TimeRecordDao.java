package ru.kupchinskiy.issuetimewatchdog.dao;

import android.util.Pair;

import java.util.Date;
import java.util.List;

import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecord;

public interface TimeRecordDao extends RuntimeExceptionDao<TimeRecord, Integer> {

    int SHOW_LIMIT = 7;

    List<TimeRecord> queryOfIssue(Issue issue);

    List<TimeRecord> queryLastOfIssueList(Issue issue);

    TimeRecord queryForIssueAndDate(Issue issue, Date date);

    TimeRecord queryLastOfIssue(Issue issue);

    Pair<Long, Long> queryForMaxMinDateOfIssue(Issue issue);

    List<TimeRecord> queryOldOfIssue(Issue issue);

}
