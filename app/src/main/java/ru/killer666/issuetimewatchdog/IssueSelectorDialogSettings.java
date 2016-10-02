package ru.killer666.issuetimewatchdog;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class IssueSelectorDialogSettings extends SelectorDialog.DialogSettings<Issue> {
    @Inject
    public IssueSelectorDialogSettings(TrackorTypeObjectConverter trackorTypeObjectConverter) {
        super(trackorTypeObjectConverter);
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
