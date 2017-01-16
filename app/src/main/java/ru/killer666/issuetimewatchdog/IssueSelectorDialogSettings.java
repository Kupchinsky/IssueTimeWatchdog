package ru.killer666.issuetimewatchdog;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.ui.SelectorDialog;

@Singleton
public class IssueSelectorDialogSettings extends SelectorDialog.DialogSettings<Issue> {
    @Inject
    private IssueDao issueDao;

    @Inject
    public IssueSelectorDialogSettings(TrackorTypeObjectConverter trackorTypeObjectConverter) {
        super(trackorTypeObjectConverter);
    }

    public String getDetailsMessage(Issue instance, boolean showAlready) {
        String message = super.getDetailsMessage(instance);

        if (showAlready && this.issueDao.idExists(instance.getId()) && !instance.isAutoRemove()) {
            message = "(Already exists in list)\n\n" + message;
        }

        return message;
    }

    @Override
    public String getDetailsMessage(Issue instance) {
        return this.getDetailsMessage(instance, true);
    }

    @Override
    public String getSelectTitle() {
        return "Select new issue";
    }

    @Override
    public String getSelectItem(Issue instance) {
        return instance.getReadableName();
    }

    public boolean isConfirmable() {
        return true;
    }
}
