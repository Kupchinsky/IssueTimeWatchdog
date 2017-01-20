package ru.killer666.issuetimewatchdog.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.IssueState;
import ru.killer666.issuetimewatchdog.model.TimeRecordLogType;

@AllArgsConstructor
@Getter
public class IssueStateChangedEvent {

    private Issue issue;
    private IssueState oldState;
    private TimeRecordLogType timeRecordLogType;

}
