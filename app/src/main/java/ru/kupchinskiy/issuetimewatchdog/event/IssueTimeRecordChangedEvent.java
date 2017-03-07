package ru.kupchinskiy.issuetimewatchdog.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecordLogType;

@AllArgsConstructor
@Getter
public class IssueTimeRecordChangedEvent {

    private Issue issue;
    private TimeRecordLogType timeRecordLogType;

}
