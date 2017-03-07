package ru.kupchinskiy.issuetimewatchdog.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;

import retrofit2.Retrofit;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient;

public class ApiClientProvider implements Provider<ApiClient> {

    @Inject
    private Retrofit retrofit;

    @Override
    public ApiClient get() {
        return retrofit.create(ApiClient.class);
    }

}
