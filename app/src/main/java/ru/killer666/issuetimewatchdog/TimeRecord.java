package ru.killer666.issuetimewatchdog;

import android.support.annotation.NonNull;

import com.orm.SugarRecord;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TimeRecord extends SugarRecord implements Serializable, Comparable<TimeRecord> {
    private Long id;
    private Issue issue;
    private Date date;
    private float workedTime;

    public TimeRecord(Issue issue) {
        this.issue = issue;
        this.date = Calendar.getInstance().getTime();
    }

    public void increaseWorkedTime(float value) {
        this.workedTime += value;
        SugarRecord.save(this);
    }

    @Override
    public int compareTo(@NonNull TimeRecord another) {
        return another.getDate().compareTo(this.getDate());
    }
}
