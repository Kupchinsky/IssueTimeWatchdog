package ru.kupchinskiy.issuetimewatchdog.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Calendar;
import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.kupchinskiy.issuetimewatchdog.helper.MyDateUtils;
import ru.kupchinskiy.issuetimewatchdog.helper.RemoteUserSettings;

@Getter
@Setter
@NoArgsConstructor
@ToString
@DatabaseTable
@EqualsAndHashCode(of = "id")
public class TimeRecord implements Comparable<TimeRecord>, Trackor {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private Issue issue;

    @Expose
    @SerializedName("VQS_IT_WORK_DATE")
    @DatabaseField
    private Date date;

    @Expose
    @SerializedName("VQS_IT_SPENT_HOURS")
    @DatabaseField(canBeNull = false)
    private float workedTime;

    @Expose
    @SerializedName("TRACKOR_KEY")
    @DatabaseField(index = true)
    private String trackorKey;

    // TODO: implement write this before upload
    @Expose
    @SerializedName("VQS_IT_COMMENTS_TR")
    @DatabaseField
    private String comments;

    @DatabaseField(canBeNull = false)
    private float wroteTime;

    @Expose(serialize = false)
    @SerializedName("TRACKOR_ID")
    @DatabaseField
    private Long remoteTrackorId;

    @ForeignCollectionField(orderColumnName = "date", orderAscending = false)
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

    public String getReadableName(RemoteUserSettings remoteUserSettings) {
        return issue.getTrackorKey() + " of date " + remoteUserSettings.getDateFormatter().format(date);
    }

    public static String getTrackorTypeName() {
        return "Time_Record";
    }

}
