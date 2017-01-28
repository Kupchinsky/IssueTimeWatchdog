package ru.killer666.issuetimewatchdog.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;

import ru.killer666.issuetimewatchdog.helper.RemoteUserSettings;

public class DateSerializer implements JsonSerializer<Date> {

    private final RemoteUserSettings remoteUserSettings;

    public DateSerializer(RemoteUserSettings remoteUserSettings) {
        this.remoteUserSettings = remoteUserSettings;
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(remoteUserSettings.getDateFormatter().format(src));
    }

}
