package ru.killer666.issuetimewatchdog.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.killer666.issuetimewatchdog.model.Issue;

@AllArgsConstructor
@Getter
public class IssueTimeRecordsUploadCompleteEvent {

    private Issue issue;
    private Throwable throwable;

    public boolean isErrored() {
        return throwable != null;
    }

}
