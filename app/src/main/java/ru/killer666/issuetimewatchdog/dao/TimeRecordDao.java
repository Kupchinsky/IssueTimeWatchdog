package ru.killer666.issuetimewatchdog.dao;

import java.util.Date;
import java.util.List;

import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.TimeRecord;

public interface TimeRecordDao extends RuntimeExceptionDao<TimeRecord, Integer> {

    int SHOW_LIMIT = 7;

    List<TimeRecord> queryNotUploadedOfIssue(Issue issue);

    List<TimeRecord> queryLastOfIssueList(Issue issue);

    TimeRecord queryForIssueAndDate(Issue issue, Date date);

    TimeRecord queryLastOfIssue(Issue issue);

    TimeRecord queryForTrackorKey(String trackorKey);

    List<TimeRecord> queryOldOfIssue(Issue issue);

}
