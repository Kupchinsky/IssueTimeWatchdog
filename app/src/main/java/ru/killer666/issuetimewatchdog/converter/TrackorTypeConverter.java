package ru.killer666.issuetimewatchdog.converter;

import com.google.gson.JsonObject;

import java.util.List;

import ru.killer666.issuetimewatchdog.model.TrackorType;

public interface TrackorTypeConverter {

    String instanceToString(TrackorType trackorType);

    <T extends TrackorType> T fromJson(Class<T> typeClass, JsonObject jsonObject);

    <T extends TrackorType> String getTrackorTypeName(Class<T> typeClass);

    <T extends TrackorType> List<String> formatTrackorTypeFields(Class<T> typeClass);

}
