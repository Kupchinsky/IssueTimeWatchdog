package ru.killer666.issuetimewatchdog.providers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provider;

public class GsonProvider implements Provider<Gson> {

    @Override
    public Gson get() {
        return new GsonBuilder().create();
    }

}
