package ru.killer666.issuetimewatchdog.converter;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.TimeRecord;
import ru.killer666.issuetimewatchdog.model.Trackor;
import ru.killer666.issuetimewatchdog.services.ApiClient;

@Singleton
public class TrackorTypeConverterImpl implements TrackorTypeConverter {

    private static final List<Class<? extends Trackor>> TRACKOR_TYPE_CLASSES = Collections.unmodifiableList(
            Arrays.asList(Issue.class, TimeRecord.class));

    private ListMultimap<Class<? extends Trackor>, FieldParser> fieldParserListMultimap =
            Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    @Inject
    private Gson gson;

    private final Function<FieldWithValue, Object> primitiveConverterFunc = new Function<FieldWithValue, Object>() {

        private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        {
            dateFormat.setLenient(false);
        }

        @Override
        public Object apply(FieldWithValue fieldWithValue) {
            if (fieldWithValue.getJsonElement().isJsonNull()) {
                return null;
            }

            Field field = fieldWithValue.getField();
            JsonPrimitive jsonPrimitive = fieldWithValue.getJsonElement().getAsJsonPrimitive();

            if (field.getType().equals(String.class)) {
                return jsonPrimitive.getAsString();
            } else if (field.getType().equals(Long.class)) {
                return jsonPrimitive.getAsLong();
            } else if (field.getType().equals(Float.class)) {
                return jsonPrimitive.getAsFloat();
            } else if (field.getType().equals(Integer.class)) {
                return jsonPrimitive.getAsInt();
            } else if (field.getType().equals(Date.class)) {
                try {
                    return dateFormat.parse(jsonPrimitive.getAsString());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalStateException("Unknown type");
            }
        }

    };

    public TrackorTypeConverterImpl() {
        List<Type> validTypesForPrimitiveConverter = Arrays.asList(String.class,
                Long.class, Long.TYPE,
                Double.class, Double.TYPE,
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
    public String instanceToString(Trackor trackor) {
        String result = "";

        for (Field field : trackor.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(SerializedName.class)) {
                continue;
            }

            String fieldName = getFieldName(field);
            field.setAccessible(true);

            try {
                Object value = field.get(trackor);
                result += fieldName + ": " + (value != null ? value.toString() : "[empty]") + "\n";
            } catch (IllegalAccessException ignored) {
            }
        }

        return result;
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
        List<String> result = Lists.newArrayList();

        for (Field field : typeClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(SerializedName.class)) {
                continue;
            }

            SerializedName serializedName = field.getAnnotation(SerializedName.class);
            result.add(serializedName.value());
        }

        return result;
    }

    private String getFieldName(Field field) {
        // TODO: implement
        return "Not implemented now!";
    }

    @Override
    public void fillTrackorCreateRequest(ApiClient.V2TrackorCreateRequest trackorCreateRequest, Trackor trackor) {
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
