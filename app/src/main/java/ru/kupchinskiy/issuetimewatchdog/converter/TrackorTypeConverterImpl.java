package ru.kupchinskiy.issuetimewatchdog.converter;

import android.support.annotation.NonNull;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import roboguice.inject.ContextSingleton;
import ru.kupchinskiy.issuetimewatchdog.helper.RemoteUserSettings;
import ru.kupchinskiy.issuetimewatchdog.model.Issue;
import ru.kupchinskiy.issuetimewatchdog.model.TimeRecord;
import ru.kupchinskiy.issuetimewatchdog.model.Trackor;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3TrackorCreateRequest;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient.V3TrackorTypeSpec;

@ContextSingleton
public class TrackorTypeConverterImpl implements TrackorTypeConverter {

    private static final List<Class<? extends Trackor>> TRACKOR_TYPE_CLASSES = Collections.unmodifiableList(
            Arrays.asList(Issue.class, TimeRecord.class));

    private ListMultimap<Class<? extends Trackor>, FieldParser> fieldParserListMultimap =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    private static final Set<String> FORMAT_FIELDS_EXCLUDE = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("TRACKOR_ID")));

    @Inject
    private Gson gson;

    @Inject
    private RemoteUserSettings remoteUserSettings;

    private final Function<FieldWithValue, Object> primitiveConverterFunc = new Function<FieldWithValue, Object>() {

        @Override
        public Object apply(FieldWithValue fieldWithValue) {
            Field field = fieldWithValue.getField();

            if (fieldWithValue.getJsonElement().isJsonNull()) {
                if (field.getType().isPrimitive()) {
                    throw new IllegalArgumentException("Null can be assigned to primitive [field=\"" + field.getName() + "\"]");
                }

                return null;
            }

            JsonPrimitive jsonPrimitive = fieldWithValue.getJsonElement().getAsJsonPrimitive();

            if (String.class.equals(field.getType())) {
                return jsonPrimitive.getAsString();
            } else if (Long.class.equals(field.getType()) || Long.TYPE.equals(field.getType())) {
                return jsonPrimitive.getAsLong();
            } else if (Float.class.equals(field.getType()) || Float.TYPE.equals(field.getType())) {
                return jsonPrimitive.getAsFloat();
            } else if (Integer.class.equals(field.getType()) || Integer.TYPE.equals(field.getType())) {
                return jsonPrimitive.getAsInt();
            } else if (Date.class.equals(field.getType())) {
                try {
                    return remoteUserSettings.getDateFormatter().parse(jsonPrimitive.getAsString());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalArgumentException("Unknown type [field=\"" + field.getName() + "\"]");
            }
        }

    };

    public TrackorTypeConverterImpl() {
        List<Type> validTypesForPrimitiveConverter = Arrays.asList(
                String.class,
                Long.class, Long.TYPE,
                Float.class, Float.TYPE,
                Integer.class, Integer.TYPE,
                Date.class);

        for (Class<? extends Trackor> typeClass : TRACKOR_TYPE_CLASSES) {
            for (Field field : typeClass.getDeclaredFields()) {
                if (!field.isAnnotationPresent(SerializedName.class)) {
                    continue;
                }

                if (validTypesForPrimitiveConverter.contains(field.getType())) {
                    fieldParserListMultimap.put(typeClass, new FieldParser(field, primitiveConverterFunc));
                }
            }
        }
    }

    @Override
    public String instanceToString(Trackor trackor, @NonNull List<V3TrackorTypeSpec> trackorTypeSpecs) {
        StringBuilder result = new StringBuilder();

        for (Field field : trackor.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(SerializedName.class)) {
                continue;
            }

            String fieldName = getFieldName(field, trackorTypeSpecs);
            field.setAccessible(true);

            try {
                Object value = field.get(trackor);
                result.append(fieldName).append(": ").append(value != null ? value.toString() : "[empty]").append("\n");
            } catch (IllegalAccessException ignored) {
            }
        }

        return result.toString();
    }

    @Override
    public <T extends Trackor> T fromJson(Class<T> typeClass, JsonObject jsonObject) {
        T trackorType;

        try {
            trackorType = typeClass.newInstance();
        } catch (Exception e) {
            return null;
        }

        List<FieldParser> fieldParsers = fieldParserListMultimap.get(typeClass);

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            for (FieldParser fieldParser : fieldParsers) {
                if (entry.getKey().equals(fieldParser.getRemoteName())) {
                    Field field = fieldParser.getField();
                    FieldWithValue fieldWithValue = new FieldWithValue(field, entry.getValue());
                    Object value = fieldParser.getConverter().apply(fieldWithValue);

                    field.setAccessible(true);
                    try {
                        field.set(trackorType, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return trackorType;
    }

    @Override
    public <T extends Trackor> String getTrackorTypeName(Class<T> typeClass) {
        Method method;
        try {
            method = typeClass.getDeclaredMethod("getTrackorTypeName");
            return Modifier.isStatic(method.getModifiers()) ? (String) method.invoke(null) : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public <T extends Trackor> List<String> formatTrackorTypeFields(Class<T> typeClass) {
        List<String> result = new ArrayList<>();

        for (Field field : typeClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(SerializedName.class)) {
                continue;
            }

            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            if (!FORMAT_FIELDS_EXCLUDE.contains(serializedName.value())) {
                result.add(serializedName.value());
            }
        }

        return result;
    }

    private String getFieldName(Field field, List<V3TrackorTypeSpec> trackorTypeSpecs) {
        SerializedName serializedName = field.getAnnotation(SerializedName.class);
        String remoteFieldName = serializedName.value();

        // Find field in trackor specs
        for (V3TrackorTypeSpec spec : trackorTypeSpecs) {
            if (spec.getName().equals(remoteFieldName)) {
                return spec.getLabel();
            }
        }

        return remoteFieldName + "[no lbl]";
    }

    @Override
    public void fillTrackorCreateRequest(V3TrackorCreateRequest trackorCreateRequest, Trackor trackor) {
        JsonObject jsonObject = gson.toJsonTree(trackor).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getValue().isJsonPrimitive()) {
                trackorCreateRequest.getFields().put(entry.getKey(), entry.getValue().getAsString());
            } else if (entry.getValue().isJsonNull()) {
                trackorCreateRequest.getFields().put(entry.getKey(), "null");
            }
        }
    }

    @Getter
    @RequiredArgsConstructor
    private class FieldParser {

        private final Field field;
        private final Function<FieldWithValue, Object> converter;

        private String getRemoteName() {
            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            return serializedName.value();
        }

    }

    @Getter
    @RequiredArgsConstructor
    private class FieldWithValue {

        private final Field field;
        private final JsonElement jsonElement;

    }

}
