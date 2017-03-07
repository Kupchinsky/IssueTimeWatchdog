package ru.kupchinskiy.issuetimewatchdog.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.model.IssueState;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecordLogType;

@AllArgsConstructor
@Getter
public class IssueStateChangedEvent {

    private Issue issue;
    private IssueState oldState;
    private TimeRecordLogType timeRecordLogType;

}
