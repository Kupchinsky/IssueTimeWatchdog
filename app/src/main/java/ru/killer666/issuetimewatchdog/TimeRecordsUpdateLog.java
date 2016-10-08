package ru.killer666.issuetimewatchdog;

import com.google.common.collect.ArrayListMultimap;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@DatabaseTable
public class TimeRecordsUpdateLog {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private Date date;

    @DatabaseField(canBeNull = false)
    private boolean isIssueRemoved;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private ArrayListMultimap<String, TimeRecordCopy> timeRecords;

    @AllArgsConstructor
    @Getter
    static class TimeRecordCopy implements Serializable {
        private Date date;
        private float wrote;
    }
}
