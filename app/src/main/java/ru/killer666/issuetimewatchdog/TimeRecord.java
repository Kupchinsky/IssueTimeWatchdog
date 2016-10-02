package ru.killer666.issuetimewatchdog;

import android.support.annotation.NonNull;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Calendar;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@DatabaseTable(daoClass = TimeRecordDao.class)
public class TimeRecord implements Comparable<TimeRecord>, TrackorType {
    @DatabaseField(generatedId = true)
    private int id;

    @TrackorField
    @DatabaseField
    private Issue issue;

    @TrackorField("VQS_IT_WORK_DATE")
    @DatabaseField
    private Date date;

    @TrackorField("VQS_IT_SPENT_HOURS")
    @DatabaseField
    private float workedTime;

    @TrackorField(TrackorType.ID)
    @DatabaseField
    private Long trackorId;

    @TrackorField(TrackorType.KEY)
    @DatabaseField
    private String trackorKey;

    TimeRecord(Issue issue) {
        this.issue = issue;
        this.date = MyDateUtils.getStartOfDay(Calendar.getInstance().getTime());
    }

    void increaseWorkedTime(float value) {
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
