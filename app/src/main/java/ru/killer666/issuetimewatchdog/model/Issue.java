package ru.killer666.issuetimewatchdog.model;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.killer666.issuetimewatchdog.helper.ReadableName;

@Getter
@Setter
@NoArgsConstructor
@ToString
@DatabaseTable
public class Issue {

    @DatabaseField(generatedId = true)
    private int id;

    @SerializedName(value = "TRACKOR_KEY")
    @ReadableName("Issue ID")
    @DatabaseField(canBeNull = false, index = true)
    private String trackorKey;

    @SerializedName(value = "VQS_IT_XITOR_NAME")
    @ReadableName("Summary")
    @DatabaseField(canBeNull = false)
    private String summary;

    @SerializedName(value = "VQS_IT_SUBM_DATE")
    @ReadableName("Submission date")
    @DatabaseField(canBeNull = false)
    private Date submissionDate;

    @SerializedName(value = "VQS_IT_SUBMITTED_BY")
    @ReadableName("Submitted by")
    @DatabaseField(canBeNull = false)
    private String submittedBy;

    @SerializedName(value = "VQS_IT_ASSIGNED")
    @ReadableName("Assigned to")
    @DatabaseField(canBeNull = false)
    private String assignedTo;

    @SerializedName(value = "VQS_IT_PRIORITY")
    @ReadableName("Priority")
    @DatabaseField(canBeNull = false)
    private String priority;

    @SerializedName(value = "VQS_IT_STATUS")
    @ReadableName("Status")
    @DatabaseField(canBeNull = false)
    private String status;

    @SerializedName(value = "Version.TRACKOR_KEY")
    @ReadableName("Version")
    @DatabaseField
    private String version;

    @DatabaseField
    private boolean autoRemove;

    @ForeignCollectionField
    private ForeignCollection<TimeRecord> timeRecordForeignCollection;

    private IssueState state = IssueState.Idle;

    public String getReadableName() {
        return this.getTrackorKey() + " (" + this.getSummary() + ")";
    }

}
