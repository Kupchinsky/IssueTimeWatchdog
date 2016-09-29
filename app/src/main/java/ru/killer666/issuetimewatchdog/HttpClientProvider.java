package ru.killer666.issuetimewatchdog;

import com.google.inject.Inject;
import com.google.inject.Provider;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class HttpClientProvider implements Provider<OkHttpClient> {
    @Inject
    private LoginCredentials loginCredentials;

    @Override
    public OkHttpClient get() {
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credentials = HttpClientProvider.this.loginCredentials.getCredentials();

                        if (credentials == null) {
                            return null;
                        }

                        return response.request().newBuilder().header("Authorization", credentials).build();
                    }
                })
                .build();
    }
}
