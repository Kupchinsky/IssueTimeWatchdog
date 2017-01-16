package ru.killer666.issuetimewatchdog.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import ru.killer666.issuetimewatchdog.services.ApiClient;

public class RetrofitProvider implements Provider<Retrofit> {

    @Inject
    private OkHttpClient httpClient;

    @Override
    public Retrofit get() {
        return new Retrofit.Builder().baseUrl(ApiClient.TRACKOR_BASEURL).client(httpClient).build();
    }

}
