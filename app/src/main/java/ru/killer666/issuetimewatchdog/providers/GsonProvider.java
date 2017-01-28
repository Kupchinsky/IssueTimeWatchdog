package ru.killer666.issuetimewatchdog.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.Date;

import ru.killer666.issuetimewatchdog.adapters.DateSerializer;
import ru.killer666.issuetimewatchdog.helper.RemoteUserSettings;

public class GsonProvider implements Provider<Gson> {

    @Inject
    private RemoteUserSettings remoteUserSettings;

    @Override
    public Gson get() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Date.class, new DateSerializer(remoteUserSettings))
                .create();
    }

}
