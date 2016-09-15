package ru.killer666.issuetimewatchdog;

import android.support.annotation.NonNull;

import com.google.common.collect.Iterables;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Issue extends SugarRecord implements Serializable, Comparable<Issue> {
    public static final int TIME_RECORD_SHOW_LIMIT = 7;

    private Long id;
    private String name;
    private String description;

    @Ignore
    private IssueState state = IssueState.Idle;

    public String getReadableName() {
        return name + " (" + description + ")";
    }

    public List<TimeRecord> getLastTimeRecords() {
        return SugarRecord.find(TimeRecord.class, "issue = ?",
                new String[]{String.valueOf(this.getId())}, null, "date DESC",
                String.valueOf(TIME_RECORD_SHOW_LIMIT));
    }

    public TimeRecord getLastTimeRecord() {
        return Iterables.getFirst(SugarRecord.find(TimeRecord.class, "issue = ?",
                new String[]{String.valueOf(this.getId())}, null, "date DESC",
                String.valueOf(1)), null);
    }

    @Override
    public int compareTo(@NonNull Issue another) {
        TimeRecord timeRecord = this.getLastTimeRecord();
        TimeRecord timeRecordAnother = another.getLastTimeRecord();

        return timeRecord == null ? 1 : (timeRecordAnother == null ? -1 : (timeRecord.compareTo(timeRecordAnother)));
    }
}
