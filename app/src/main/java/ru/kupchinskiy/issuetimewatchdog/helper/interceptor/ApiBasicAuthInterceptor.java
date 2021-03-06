package ru.kupchinskiy.issuetimewatchdog.helper.interceptor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import ru.kupchinskiy.issuetimewatchdog.prefs.ApiAuthPrefs;
import ru.kupchinskiy.issuetimewatchdog.services.ApiClient;

@Singleton
@Slf4j
public class ApiBasicAuthInterceptor implements Interceptor {

    @Inject
    private ApiAuthPrefs apiAuthPrefs;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (request.url().host().equals(ApiClient.TRACKOR_HOSTNAME) &&
                (request.url().scheme() + "://").equals(ApiClient.TRACKOR_PROTOCOL)) {
            log.info("Handled request to Trackor");

            String credentials = apiAuthPrefs.getCredentials();

            if (credentials == null) {
                throw new NoApiCredentialsPresentException();
            }

            Request authenticatedRequest = request.newBuilder().header("Authorization", credentials).build();
            return chain.proceed(authenticatedRequest);
        }

        return chain.proceed(request);
    }

    public static class NoApiCredentialsPresentException extends IOException {

        private NoApiCredentialsPresentException() {
            super("No API credentials present!");
        }

    }

}
