package ru.kupchinskiy.issuetimewatchdog.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.inject.Inject;

import java.lang.reflect.Type;
import java.util.Date;

import ru.kupchinskiy.issuetimewatchdog.helper.RemoteUserSettings;

public class DateSerializer implements JsonSerializer<Date> {

    @Inject
    private RemoteUserSettings remoteUserSettings;

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(remoteUserSettings.getDateFormatter().format(src));
    }

}
