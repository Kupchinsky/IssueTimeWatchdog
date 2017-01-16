package ru.killer666.issuetimewatchdog.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import ru.killer666.issuetimewatchdog.helper.interceptor.BasicAuthInterceptor;
import ru.killer666.issuetimewatchdog.helper.interceptor.LoggingInterceptor;

public class HttpClientProvider implements Provider<OkHttpClient> {

    @Inject
    private BasicAuthInterceptor basicAuthInterceptor;

    @Inject
    private LoggingInterceptor loggingInterceptor;

    @Override
    public OkHttpClient get() {
        return new OkHttpClient.Builder()
                .addInterceptor(basicAuthInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

}
