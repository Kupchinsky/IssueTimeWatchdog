package ru.killer666.issuetimewatchdog.helper;

import com.google.inject.Inject;

import ru.killer666.issuetimewatchdog.converter.TrackorTypeConverter;
import ru.killer666.issuetimewatchdog.model.TrackorType;

public abstract class SelectorDialogSettings<T extends TrackorType> {

    @Inject
    private TrackorTypeConverter trackorTypeConverter;

    public abstract String getSelectTitle();

    public String getSelectItem(T instance) {
        return instance.getTrackorKey();
    }

    public String getDetailsMessage(T instance) {
        // TODO: implement (required v3 api for read config field labels)
        return "Not implemented now!";
    }

    public boolean isConfirmable() {
        return false;
    }

}