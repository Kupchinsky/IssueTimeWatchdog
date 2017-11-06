package ru.kupchinskiy.issuetimewatchdog.converter;

import com.google.gson.JsonObject;

import java.util.List;

import ru.kupchinskiy.issuetimewatchdog.model.Trackor;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3TrackorCreateRequest;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3TrackorTypeSpec;

public interface TrackorTypeConverter {

    String instanceToString(Trackor trackor, List<V3TrackorTypeSpec> trackorTypeSpecs);

    <T extends Trackor> T fromJson(Class<T> typeClass, JsonObject jsonObject);

    <T extends Trackor> String getTrackorTypeName(Class<T> typeClass);

    <T extends Trackor> List<String> formatTrackorTypeFields(Class<T> typeClass);

    void fillTrackorCreateRequest(V3TrackorCreateRequest trackorCreateRequest, Trackor trackor);

}
