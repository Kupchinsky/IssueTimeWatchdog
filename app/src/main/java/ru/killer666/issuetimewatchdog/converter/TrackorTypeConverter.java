package ru.killer666.issuetimewatchdog.converter;

import com.google.gson.JsonObject;

import java.util.List;

import ru.killer666.issuetimewatchdog.model.Trackor;
import ru.killer666.issuetimewatchdog.services.ApiClient.V3TrackorCreateRequest;

public interface TrackorTypeConverter {

    // TODO: use or remove
    String instanceToString(Trackor trackor);

    <T extends Trackor> T fromJson(Class<T> typeClass, JsonObject jsonObject);

    <T extends Trackor> String getTrackorTypeName(Class<T> typeClass);

    <T extends Trackor> List<String> formatTrackorTypeFields(Class<T> typeClass);

    void fillTrackorCreateRequest(V3TrackorCreateRequest trackorCreateRequest, Trackor trackor);

}
