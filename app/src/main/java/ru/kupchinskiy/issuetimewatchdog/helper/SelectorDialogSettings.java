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
        // TODO: implement (required v3 api for read config field labels) and select view
        return "Not implemented now!";
    }

    public boolean isConfirmable() {
        return false;
    }

}