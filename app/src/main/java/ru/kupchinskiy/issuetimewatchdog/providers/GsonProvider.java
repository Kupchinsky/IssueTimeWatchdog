package ru.kupchinskiy.issuetimewatchdog.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.Date;

import roboguice.inject.ContextSingleton;
import ru.kupchinskiy.issuetimewatchdog.adapters.DateSerializer;

@ContextSingleton
public class GsonProvider implements Provider<Gson> {

    @Inject
    private DateSerializer dateSerializer;

    @Override
    public Gson get() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Date.class, dateSerializer)
                .create();
    }

}
