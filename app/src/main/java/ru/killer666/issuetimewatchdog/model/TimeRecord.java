package ru.killer666.issuetimewatchdog.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
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
import ru.killer666.issuetimewatchdog.helper.MyDateUtils;
import ru.killer666.issuetimewatchdog.helper.ReadableName;

@Getter
@Setter
@NoArgsConstructor
@ToString
@DatabaseTable
public class TimeRecord implements Comparable<TimeRecord>, TrackorType {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Issue issue;

    @SerializedName("VQS_IT_WORK_DATE")
    @ReadableName("Work date")
    @DatabaseField(canBeNull = false)
    private Date date;

    @SerializedName("VQS_IT_SPENT_HOURS")
    @ReadableName("Spent hours")
    @DatabaseField(canBeNull = false)
    private float workedTime;

    @SerializedName("TRACKOR_KEY")
    @DatabaseField(index = true)
    private String trackorKey;

    @SerializedName("") // TODO: get field name
    @DatabaseField(index = true)
    private String comments;

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

    public static String getTrackorTypeName() {
        return "Time_Record";
    }

}
