package ru.killer666.issuetimewatchdog;

import android.support.annotation.NonNull;

import com.google.common.collect.Iterables;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Issue extends SugarRecord implements Comparable<Issue>, TrackorType {
    public static final int TIME_RECORD_SHOW_LIMIT = 7;

    @TrackorField(TrackorType.KEY)
    private String name;

    @TrackorField("VQS_IT_XITOR_NAME")
    private String description;

    @TrackorField(TrackorType.ID)
    private Long trackorId;

    @Ignore
    private IssueState state = IssueState.Idle;

    String getReadableName() {
        return name + " (" + description + ")";
    }

    List<TimeRecord> getLastTimeRecords() {
        return SugarRecord.find(TimeRecord.class, "issue = ?",
                new String[]{String.valueOf(this.getId())}, null, "date DESC",
                String.valueOf(TIME_RECORD_SHOW_LIMIT));
    }

    TimeRecord getLastTimeRecord() {
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

    @Override
    public String getTrackorName() {
        return "Issue";
    }

    @Override
    public String getTrackorKey() {
        return this.name;
    }
}
