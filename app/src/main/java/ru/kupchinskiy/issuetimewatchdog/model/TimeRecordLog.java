package ru.kupchinskiy.issuetimewatchdog.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@DatabaseTable
public class TimeRecordLog implements Comparable<TimeRecordLog> {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private TimeRecord timeRecord;

    @DatabaseField(canBeNull = false)
    private Date date;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_INTEGER)
    private TimeRecordLogType type;

    @Override
    public int compareTo(TimeRecordLog another) {
        return another.getDate().compareTo(getDate());
    }

}
