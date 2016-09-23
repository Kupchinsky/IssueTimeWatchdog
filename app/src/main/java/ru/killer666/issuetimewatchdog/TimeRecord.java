package ru.killer666.issuetimewatchdog;

import android.support.annotation.NonNull;

import com.orm.SugarRecord;

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
public class TimeRecord extends SugarRecord implements Comparable<TimeRecord>, TrackorType {
    @TrackorField
    private Issue issue;

    @TrackorField("VQS_IT_WORK_DATE")
    private Date date;

    @TrackorField("VQS_IT_SPENT_HOURS")
    private float workedTime;

    @TrackorField(TrackorType.ID)
    private Long trackorId;

    @TrackorField(TrackorType.KEY)
    private String trackorKey;

    TimeRecord(Issue issue) {
        this.issue = issue;
        this.date = MyDateUtils.getStartOfDay(Calendar.getInstance().getTime());
    }

    void increaseWorkedTime(float value) {
        this.workedTime += value;
        this.save();
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
