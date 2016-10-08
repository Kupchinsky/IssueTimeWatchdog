package ru.killer666.issuetimewatchdog;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@DatabaseTable
public class Issue implements TrackorType {
    @Getter
    static final String[] statuses = {"Opened",
            "In Progress",
            "Ready for Test",
            "Testing In Progress",
            "Tested",
            "Closed",
            "Awaiting Response",
            "Deferred",
            "On Track",
            "Reopened"};

    @DatabaseField(generatedId = true)
    private int id;

    @TrackorField(value = TrackorType.KEY, humanName = "Issue ID")
    @DatabaseField(canBeNull = false, index = true)
    private String trackorKey;

    @TrackorField(value = "VQS_IT_XITOR_NAME", humanName = "Summary")
    @DatabaseField(canBeNull = false)
    private String summary;

    @TrackorField(value = "VQS_IT_SUBM_DATE", humanName = "Submission date")
    @DatabaseField(canBeNull = false)
    private Date submissionDate;

    @TrackorField(value = "VQS_IT_SUBMITTED_BY", humanName = "Submitted by")
    @DatabaseField(canBeNull = false)
    private String submittedBy;

    @TrackorField(value = "VQS_IT_ASSIGNED", humanName = "Assigned to")
    @DatabaseField(canBeNull = false)
    private String assignedTo;

    @TrackorField(value = "VQS_IT_PRIORITY", humanName = "Priority")
    @DatabaseField(canBeNull = false)
    private String priority;

    @TrackorField(value = "VQS_IT_STATUS", humanName = "Status")
    @DatabaseField(canBeNull = false)
    private String status;

    @TrackorField(value = "Version.TRACKOR_KEY", humanName = "Version")
    @DatabaseField
    private String version;

    @DatabaseField
    private boolean autoRemove;

    @ForeignCollectionField
    private ForeignCollection<TimeRecord> timeRecordForeignCollection;

    private IssueState state = IssueState.Idle;

    @Override
    public String getTrackorName() {
        return "Issue";
    }

    String getReadableName() {
        return this.getTrackorKey() + " (" + this.getSummary() + ")";
    }

    @Singleton
    static class Comparator implements java.util.Comparator<Issue> {
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
}
