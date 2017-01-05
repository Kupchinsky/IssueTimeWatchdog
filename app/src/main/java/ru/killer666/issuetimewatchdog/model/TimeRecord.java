package ru.killer666.issuetimewatchdog.model;

import android.support.annotation.NonNull;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Calendar;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.killer666.issuetimewatchdog.MyDateUtils;
import ru.killer666.issuetimewatchdog.TimeRecordStartStop;

@Getter
@Setter
@NoArgsConstructor
@ToString
@DatabaseTable
public class TimeRecord implements Comparable<TimeRecord>, TrackorType {
    @DatabaseField(generatedId = true)
    private int id;

    @TrackorField
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Issue issue;

    @TrackorField("VQS_IT_WORK_DATE")
    @DatabaseField(canBeNull = false)
    private Date date;

    @TrackorField("VQS_IT_SPENT_HOURS")
    @DatabaseField(canBeNull = false)
    private float workedTime;

    @TrackorField(KEY)
    @DatabaseField(index = true)
    private String trackorKey;

    @DatabaseField(canBeNull = false)
    private float wroteTime;

    @ForeignCollectionField
    private ForeignCollection<TimeRecordStartStop> timeRecordStartStopForeignCollection;

    public TimeRecord(Issue issue) {
        this.issue = issue;
        this.date = MyDateUtils.getStartOfDay(Calendar.getInstance().getTime());
    }

    public void increaseWorkedTime(float value) {
        this.workedTime += value;
    }

    @Override
    public int compareTo(@NonNull TimeRecord another) {
        return another.getDate().compareTo(this.getDate());
    }

    @Override
    public String getTrackorName() {
        return "Time_Record";
    }
}
