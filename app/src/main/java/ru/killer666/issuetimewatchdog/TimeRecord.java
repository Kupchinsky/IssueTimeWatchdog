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
public class TimeRecord extends SugarRecord implements Comparable<TimeRecord> {
    private Issue issue;
    private Date date;
    private float workedTime;

    public TimeRecord(Issue issue) {
        this.issue = issue;
        this.date = MyDateUtils.getStartOfDay(Calendar.getInstance().getTime());
    }

    public void increaseWorkedTime(float value) {
        this.workedTime += value;
        this.save();
    }

    @Override
    public int compareTo(@NonNull TimeRecord another) {
        return another.getDate().compareTo(this.getDate());
    }
}
