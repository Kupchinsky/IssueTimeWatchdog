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
import ru.killer666.issuetimewatchdog.helper.ConfigFieldFormatter;
import ru.killer666.issuetimewatchdog.helper.MyDateUtils;

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
    @DatabaseField
    private Date date;

    @SerializedName("VQS_IT_SPENT_HOURS")
    @DatabaseField(canBeNull = false)
    private double workedTime;

    @SerializedName("TRACKOR_KEY")
    @DatabaseField(index = true)
    private String trackorKey;

    // TODO: implement write this before upload
    @SerializedName("VQS_IT_COMMENTS_TR")
    @DatabaseField
    private String comments;

    @DatabaseField(canBeNull = false)
    private double wroteTime;

    @DatabaseField
    private Long remoteTrackorId;

    @ForeignCollectionField
    private ForeignCollection<TimeRecordLog> timeRecordLogForeignCollection;

    public TimeRecord(Issue issue) {
        this.issue = issue;
        date = MyDateUtils.getStartOfDay(Calendar.getInstance().getTime());
    }

    public void increaseWorkedTime(double value) {
        workedTime += value;
    }

    public void decreaseWorkedTime(double value) {
        workedTime -= value;
    }

    @Override
    public int compareTo(@NonNull TimeRecord another) {
        return another.getDate().compareTo(getDate());
    }

    public String getReadableName(ConfigFieldFormatter configFieldFormatter) {
        return issue.getTrackorKey() + " of date " + configFieldFormatter.getDateFormatter().format(date);
    }

    public static String getTrackorTypeName() {
        return "Time_Record";
    }

}
