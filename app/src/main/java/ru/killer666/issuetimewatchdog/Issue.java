package ru.killer666.issuetimewatchdog;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@DatabaseTable(daoClass = IssueDao.class)
public class Issue implements TrackorType {
    @DatabaseField(generatedId = true)
    private int id;

    @TrackorField(TrackorType.KEY)
    @DatabaseField
    private String name;

    @TrackorField("VQS_IT_XITOR_NAME")
    @DatabaseField
    private String description;

    @TrackorField(TrackorType.ID)
    @DatabaseField
    private Long trackorId;

    private IssueState state = IssueState.Idle;

    String getReadableName() {
        return name + " (" + description + ")";
    }

    @Override
    public String getTrackorName() {
        return "Issue";
    }

    @Override
    public String getTrackorKey() {
        return this.name;
    }

    @Singleton
    static class Comparator implements java.util.Comparator<Issue> {
        @Inject
        private TimeRecordDao timeRecordDao;

        @Override
        public int compare(Issue lhs, Issue rhs) {
            TimeRecord timeRecordLhs = this.timeRecordDao.getLastOfIssue(lhs);
            TimeRecord timeRecordRhs = this.timeRecordDao.getLastOfIssue(rhs);

            return timeRecordLhs == null ? 1 : (timeRecordRhs == null ? -1 : (timeRecordLhs.compareTo(timeRecordRhs)));
        }
    }
}
