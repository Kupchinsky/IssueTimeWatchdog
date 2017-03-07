package ru.killer666.issuetimewatchdog.helper;

import com.google.inject.Inject;

import roboguice.inject.ContextSingleton;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.model.Issue;

@ContextSingleton
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
