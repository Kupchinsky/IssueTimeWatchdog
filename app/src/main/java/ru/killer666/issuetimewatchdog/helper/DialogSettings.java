package ru.killer666.issuetimewatchdog.helper;

import com.google.inject.Inject;

import ru.killer666.issuetimewatchdog.converter.TrackorTypeConverter;
import ru.killer666.issuetimewatchdog.model.TrackorType;

public abstract class DialogSettings<T extends TrackorType> {

    @Inject
    private TrackorTypeConverter trackorTypeConverter;

    public abstract String getSelectTitle();

    public String getSelectItem(T instance) {
        return instance.getTrackorKey();
    }

    public String getDetailsMessage(T instance) {
        return trackorTypeConverter.instanceToString(instance);
    }

    // TODO: use this or remove
    public boolean isConfirmable() {
        return false;
    }

}