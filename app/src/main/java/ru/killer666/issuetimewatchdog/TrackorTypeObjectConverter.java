package ru.killer666.issuetimewatchdog;

import android.util.Pair;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import lombok.Getter;
import roboguice.util.Strings;
import ru.killer666.issuetimewatchdog.dao.IssueDao;
import ru.killer666.issuetimewatchdog.dao.TimeRecordDao;
import ru.killer666.issuetimewatchdog.model.Issue;
import ru.killer666.issuetimewatchdog.model.TimeRecord;

@Singleton
public class TrackorTypeObjectConverter {
    @Getter
    private ListMultimap<Class<? extends TrackorType>, Pair<Field, Parser>> typesMap = Multimaps.synchronizedListMultimap(ArrayListMultimap.<Class<? extends TrackorType>, Pair<Field, Parser>>create());
    private Map<Class<? extends TrackorType>, String> trackorTypeNamesMap = Maps.newConcurrentMap();
    private Map<Class<? extends TrackorType>, JsonToPlainObjectHelper> trackorTypeHelperMap = Maps.newConcurrentMap();

    @Inject
    private IssueDao issueDao;
    @Inject
    private TimeRecordDao timeRecordDao;

    @Inject
    public TrackorTypeObjectConverter(TrackorTypeClasspathScanner trackorTypeClasspathScanner) {
        Map<Class<?>, Parser> parserMap = Maps.newHashMap();

        PrimitiveParser primitiveParser = new PrimitiveParser();
        parserMap.put(String.class, primitiveParser);
        parserMap.put(Long.class, primitiveParser);
        parserMap.put(Float.class, primitiveParser);
        parserMap.put(Integer.class, primitiveParser);
        parserMap.put(Date.class, primitiveParser);

        TrackorTypeParser trackorTypeParser = new TrackorTypeParser();

        for (Class<? extends TrackorType> typeClass : trackorTypeClasspathScanner.scan()) {
            for (Field field : typeClass.getDeclaredFields()) {
                if (!field.isAnnotationPresent(TrackorField.class)) {
                    continue;
                }

                field.setAccessible(true);

                Class<?> fieldTypeClass = field.getType();
                Parser parser = parserMap.get(fieldTypeClass);
                TrackorType typeInstance;

                try {
                    typeInstance = typeClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                if (parser == null && TrackorType.class.isInstance(typeInstance)) {
                    parser = trackorTypeParser;
                }

                Preconditions.checkState(parser != null, "Parser not found for " + typeClass.getSimpleName() + "." + field.getName());

                this.typesMap.put(typeClass, Pair.create(field, parser));
                this.trackorTypeNamesMap.put(typeClass, typeInstance.getTrackorName());
            }
        }

        this.trackorTypeHelperMap.put(Issue.class, trackorKey -> this.issueDao.queryForTrackorKey(trackorKey));
        this.trackorTypeHelperMap.put(TimeRecord.class, trackorKey -> this.timeRecordDao.queryForTrackorKey(trackorKey));
    }

    public String getTrackorTypeName(Class<? extends TrackorType> trackorTypeClass) {
        return this.trackorTypeNamesMap.get(trackorTypeClass);
    }

    public <T extends TrackorType> T fromJson(Class<T> typeClass, JsonObject jsonObject) {
        Preconditions.checkState(this.typesMap.containsKey(typeClass), "Class " + typeClass.getSimpleName() + " is not TrackorType");

        @SuppressWarnings("unchecked")
        JsonToPlainObjectHelper<T> helper = this.trackorTypeHelperMap.get(typeClass);

        T instance = null;

        if (helper != null) {
            instance = helper.findExisting(jsonObject.get(TrackorType.KEY).getAsString());
        }

        if (instance == null) {
            try {
                instance = typeClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        this.fromJson(typeClass, instance, jsonObject);
        Preconditions.checkState(instance.getTrackorKey() != null && !instance.getTrackorKey().isEmpty(), "Trackor xitor_key is empty");

        return instance;
    }

    private <T extends TrackorType> void fromJson(Class<T> typeClass, TrackorType instance, final JsonObject jsonObject) {
        Callable<JsonObject> callable = () -> jsonObject;

        for (Pair<Field, Parser> pair : typesMap.get(typeClass)) {
            TrackorField trackorField = pair.first.getAnnotation(TrackorField.class);
            Object fieldValue;
            JsonElement jsonElement = jsonObject.get(trackorField.value());

            if (jsonElement.isJsonPrimitive()) {
                fieldValue = pair.second.parse(pair.first, jsonElement.getAsJsonPrimitive(), callable);
            } else if (jsonElement.isJsonNull()) {
                fieldValue = null;
            } else {
                throw new IllegalArgumentException("Invalid json element: not primitive and not null");
            }

            try {
                pair.first.set(instance, fieldValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <T extends TrackorType> String getFieldsOf(Class<T> trackorTypeClass) {
        List<String> result = Lists.newArrayList();

        for (Pair<Field, Parser> pair : this.typesMap.get(trackorTypeClass)) {
            TrackorField trackorField = pair.first.getAnnotation(TrackorField.class);
            result.add(trackorField.value());
        }

        return Strings.join(",", result);
    }

    // TODO
    /*<T extends TrackorType> JsonObject toJson(T instance, FieldFilter fieldFilter) {
        Class<? extends TrackorType> typeClass = instance.getClass();

        Preconditions.checkState(typesMap.containsKey(typeClass), "Class " + typeClass.getSimpleName() + " is not TrackorType");

        JsonObject jsonObject = new JsonObject();

        for (Pair<Field, Parser> pair : typesMap.get(typeClass)) {
            if (!fieldFilter.apply(pair.first)) {
                continue;
            }

            TrackorField trackorField = pair.first.getAnnotation(TrackorField.class);
            Object fieldValue;

            try {
                fieldValue = pair.first.get(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (fieldValue == null) {
                continue;
            }

            JsonPrimitive jsonPrimitiveLocal = pair.second.parseTo(pair.first, fieldValue);
            jsonObject.add(trackorField.value(), jsonPrimitiveLocal);
        }

        return jsonObject;
    }*/

    private class PrimitiveParser implements Parser {
        private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        {
            dateFormat.setLenient(false);
        }

        @Override
        public Object parse(Field field, JsonPrimitive jsonPrimitive, Callable<JsonObject> jsonObjectCallable) {
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
                    return this.dateFormat.parse(jsonPrimitive.getAsString());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalStateException("Unknown type");
            }
        }

        @Override
        public JsonPrimitive parseTo(Field field, Object object) {
            if (field.getType().equals(String.class)) {
                return new JsonPrimitive((String) object);
            } else if (field.getType().equals(Long.class)) {
                return new JsonPrimitive((Long) object);
            } else if (field.getType().equals(Float.class)) {
                return new JsonPrimitive((Float) object);
            } else if (field.getType().equals(Integer.class)) {
                return new JsonPrimitive((Integer) object);
            } else if (field.getType().equals(Date.class)) {
                return new JsonPrimitive(this.dateFormat.format(object));
            } else {
                throw new IllegalStateException("Unknown type");
            }
        }
    }

    private class TrackorTypeParser implements Parser {
        @SuppressWarnings("unchecked")
        @Override
        public Object parse(Field field, JsonPrimitive jsonPrimitive, Callable<JsonObject> jsonObjectCallable) {
            Class<?> targetTypeClass = field.getType();

            Preconditions.checkState(typesMap.containsKey(targetTypeClass), "Class " + targetTypeClass.getSimpleName() + " is not TrackorType");

            TrackorType instance;
            JsonObject jsonObject;

            try {
                instance = (TrackorType) targetTypeClass.newInstance();
                jsonObject = jsonObjectCallable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            for (Pair<Field, Parser> pair : typesMap.get((Class<? extends TrackorType>) targetTypeClass)) {
                TrackorField trackorField = pair.first.getAnnotation(TrackorField.class);
                JsonPrimitive jsonPrimitiveLocal = jsonObject.getAsJsonPrimitive(instance.getTrackorName() + "." + trackorField.value());

                try {
                    pair.first.set(instance, pair.second.parse(pair.first, jsonPrimitiveLocal, jsonObjectCallable));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            Preconditions.checkState(instance.getTrackorKey() != null && !instance.getTrackorKey().isEmpty(), "Trackor key is empty");

            return instance;
        }

        @Override
        public JsonPrimitive parseTo(Field field, Object object) {
            Class<?> targetTypeClass = field.getType();

            Preconditions.checkState(targetTypeClass.isAssignableFrom(TrackorType.class), "Class " + targetTypeClass.getSimpleName() + " is not TrackorType");

            return new JsonPrimitive(((TrackorType) object).getTrackorKey());
        }
    }

    public interface Parser {
        Object parse(Field field, JsonPrimitive jsonPrimitive, Callable<JsonObject> jsonObjectCallable);

        JsonPrimitive parseTo(Field field, Object object);
    }

    interface FieldFilter extends Function<Field, Boolean> {
    }

    interface JsonToPlainObjectHelper<T extends TrackorType> {
        T findExisting(String trackorKey);
    }
}
