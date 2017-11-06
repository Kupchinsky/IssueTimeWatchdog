package ru.kupchinskiy.issuetimewatchdog.helper;

import com.google.inject.Inject;

import java.util.List;

import ru.kupchinskiy.issuetimewatchdog.converter.TrackorTypeConverter;
import ru.kupchinskiy.issuetimewatchdog.model.Trackor;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3TrackorTypeSpec;

public abstract class SelectorDialogSettings<T extends Trackor> {

    @Inject
    private TrackorTypeConverter trackorTypeConverter;

    public abstract String getSelectTitle();

    public String getSelectItem(T instance) {
        return instance.getTrackorKey();
    }

    public String getDetailsMessage(T instance, List<V3TrackorTypeSpec> trackorTypeSpecs) {
        return trackorTypeConverter.instanceToString(instance, trackorTypeSpecs);
    }

    public boolean isConfirmable() {
        return false;
    }

}