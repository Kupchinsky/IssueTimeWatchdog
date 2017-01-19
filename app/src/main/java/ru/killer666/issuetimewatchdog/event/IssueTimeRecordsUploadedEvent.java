package ru.killer666.issuetimewatchdog.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.killer666.issuetimewatchdog.model.Issue;

@AllArgsConstructor
@Getter
public class IssueTimeRecordsUploadedEvent {

    private Issue issue;

}
