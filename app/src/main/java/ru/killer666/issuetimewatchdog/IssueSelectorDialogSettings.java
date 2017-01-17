package ru.killer666.issuetimewatchdog;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.helper.SelectorDialogSettings;
import ru.killer666.issuetimewatchdog.model.Issue;

@Singleton
public class IssueSelectorDialogSettings extends SelectorDialogSettings<Issue> {

    @Inject
    private IssueDao issueDao;

    @Override
    public String getSelectTitle() {
        return "Select new issue";
    }

    @Override
    public String getSelectItem(Issue instance) {
        return instance.getReadableName();
    }

    @Override
    public boolean isConfirmable() {
        return true;
    }

}
