package ru.kupchinskiy.issuetimewatchdog.helper;

import com.google.inject.Inject;

import roboguice.inject.ContextSingleton;
import ru.kupchinskiy.issuetimewatchdog.dao.IssueDao;
import ru.kupchinskiy.issuetimewatchdog.model.Issue;

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
