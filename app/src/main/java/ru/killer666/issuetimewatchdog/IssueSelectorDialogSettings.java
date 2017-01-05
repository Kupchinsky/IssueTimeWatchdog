package ru.killer666.issuetimewatchdog;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.model.Issue;

@Singleton
public class IssueSelectorDialogSettings extends SelectorDialog.DialogSettings<Issue> {
    @Inject
    private IssueDao issueDao;

    @Inject
    public IssueSelectorDialogSettings(TrackorTypeObjectConverter trackorTypeObjectConverter) {
        super(trackorTypeObjectConverter);
    }

    String getDetailsMessage(Issue instance, boolean showAlready) {
        String message = super.getDetailsMessage(instance);

        if (showAlready && this.issueDao.idExists(instance.getId()) && !instance.isAutoRemove()) {
            message = "(Already exists in list)\n\n" + message;
        }

        return message;
    }

    @Override
    String getDetailsMessage(Issue instance) {
        return this.getDetailsMessage(instance, true);
    }

    @Override
    String getSelectTitle() {
        return "Select new issue";
    }

    @Override
    String getSelectItem(Issue instance) {
        return instance.getReadableName();
    }

    @Override
    boolean isConfirmable() {
        return true;
    }
}
