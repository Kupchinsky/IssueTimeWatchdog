package ru.killer666.issuetimewatchdog.helper;


import android.util.Pair;

import java.lang.reflect.Field;
import java.util.List;

import ru.killer666.issuetimewatchdog.TrackorTypeObjectConverter;

public abstract class DialogSettings<T> {

    public DialogSettings(TrackorTypeObjectConverter trackorTypeObjectConverter) {
        this.trackorTypeObjectConverter = trackorTypeObjectConverter;
    }

    public abstract String getSelectTitle();

    public String getSelectItem(T instance) {
        return instance.getTrackorKey();
    }

    public String getDetailsMessage(T instance) {
        List<Pair<Field, TrackorTypeObjectConverter.Parser>> pairList = this.trackorTypeObjectConverter.getTypesMap().get(instance.getClass());
        String message = "";

        for (Pair<Field, TrackorTypeObjectConverter.Parser> pair : pairList) {
            TrackorField trackorField = pair.first.getAnnotation(TrackorField.class);
            String humanName = trackorField.humanName();

            if (humanName.isEmpty()) {
                humanName = pair.first.getName() + "[auto]";
            }

            try {
                Object value = pair.first.get(instance);
                message += "\n" + humanName + ": " +
                        (value != null ? pair.second.parseTo(pair.first, value).getAsString() : "[empty]");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return message.trim();
    }

    boolean isConfirmable() {
        return false;
    }

}