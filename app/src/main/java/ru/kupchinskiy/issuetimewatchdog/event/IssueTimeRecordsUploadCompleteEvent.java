package ru.kupchinskiy.issuetimewatchdog.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.kupchinskiy.issuetimewatchdog.model.Issue;

@AllArgsConstructor
@Getter
public class IssueTimeRecordsUploadCompleteEvent {

    private Issue issue;
    private Throwable throwable;

    public boolean isErrored() {
        return throwable != null;
    }

}
