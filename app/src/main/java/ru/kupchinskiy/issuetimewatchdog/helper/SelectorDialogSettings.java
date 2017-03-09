package ru.kupchinskiy.issuetimewatchdog.helper;

import com.google.inject.Inject;

import ru.kupchinskiy.issuetimewatchdog.converter.TrackorTypeConverter;
import ru.kupchinskiy.issuetimewatchdog.model.Trackor;

public abstract class SelectorDialogSettings<T extends Trackor> {

    @Inject
    private TrackorTypeConverter trackorTypeConverter;

    public abstract String getSelectTitle();

    public String getSelectItem(T instance) {
        return instance.getTrackorKey();
    }

    public String getDetailsMessage(T instance) {
        return trackorTypeConverter.instanceToString(instance);
    }

    public boolean isConfirmable() {
        return false;
    }

}