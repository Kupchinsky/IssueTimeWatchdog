package ru.killer666.issuetimewatchdog.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@DatabaseTable
public class Issue implements Trackor {

    @Expose(serialize = false, deserialize = false)
    @DatabaseField(generatedId = true)
    private int id;

    @SerializedName(value = "TRACKOR_KEY")
    @DatabaseField(canBeNull = false, index = true, unique = true)
    private String trackorKey;

    @SerializedName(value = "VQS_IT_XITOR_NAME")
    @DatabaseField(canBeNull = false)
    private String summary;

    @Expose(serialize = false, deserialize = false)
    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_INTEGER)
    private IssueState state = IssueState.Idle;

    @Expose(serialize = false, deserialize = false)
    @DatabaseField(canBeNull = false)
    private boolean removeAfterUpload;

    @Expose(serialize = false, deserialize = false)
    @ForeignCollectionField
    private ForeignCollection<TimeRecord> timeRecordForeignCollection;

    public String getReadableName() {
        return getTrackorKey() + " (" + getSummary() + ")";
    }

    public static String getTrackorTypeName() {
        return "Issue";
    }

}
