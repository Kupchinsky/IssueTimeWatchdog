package ru.killer666.issuetimewatchdog.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;

import retrofit2.Retrofit;
import ru.killer666.issuetimewatchdog.services.ApiClient;

public class ApiClientProvider implements Provider<ApiClient> {

    @Inject
    private Retrofit retrofit;

    @Override
    public ApiClient get() {
        return this.retrofit.create(ApiClient.class);
    }

}
