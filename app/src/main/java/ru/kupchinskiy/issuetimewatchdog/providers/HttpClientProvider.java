package ru.kupchinskiy.issuetimewatchdog.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import ru.kupchinskiy.issuetimewatchdog.helper.interceptor.ApiBasicAuthInterceptor;

public class HttpClientProvider implements Provider<OkHttpClient> {

    @Inject
    private ApiBasicAuthInterceptor basicAuthInterceptor;

    @Override
    public OkHttpClient get() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(basicAuthInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

}
